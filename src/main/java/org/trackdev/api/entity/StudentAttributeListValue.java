package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

/**
 * Stores an individual list item for a LIST-type profile attribute applied to a student.
 * Each row represents one item in the ordered list.
 */
@Entity
@Table(name = "student_attribute_list_values", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "attribute_id", "order_index"})
})
public class StudentAttributeListValue extends BaseEntityLong {

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

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    /**
     * Enum value selected from the referenced ProfileEnum.
     * NULL for STRING-only lists (when the attribute has no enumRef).
     */
    @Column(name = "enum_value", length = 100)
    private String enumValue;

    /**
     * Short title for this list item.
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Extended description for this list item. Supports Markdown formatting.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public StudentAttributeListValue() {}

    public StudentAttributeListValue(User user, ProfileAttribute attribute, int orderIndex, String enumValue, String title, String description) {
        this.user = user;
        this.attribute = attribute;
        this.orderIndex = orderIndex;
        this.enumValue = enumValue;
        this.title = title;
        this.description = description;
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(String enumValue) {
        this.enumValue = enumValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
