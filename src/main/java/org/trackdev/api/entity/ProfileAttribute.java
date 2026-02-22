package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

/**
 * Attribute definition within a profile.
 * Defines the name, type, target object, and who can apply values.
 */
@Entity
@Table(name = "profile_attributes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "profile_id"})
})
public class ProfileAttribute extends BaseEntityLong {

    public static final int NAME_LENGTH = 50;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttributeType type;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttributeTarget target;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "applied_by", length = 20)
    private AttributeAppliedBy appliedBy = AttributeAppliedBy.PROFESSOR;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttributeVisibility visibility = AttributeVisibility.PROFESSOR_ONLY;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "profile_id", insertable = false, updatable = false)
    private Long profileId;

    /**
     * Reference to ProfileEnum when type is ENUM.
     * Must belong to the same profile.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enum_ref_id")
    private ProfileEnum enumRef;

    @Column(name = "enum_ref_id", insertable = false, updatable = false)
    private Long enumRefId;

    /**
     * Default value for this attribute when not explicitly set.
     * For INTEGER/FLOAT types, this is used as fallback in reports.
     */
    @Column(length = 255)
    private String defaultValue;

    /**
     * Minimum value for INTEGER/FLOAT types.
     */
    @Column(name = "min_value", length = 255)
    private String minValue;

    /**
     * Maximum value for INTEGER/FLOAT types.
     */
    @Column(name = "max_value", length = 255)
    private String maxValue;

    public ProfileAttribute() {}

    public ProfileAttribute(String name, AttributeType type, AttributeTarget target, Profile profile) {
        this.name = name;
        this.type = type;
        this.target = target;
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public AttributeTarget getTarget() {
        return target;
    }

    public void setTarget(AttributeTarget target) {
        this.target = target;
    }

    public AttributeAppliedBy getAppliedBy() {
        return appliedBy;
    }

    public void setAppliedBy(AttributeAppliedBy appliedBy) {
        this.appliedBy = appliedBy;
    }

    public AttributeVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(AttributeVisibility visibility) {
        this.visibility = visibility;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Long getProfileId() {
        return profileId;
    }

    public ProfileEnum getEnumRef() {
        return enumRef;
    }

    public void setEnumRef(ProfileEnum enumRef) {
        this.enumRef = enumRef;
    }

    public Long getEnumRefId() {
        return enumRefId;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }
}
