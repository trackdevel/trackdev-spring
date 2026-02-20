package org.trackdev.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Embeddable representing a single enum value with an optional description.
 */
@Embeddable
public class EnumValueEntry {

    @Column(name = "value", length = 100)
    private String value;

    @Column(name = "description", length = 500)
    private String description;

    public EnumValueEntry() {}

    public EnumValueEntry(String value) {
        this.value = value;
    }

    public EnumValueEntry(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumValueEntry that = (EnumValueEntry) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
