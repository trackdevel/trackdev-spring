-- ============================================
-- Create tables (without foreign key constraints)
-- ============================================

CREATE TABLE IF NOT EXISTS `profiles` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`owner_id` varchar(36),
	`name` varchar(100),
	`description` varchar(500),
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKmfr2ultil6acd2q3f1lodmkm0` (`name`, `owner_id`),
	KEY `FK3bc2496crlurjp4hd5qe480py` (`owner_id`)
) ENGINE InnoDB,
  AUTO_INCREMENT 3,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `profile_enums` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`profile_id` bigint,
	`name` varchar(50),
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK5xyntntdc6ayl404pj103ubyu` (`name`, `profile_id`),
	KEY `FKkvh0ag2w93s0tawtq3rw6r4ty` (`profile_id`)
) ENGINE InnoDB,
  AUTO_INCREMENT 4,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `profile_attributes` (
	`enum_ref_id` bigint,
	`id` bigint NOT NULL AUTO_INCREMENT,
	`profile_id` bigint,
	`name` varchar(50),
	`target` enum('PULL_REQUEST', 'STUDENT', 'TASK'),
	`type` enum('ENUM', 'FLOAT', 'INTEGER', 'STRING'),
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKii3xxf7d4ggxn5ounnggg2e9m` (`name`, `profile_id`),
	KEY `FKide8cold6ua76warl4xnwr6oe` (`enum_ref_id`),
	KEY `FK8qpcr7ddtu2buxp41l3r4kbgu` (`profile_id`)
) ENGINE InnoDB,
  AUTO_INCREMENT 13,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `profile_enum_values` (
	`order_index` int NOT NULL,
	`enum_id` bigint NOT NULL,
	`value` varchar(100),
	PRIMARY KEY (`order_index`, `enum_id`),
	KEY `FKq6h6rbnt1wic0gv6kcwsqwwpx` (`enum_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

-- Add profile_id column to courses table
ALTER TABLE `courses` ADD COLUMN `profile_id` bigint AFTER `id`, ADD KEY `FKfhxp8h4fcl0r9y7uyem4jpcmd` (`profile_id`);

-- ============================================
-- Add foreign key constraints
-- ============================================

ALTER TABLE `courses` ADD CONSTRAINT `FKfhxp8h4fcl0r9y7uyem4jpcmd` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `profile_enums` ADD CONSTRAINT `FKkvh0ag2w93s0tawtq3rw6r4ty` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `profile_attributes` ADD CONSTRAINT `FK8qpcr7ddtu2buxp41l3r4kbgu` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`);
ALTER TABLE `profile_attributes` ADD CONSTRAINT `FKide8cold6ua76warl4xnwr6oe` FOREIGN KEY (`enum_ref_id`) REFERENCES `profile_enums` (`id`);

ALTER TABLE `profile_enum_values` ADD CONSTRAINT `FKq6h6rbnt1wic0gv6kcwsqwwpx` FOREIGN KEY (`enum_id`) REFERENCES `profile_enums` (`id`);
