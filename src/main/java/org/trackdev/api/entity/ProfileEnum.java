package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum definition within a profile.
 * Contains a list of possible values, each with an optional description.
 */
@Entity
@Table(name = "profile_enums", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "profile_id"})
})
public class ProfileEnum extends BaseEntityLong {

    public static final int NAME_LENGTH = 50;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "profile_id", insertable = false, updatable = false)
    private Long profileId;

    @ElementCollection
    @CollectionTable(name = "profile_enum_values", joinColumns = @JoinColumn(name = "enum_id"))
    @OrderColumn(name = "order_index")
    private List<EnumValueEntry> values = new ArrayList<>();

    public ProfileEnum() {}

    public ProfileEnum(String name, Profile profile) {
        this.name = name;
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<EnumValueEntry> getValues() {
        return values;
    }

    public void setValues(List<EnumValueEntry> values) {
        this.values = values;
    }

    /**
     * Returns the value strings only, for validation and backward compatibility.
     */
    public List<String> getValueStrings() {
        return values.stream()
                .map(EnumValueEntry::getValue)
                .collect(Collectors.toList());
    }

    public void addValue(EnumValueEntry entry) {
        if (values.stream().noneMatch(e -> e.getValue().equals(entry.getValue()))) {
            values.add(entry);
        }
    }

    public void removeValue(String value) {
        values.removeIf(e -> e.getValue().equals(value));
    }
}
