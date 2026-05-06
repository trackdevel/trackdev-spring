CREATE TABLE `user_push_tokens` (
    `id` varchar(36) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `token` varchar(512) NOT NULL,
    `platform` varchar(16) NOT NULL,
    `device_id` varchar(128) NULL,
    `created_at` timestamp NOT NULL,
    `last_seen_at` timestamp NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_upt_token` (`token`),
    KEY `IDX_upt_user` (`user_id`),
    CONSTRAINT `FK_upt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
