CREATE TABLE `personal_access_tokens` (
    `id` varchar(36) NOT NULL,
    `name` varchar(100) NOT NULL,
    `token_hash` varchar(64) NOT NULL,
    `token_prefix` varchar(10) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `expires_at` timestamp NULL,
    `created_at` timestamp NOT NULL,
    `last_used_at` timestamp NULL,
    `revoked` bit(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_pat_token_hash` (`token_hash`),
    KEY `FK_pat_user` (`user_id`),
    CONSTRAINT `FK_pat_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
