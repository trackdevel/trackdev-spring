package org.trackdev.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.trackdev.api.entity.AttributeTarget;
import org.trackdev.api.entity.AttributeType;

import java.util.List;

/**
 * Request model for creating or updating a profile
 */
public class ProfileRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    public String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    public String description;

    @Valid
    public List<EnumRequest> enums;

    @Valid
    public List<AttributeRequest> attributes;

    public static class EnumRequest {
        /**
         * ID of existing enum (null for new enums)
         */
        public Long id;

        @NotBlank(message = "Enum name is required")
        @Size(min = 1, max = 50, message = "Enum name must be between 1 and 50 characters")
        public String name;

        public List<String> values;
    }

    public static class AttributeRequest {
        /**
         * ID of existing attribute (null for new attributes)
         */
        public Long id;

        @NotBlank(message = "Attribute name is required")
        @Size(min = 1, max = 50, message = "Attribute name must be between 1 and 50 characters")
        public String name;

        @NotNull(message = "Attribute type is required")
        public AttributeType type;

        @NotNull(message = "Attribute target is required")
        public AttributeTarget target;

        /**
         * Reference to enum by name (required when type is ENUM)
         */
        public String enumRefName;

        /**
         * Default value for this attribute when not explicitly set on a task.
         * For INTEGER/FLOAT types, this is used as fallback in reports.
         */
        public String defaultValue;
    }
}
