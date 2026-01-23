CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
	`used` bit(1) NOT NULL,
	`created_at` timestamp NOT NULL,
	`expires_at` timestamp NOT NULL,
	`id` varchar(36) NOT NULL,
	`user_id` varchar(36) NOT NULL,
	`token` varchar(64) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK71lqwbwtklmljk3qlsugr1mig` (`token`),
	KEY `FKk3ndxg5xp6v7wd4gjyusp15gq` (`user_id`),
	CONSTRAINT `FKk3ndxg5xp6v7wd4gjyusp15gq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;
