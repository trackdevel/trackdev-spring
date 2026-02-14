-- Schema migration V9
-- Discord Integration

-- ============================================
-- CREATE TABLES
-- ============================================

CREATE TABLE IF NOT EXISTS `discord_users_info` (
  `id` varchar(36) NOT NULL,
  `discord_id` varchar(256) DEFAULT NULL,
  `username` varchar(256) DEFAULT NULL,
  `discriminator` varchar(10) DEFAULT NULL,
  `avatar_hash` varchar(512) DEFAULT NULL,
  `access_token` varchar(512) DEFAULT NULL,
  `refresh_token` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================  
-- ADD COLUMNS
-- ============================================

ALTER TABLE `users` ADD COLUMN `discord_info_id` varchar(36) DEFAULT NULL AFTER `github_info_id`;

-- ============================================
-- ADD INDEXES/KEYS
-- ============================================

ALTER TABLE `users` ADD UNIQUE KEY `UK_users_discord_info` (`discord_info_id`);

-- ============================================
-- ADD FOREIGN KEY CONSTRAINTS
-- ============================================

ALTER TABLE `users` ADD CONSTRAINT `FK_users_discord_info` FOREIGN KEY (`discord_info_id`) REFERENCES `discord_users_info` (`id`);
