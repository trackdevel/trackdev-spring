package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

/**
 * Stores the actual value of a profile attribute for a specific task.
 * Only applies to attributes with target = TASK.
 */
@Entity
@Table(name = "task_attribute_values", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "attribute_id"})
})
public class TaskAttributeValue extends BaseEntityLong {

    public static final int VALUE_LENGTH = 500;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "task_id", insertable = false, updatable = false)
    private Long taskId;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private ProfileAttribute attribute;

    @Column(name = "attribute_id", insertable = false, updatable = false)
    private Long attributeId;

    /**
     * The actual value stored as a string.
     * For ENUM type: stores the enum value name
     * For STRING type: stores the string value
     * For INTEGER type: stores the integer as string
     * For FLOAT type: stores the float as string
     */
    @Column(length = VALUE_LENGTH)
    private String value;

    public TaskAttributeValue() {}

    public TaskAttributeValue(Task task, ProfileAttribute attribute, String value) {
        this.task = task;
        this.attribute = attribute;
        this.value = value;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Long getTaskId() {
        return taskId;
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
