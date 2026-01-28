-- Schema migration V6
-- Organized for proper execution order
-- Generated with Copilot (Claude Sonnet 4.5)

-- ============================================
-- CREATE TABLES (without foreign keys)
-- ============================================

CREATE TABLE `task_attribute_values` (
	`attribute_id` bigint,
	`id` bigint NOT NULL AUTO_INCREMENT,
	`task_id` bigint,
	`value` varchar(500),
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKsiru5j9flfs8iw3639room8r4` (`task_id`, `attribute_id`),
	KEY `FKaim80jexh57hatffubn80702t` (`attribute_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

-- ============================================  
-- ADD COLUMNS
-- ============================================

ALTER TABLE `profile_attributes` ADD COLUMN `default_value` varchar(255) AFTER `name`;

ALTER TABLE `reports` ADD COLUMN `profile_attribute_id` bigint AFTER `id`;

-- ============================================
-- ADD INDEXES/KEYS
-- ============================================

ALTER TABLE `reports` ADD KEY `FKqt366n1fou1iauscfm2k3njyb` (`profile_attribute_id`);

-- ============================================
-- ADD FOREIGN KEY CONSTRAINTS
-- ============================================

ALTER TABLE `profiles` ADD CONSTRAINT `FK3bc2496crlurjp4hd5qe480py` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

ALTER TABLE `reports` ADD CONSTRAINT `FKqt366n1fou1iauscfm2k3njyb` FOREIGN KEY (`profile_attribute_id`) REFERENCES `profile_attributes` (`id`);

ALTER TABLE `task_attribute_values` ADD CONSTRAINT `FKaim80jexh57hatffubn80702t` FOREIGN KEY (`attribute_id`) REFERENCES `profile_attributes` (`id`);

ALTER TABLE `task_attribute_values` ADD CONSTRAINT `FKnoecagfx9038fx8xj2k5lmv5b` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

-- ============================================
-- DROP STATEMENTS
-- ============================================