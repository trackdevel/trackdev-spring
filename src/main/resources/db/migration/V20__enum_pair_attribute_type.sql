-- Add ENUM_PAIR to the AttributeType enum on profile_attributes.type
ALTER TABLE profile_attributes
    MODIFY COLUMN `type` enum('ENUM','FLOAT','INTEGER','STRING','LIST','TEXT','NUMERIC_TEXT','ENUM_PAIR') NOT NULL;

-- Second enum reference column for ENUM_PAIR attributes
ALTER TABLE profile_attributes
    ADD COLUMN `enum_ref_id_2` bigint NULL AFTER `enum_ref_id`,
    ADD KEY `FK_profile_attributes_enum_ref_2` (`enum_ref_id_2`),
    ADD CONSTRAINT `FK_profile_attributes_enum_ref_2`
        FOREIGN KEY (`enum_ref_id_2`) REFERENCES `profile_enums` (`id`);

-- Second value column on task and student attribute value tables.
-- Stores the second half of an ENUM_PAIR value.
ALTER TABLE task_attribute_values
    ADD COLUMN `value_b` varchar(500) NULL AFTER `value`;

ALTER TABLE student_attribute_values
    ADD COLUMN `value_b` varchar(500) NULL AFTER `value`;