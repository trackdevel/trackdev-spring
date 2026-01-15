package org.trackdev.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.CourseInvite;
import org.trackdev.api.entity.Role;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.CourseInviteRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CourseInviteService extends BaseServiceLong<CourseInvite, CourseInviteRepository> {

    private static final int TOKEN_LENGTH = 32;
    private static final int INVITE_EXPIRY_DAYS = 30;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AccessChecker accessChecker;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create and send invitations for a list of emails.
     * Returns the list of created invitations.
     */
    @Transactional
    public List<CourseInvite> createInvitations(Long courseId, Collection<String> emails, String inviterId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, inviterId);
        User inviter = userService.get(inviterId);

        List<CourseInvite> invitations = new ArrayList<>();

        for (String rawEmail : emails) {
            String email = rawEmail.toLowerCase().trim();
            
            // Check if user already exists and is enrolled
            if (userService.existsEmail(email)) {
                User existingUser = userService.getByEmail(email);
                if (course.isStudentEnrolled(existingUser)) {
                    // Already enrolled, skip
                    continue;
                }
                // User exists but not enrolled - enroll them directly
                course.addStudent(existingUser);
                continue;
            }

            // Check if there's already a pending invite for this email and course
            Optional<CourseInvite> existingInvite = repo.findByCourseIdAndEmailAndStatus(courseId, email, CourseInvite.InviteStatus.PENDING);
            if (existingInvite.isPresent()) {
                // Resend the existing invitation (async - won't block)
                sendInviteEmail(existingInvite.get(), course);
                invitations.add(existingInvite.get());
                continue;
            }

            // Create new invitation
            String token = generateUniqueToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(INVITE_EXPIRY_DAYS);
            
            CourseInvite invite = new CourseInvite(token, email, course, inviter, expiresAt);
            repo.save(invite);

            // Send invitation email (async - won't block)
            sendInviteEmail(invite, course);

            invitations.add(invite);
        }

        return invitations;
    }

    /**
     * Accept an invitation by token.
     * If user exists, enroll them. If not, create a new user with temporary password.
     */
    @Transactional
    public AcceptInviteResult acceptInvitation(String token, String password) {
        CourseInvite invite = repo.findByToken(token)
                .orElseThrow(() -> new EntityNotFound(ErrorConstants.INVITE_NOT_FOUND));

        if (!invite.isPending()) {
            if (invite.getStatus() == CourseInvite.InviteStatus.ACCEPTED) {
                throw new ServiceException(ErrorConstants.INVITE_ALREADY_USED);
            } else if (invite.isExpired() || invite.getStatus() == CourseInvite.InviteStatus.EXPIRED) {
                throw new ServiceException(ErrorConstants.INVITE_EXPIRED);
            } else {
                throw new ServiceException(ErrorConstants.INVITE_CANCELLED);
            }
        }

        String email = invite.getEmail();
        Course course = invite.getCourse();
        User user;
        boolean newUserCreated = false;
        boolean passwordChangeRequired = false;

        if (userService.existsEmail(email)) {
            // Existing user - just enroll them
            user = userService.getByEmail(email);
        } else {
            // New user - create account
            if (password == null || password.length() < 8) {
                throw new ServiceException(ErrorConstants.PASSWORD_MINIUM_LENGTH);
            }
            
            String username = email.split("@")[0];
            user = new User(username, username, email, passwordEncoder.encode(password));
            user.setChangePassword(false);
            user.setEnabled(true);
            Role studentRole = roleService.get(UserType.STUDENT);
            user.addRole(studentRole);
            user = userService.save(user);
            newUserCreated = true;
        }

        // Enroll in course
        if (!course.isStudentEnrolled(user)) {
            course.addStudent(user);
        }

        // Mark invitation as accepted
        invite.markAsAccepted(user);
        repo.save(invite);

        String subjectName = course.getSubject() != null ? course.getSubject().getName() : "Course";
        
        return new AcceptInviteResult(
            course.getId(),
            subjectName,
            course.getStartYear(),
            newUserCreated,
            passwordChangeRequired,
            user
        );
    }

    /**
     * Get invite info by token (for displaying invite details before accepting)
     */
    public CourseInvite getInviteByToken(String token) {
        return repo.findByToken(token)
                .orElseThrow(() -> new EntityNotFound(ErrorConstants.INVITE_NOT_FOUND));
    }

    /**
     * Get all pending invites for a course.
     */
    public Collection<CourseInvite> getPendingInvites(Long courseId, String userId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        return repo.findByCourseIdAndStatus(courseId, CourseInvite.InviteStatus.PENDING);
    }

    /**
     * Get all invites for a course.
     */
    public Collection<CourseInvite> getAllInvites(Long courseId, String userId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        return repo.findByCourseId(courseId);
    }

    /**
     * Cancel an invitation.
     */
    @Transactional
    public void cancelInvitation(Long inviteId, String userId) {
        CourseInvite invite = get(inviteId);
        Course course = invite.getCourse();
        accessChecker.checkCanManageCourse(course, userId);

        if (invite.getStatus() != CourseInvite.InviteStatus.PENDING) {
            throw new ServiceException(ErrorConstants.INVITE_CANNOT_CANCEL);
        }

        invite.markAsCancelled();
        repo.save(invite);
    }

    /**
     * Remove a student from a course.
     */
    @Transactional
    public void removeStudent(Long courseId, String studentId, String userId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        
        User student = userService.get(studentId);
        if (!course.isStudentEnrolled(student)) {
            throw new ServiceException(ErrorConstants.STUDENT_NOT_ENROLLED);
        }
        
        course.removeStudent(student);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH);
        } while (repo.findByToken(token).isPresent());
        return token;
    }

    private void sendInviteEmail(CourseInvite invite, Course course) {
        emailSenderService.sendCourseInviteEmail(
            invite.getEmail(),
            invite.getToken(),
            course.getSubject() != null ? course.getSubject().getName() : "Course",
            course.getStartYear(),
            invite.getInvitedBy().getUsername(),
            course.getLanguage() != null ? course.getLanguage() : "en"
        );
    }

    /**
     * Result of accepting an invitation
     */
    public static class AcceptInviteResult {
        private final Long courseId;
        private final String courseName;
        private final Integer startYear;
        private final boolean newUserCreated;
        private final boolean passwordChangeRequired;
        private final User user;

        public AcceptInviteResult(Long courseId, String courseName, Integer startYear,
                                   boolean newUserCreated, boolean passwordChangeRequired, User user) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.startYear = startYear;
            this.newUserCreated = newUserCreated;
            this.passwordChangeRequired = passwordChangeRequired;
            this.user = user;
        }

        public Long getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        public Integer getStartYear() { return startYear; }
        public boolean isNewUserCreated() { return newUserCreated; }
        public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
        public User getUser() { return user; }
    }
}
