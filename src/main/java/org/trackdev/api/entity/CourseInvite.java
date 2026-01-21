package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.time.ZoneId;

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

    @Column(length = 200)
    private String fullName;

    @NotNull
    @Column(length = 128)
    private String email;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @Column(name = "course_id", insertable = false, updatable = false)
    private Long courseId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User invitedBy;

    @Column(name = "invited_by", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String invitedById;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InviteStatus status = InviteStatus.PENDING;

    @NotNull
    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime expiresAt;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User acceptedBy;

    @Column(name = "accepted_by", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String acceptedById;

    public CourseInvite() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public CourseInvite(String token, String fullName, String email, Course course, User invitedBy, ZonedDateTime expiresAt) {
        this();
        this.token = token;
        this.fullName = fullName;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ZonedDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(ZonedDateTime acceptedAt) {
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
        return expiresAt != null && ZonedDateTime.now(ZoneId.of("UTC")).isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == InviteStatus.PENDING && !isExpired();
    }

    public void markAsAccepted(User user) {
        this.status = InviteStatus.ACCEPTED;
        this.acceptedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.acceptedBy = user;
    }

    public void markAsExpired() {
        this.status = InviteStatus.EXPIRED;
    }

    public void markAsCancelled() {
        this.status = InviteStatus.CANCELLED;
    }
}
