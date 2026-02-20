package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

/**
 * Stores the actual value of a profile attribute for a specific student.
 * Only applies to attributes with target = STUDENT.
 */
@Entity
@Table(name = "student_attribute_values", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "attribute_id"})
})
public class StudentAttributeValue extends BaseEntityLong {

    public static final int VALUE_LENGTH = 500;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String userId;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private ProfileAttribute attribute;

    @Column(name = "attribute_id", insertable = false, updatable = false)
    private Long attributeId;

    /**
     * The actual value stored as a string.
     */
    @Column(length = VALUE_LENGTH)
    private String value;

    public StudentAttributeValue() {}

    public StudentAttributeValue(User user, ProfileAttribute attribute, String value) {
        this.user = user;
        this.attribute = attribute;
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public ProfileAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(ProfileAttribute attribute) {
        this.attribute = attribute;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
