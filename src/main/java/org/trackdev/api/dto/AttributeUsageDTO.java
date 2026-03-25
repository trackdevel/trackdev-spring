package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO that describes how many instantiated values an attribute has,
 * along with a sample of the first values for preview purposes.
 */
@Data
public class AttributeUsageDTO {
    private long totalCount;
    private List<AttributeUsageSampleDTO> samples;

    @Data
    public static class AttributeUsageSampleDTO {
        private String entityType;
        private String entityName;
        private String value;

        public AttributeUsageSampleDTO(String entityType, String entityName, String value) {
            this.entityType = entityType;
            this.entityName = entityName;
            this.value = value;
        }
    }
}
