package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an invitation to join a course.
 * Invitations are single-use tokens sent to students via email.
 */
@Entity
@Table(name = "course_invites")
public class CourseInvite extends BaseEntityLong {

    public enum InviteStatus {
        PENDING,
        ACCEPTED,
        EXPIRED,
        CANCELLED
    }

    @NotNull
    @Column(unique = true, length = 64)
    private String token;

    @NotNull
    @Column(length = 128)
    private String email;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId")
    private Course course;

    @Column(name = "courseId", insertable = false, updatable = false)
    private Long courseId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitedBy")
    private User invitedBy;

    @Column(name = "invitedBy", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String invitedById;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InviteStatus status = InviteStatus.PENDING;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptedBy")
    private User acceptedBy;

    @Column(name = "acceptedBy", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String acceptedById;

    public CourseInvite() {
        this.createdAt = LocalDateTime.now();
    }

    public CourseInvite(String token, String email, Course course, User invitedBy, LocalDateTime expiresAt) {
        this();
        this.token = token;
        this.email = email.toLowerCase().trim();
        this.course = course;
        this.invitedBy = invitedBy;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase().trim();
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getCourseId() {
        return courseId;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getInvitedById() {
        return invitedById;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public User getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(User acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public String getAcceptedById() {
        return acceptedById;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == InviteStatus.PENDING && !isExpired();
    }

    public void markAsAccepted(User user) {
        this.status = InviteStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.acceptedBy = user;
    }

    public void markAsExpired() {
        this.status = InviteStatus.EXPIRED;
    }

    public void markAsCancelled() {
        this.status = InviteStatus.CANCELLED;
    }
}
