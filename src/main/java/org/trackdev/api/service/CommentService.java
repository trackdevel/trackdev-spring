package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Comment;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.CommentRepository;
import org.trackdev.api.utils.ErrorConstants;
import org.trackdev.api.utils.HtmlSanitizer;

import java.util.Collection;

@Service
public class CommentService extends BaseServiceLong<Comment, CommentRepository>{

    @Autowired
    private UserService userService;

    public Comment addComment(String content, User author, Task task) {
        // Sanitize HTML content to prevent XSS attacks
        String sanitizedContent = HtmlSanitizer.sanitize(content);
        Comment comment = new Comment(sanitizedContent, author, task);
        repo.save(comment);
        return comment;
    }

    public Collection<Comment> getComments(Long taskId) {
        return repo().findByTaskId(taskId);
    }

    /**
     * Edit a comment. 
     * - Students can only edit their own comments
     * - Professors owning the project can edit any comment
     */
    @Transactional
    public Comment editComment(Long commentId, String newContent, String userId) {
        Comment comment = repo.findById(commentId)
                .orElseThrow(() -> new EntityNotFound(ErrorConstants.COMMENT_NOT_FOUND));
        
        User user = userService.get(userId);
        
        // Check authorization
        if (!canEditComment(comment, user)) {
            throw new ServiceException(ErrorConstants.CANNOT_EDIT_OTHERS_COMMENT);
        }
        
        // Sanitize and update content
        String sanitizedContent = HtmlSanitizer.sanitize(newContent);
        comment.setContent(sanitizedContent);
        return repo.save(comment);
    }

    /**
     * Delete a comment.
     * - Students CANNOT delete any comment
     * - Professors owning the project can delete any comment
     */
    @Transactional
    public void deleteComment(Long commentId, String userId) {
        Comment comment = repo.findById(commentId)
                .orElseThrow(() -> new EntityNotFound(ErrorConstants.COMMENT_NOT_FOUND));
        
        User user = userService.get(userId);
        
        // Check authorization - only professors can delete
        if (!canDeleteComment(comment, user)) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_COMMENT);
        }
        
        repo.delete(comment);
    }

    /**
     * Check if user can edit a comment.
     * - Author can edit their own comment
     * - Course owner (professor) can edit any comment in their project
     * - Admin can edit any comment
     */
    public boolean canEditComment(Comment comment, User user) {
        // Author can always edit their own comment
        if (comment.getAuthor() != null && comment.getAuthor().getId().equals(user.getId())) {
            return true;
        }
        
        // Admin can edit any comment
        if (user.isUserType(UserType.ADMIN)) {
            return true;
        }
        
        // Professor who owns the course can edit any comment
        Task task = comment.getTask();
        if (task != null && task.getProject() != null) {
            Project project = task.getProject();
            Course course = project.getCourse();
            if (course != null && course.getOwnerId().equals(user.getId())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if user can delete a comment.
     * - Students CANNOT delete any comment
     * - Course owner (professor) can delete any comment in their project
     * - Admin can delete any comment
     */
    public boolean canDeleteComment(Comment comment, User user) {
        // Admin can delete any comment
        if (user.isUserType(UserType.ADMIN)) {
            return true;
        }
        
        // Professor who owns the course can delete any comment
        Task task = comment.getTask();
        if (task != null && task.getProject() != null) {
            Project project = task.getProject();
            Course course = project.getCourse();
            if (course != null && course.getOwnerId().equals(user.getId())) {
                return true;
            }
        }
        
        // Students cannot delete any comment
        return false;
    }
}
