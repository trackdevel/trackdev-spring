package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

/**
 * Attribute definition within a profile.
 * Defines the name, type, and target object for the attribute.
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
}
