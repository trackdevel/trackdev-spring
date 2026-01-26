package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile entity that contains attribute definitions and enum definitions.
 * Profiles are created by professors and can be applied to courses.
 */
@Entity
@Table(name = "profiles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "owner_id"})
})
public class Profile extends BaseEntityLong {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 100;
    public static final int DESCRIPTION_LENGTH = 500;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @Column(length = DESCRIPTION_LENGTH)
    private String description;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "owner_id", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String ownerId;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileEnum> enums = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileAttribute> attributes = new ArrayList<>();

    public Profile() {}

    public Profile(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public List<ProfileEnum> getEnums() {
        return enums;
    }

    public void setEnums(List<ProfileEnum> enums) {
        this.enums = enums;
    }

    public void addEnum(ProfileEnum profileEnum) {
        enums.add(profileEnum);
        profileEnum.setProfile(this);
    }

    public void removeEnum(ProfileEnum profileEnum) {
        enums.remove(profileEnum);
        profileEnum.setProfile(null);
    }

    public List<ProfileAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ProfileAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(ProfileAttribute attribute) {
        attributes.add(attribute);
        attribute.setProfile(this);
    }

    public void removeAttribute(ProfileAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProfile(null);
    }
}
