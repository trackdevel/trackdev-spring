-- Schema migration V10: Profile enhancements
-- Adds: applied_by, min/max for attributes, enum value descriptions,
-- student_attribute_values table, pull_request_attribute_values table

-- ============================================
-- CREATE TABLES (without foreign keys)
-- ============================================

CREATE TABLE `student_attribute_values` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`user_id` varchar(36),
	`attribute_id` bigint,
	`value` varchar(500),
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK_student_attr_value` (`user_id`, `attribute_id`),
	KEY `FK_sav_attribute` (`attribute_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `pull_request_attribute_values` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`pull_request_id` varchar(36),
	`attribute_id` bigint,
	`value` varchar(500),
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK_pr_attr_value` (`pull_request_id`, `attribute_id`),
	KEY `FK_prav_attribute` (`attribute_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

-- ============================================
-- ADD COLUMNS
-- ============================================

-- Add applied_by to profile_attributes (default PROFESSOR for backward compat)
ALTER TABLE `profile_attributes` ADD COLUMN `applied_by` enum('PROFESSOR', 'STUDENT') NOT NULL DEFAULT 'PROFESSOR' AFTER `target`;

-- Add min_value and max_value to profile_attributes
ALTER TABLE `profile_attributes` ADD COLUMN `min_value` varchar(255) AFTER `default_value`;
ALTER TABLE `profile_attributes` ADD COLUMN `max_value` varchar(255) AFTER `min_value`;

-- Add description to profile_enum_values
ALTER TABLE `profile_enum_values` ADD COLUMN `description` varchar(500) AFTER `value`;

-- ============================================
-- ADD FOREIGN KEY CONSTRAINTS
-- ============================================

ALTER TABLE `student_attribute_values` ADD CONSTRAINT `FK_sav_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `profile_attributes` (`id`);
ALTER TABLE `student_attribute_values` ADD CONSTRAINT `FK_sav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `pull_request_attribute_values` ADD CONSTRAINT `FK_prav_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `profile_attributes` (`id`);
ALTER TABLE `pull_request_attribute_values` ADD CONSTRAINT `FK_prav_pr` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`);
