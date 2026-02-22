package org.trackdev.api.entity;

/**
 * Defines who can view/read values of a profile attribute.
 */
public enum AttributeVisibility {
    /** Only professors and admins can see */
    PROFESSOR_ONLY,
    /** All students in the same project can see */
    PROJECT_STUDENTS,
    /** Only the student with edit access can see (e.g. task assignee) */
    ASSIGNED_STUDENT
}
