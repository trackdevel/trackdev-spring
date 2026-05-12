package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.PointsReviewConversation;
import org.trackdev.api.entity.PointsReviewMessage;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Domain-level email notifications.
 *
 * Mirrors {@link FcmNotificationService} for the email channel: resolves the
 * relevant recipients for a domain event, filters them by their per-user
 * notification preference, and delegates the actual templating + sending to
 * {@link EmailSenderService}.
 */
@Service
public class EmailNotificationService {

    private static final int BODY_PREVIEW_MAX = 240;

    private final EmailSenderService emailSenderService;

    public EmailNotificationService(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    /**
     * Notify the points-review recipients (initiator + participants + course owner, minus the actor)
     * that a new conversation has been opened on a task. Honors notifyPointsReview.
     */
    public void notifyPointsReviewCreated(PointsReviewConversation conversation, User actor) {
        if (conversation == null) return;
        Task task = conversation.getTask();
        if (task == null) return;
        Set<User> recipients = pointsReviewRecipients(conversation, actor);
        if (recipients.isEmpty()) return;

        String actorName = actor != null ? actor.getFullName() : "A team member";
        String taskKey = task.getTaskKey();
        String taskName = task.getName();
        Long taskId = task.getId();
        String language = courseLanguage(task);

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyPointsReview())) continue;
            if (r.getEmail() == null || r.getEmail().isBlank()) continue;
            emailSenderService.sendPointsReviewCreatedEmail(
                r.getEmail(), taskKey, taskName, actorName, taskId, language);
        }
    }

    /**
     * Notify all current points-review recipients (minus the message author) of a new message.
     * Honors notifyPointsReview.
     */
    public void notifyPointsReviewMessage(PointsReviewConversation conversation,
                                          PointsReviewMessage message) {
        if (conversation == null || message == null) return;
        Task task = conversation.getTask();
        if (task == null) return;
        User author = message.getAuthor();
        Set<User> recipients = pointsReviewRecipients(conversation, author);
        if (recipients.isEmpty()) return;

        String authorName = author != null ? author.getFullName() : "Someone";
        String taskKey = task.getTaskKey();
        String taskName = task.getName();
        Long taskId = task.getId();
        String language = courseLanguage(task);
        String preview = preview(message.getContent());

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyPointsReview())) continue;
            if (r.getEmail() == null || r.getEmail().isBlank()) continue;
            emailSenderService.sendPointsReviewMessageEmail(
                r.getEmail(), taskKey, taskName, authorName, preview, taskId, language);
        }
    }

    /**
     * Notify all current points-review recipients (minus the editor) that a message
     * in the conversation has been edited. Honors notifyPointsReview.
     */
    public void notifyPointsReviewMessageEdited(PointsReviewConversation conversation,
                                                PointsReviewMessage message,
                                                User editor) {
        if (conversation == null || message == null) return;
        Task task = conversation.getTask();
        if (task == null) return;
        User actor = editor != null ? editor : message.getAuthor();
        Set<User> recipients = pointsReviewRecipients(conversation, actor);
        if (recipients.isEmpty()) return;

        String authorName = actor != null ? actor.getFullName() : "Someone";
        String taskKey = task.getTaskKey();
        String taskName = task.getName();
        Long taskId = task.getId();
        String language = courseLanguage(task);
        String preview = preview(message.getContent());

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyPointsReview())) continue;
            if (r.getEmail() == null || r.getEmail().isBlank()) continue;
            emailSenderService.sendPointsReviewMessageEditedEmail(
                r.getEmail(), taskKey, taskName, authorName, preview, taskId, language);
        }
    }

    // -- helpers ------------------------------------------------------------

    private Set<User> pointsReviewRecipients(PointsReviewConversation conversation, User actor) {
        Set<User> recipients = new LinkedHashSet<>();
        if (conversation.getInitiator() != null) {
            recipients.add(conversation.getInitiator());
        }
        if (conversation.getParticipants() != null) {
            recipients.addAll(conversation.getParticipants());
        }
        Task task = conversation.getTask();
        if (task != null && task.getProject() != null && task.getProject().getCourse() != null) {
            User professor = task.getProject().getCourse().getOwner();
            if (professor != null) {
                recipients.add(professor);
            }
        }
        if (actor != null) {
            recipients.removeIf(u -> u != null && u.getId() != null && u.getId().equals(actor.getId()));
        }
        recipients.removeIf(u -> u == null || u.getId() == null);
        return recipients;
    }

    private static String courseLanguage(Task task) {
        if (task == null) return "en";
        Project project = task.getProject();
        if (project == null) return "en";
        Course course = project.getCourse();
        if (course == null || course.getLanguage() == null) return "en";
        return course.getLanguage();
    }

    private static String preview(String html) {
        if (html == null) return "";
        String stripped = html.replaceAll("<[^>]*>", "").trim();
        if (stripped.length() <= BODY_PREVIEW_MAX) return stripped;
        return stripped.substring(0, BODY_PREVIEW_MAX - 1) + "…";
    }
}
