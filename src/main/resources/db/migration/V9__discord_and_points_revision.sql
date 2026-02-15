-- Schema migration from V8 to V9
-- Reorganized for proper execution order

-- ============================================
-- CREATE TABLES (without foreign keys)
-- ============================================

CREATE TABLE `discord_users_info` (
	`discriminator` varchar(10),
	`id` varchar(36) NOT NULL,
	`discord_id` varchar(256),
	`username` varchar(256),
	`access_token` varchar(512),
	`avatar_hash` varchar(512),
	`refresh_token` varchar(512),
	PRIMARY KEY (`id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `project_analyses` (
	`processed_prs` int,
	`total_deleted_lines` int,
	`total_files` int,
	`total_prs` int,
	`total_surviving_lines` int,
	`completed_at` timestamp NULL,
	`project_id` bigint NOT NULL,
	`started_at` timestamp NULL,
	`id` varchar(36) NOT NULL,
	`started_by_id` varchar(36) NOT NULL,
	`error_message` varchar(1000),
	`status` enum('DONE', 'FAILED', 'IN_PROGRESS') NOT NULL,
	PRIMARY KEY (`id`),
	KEY `FK6fr91hlv60b8jcyk6i35p67ll` (`project_id`),
	KEY `FKdbycht2kmkoeorbof9cyg54mq` (`started_by_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `points_review_conversations` (
	`proposed_points` int NOT NULL,
	`created_at` timestamp NOT NULL,
	`id` bigint NOT NULL AUTO_INCREMENT,
	`task_id` bigint NOT NULL,
	`updated_at` timestamp NOT NULL,
	`initiator_id` varchar(36) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK9gmrrrnfwk4iwl5cy84xbhwwi` (`task_id`, `initiator_id`),
	KEY `FKk1o0rpsi4vtkutocvtia8spce` (`initiator_id`),
	CONSTRAINT `points_review_conversations_chk_1` CHECK (`proposed_points` >= 0)
) ENGINE InnoDB,
  AUTO_INCREMENT 2,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `project_analysis_files` (
	`additions` int,
	`current_lines` int,
	`deleted_lines` int,
	`deletions` int,
	`surviving_lines` int,
	`sprint_id` bigint,
	`task_id` bigint,
	`status` varchar(20),
	`analysis_id` varchar(36) NOT NULL,
	`author_id` varchar(36),
	`id` varchar(36) NOT NULL,
	`pr_id` varchar(36) NOT NULL,
	`file_path` varchar(500) NOT NULL,
	PRIMARY KEY (`id`),
	KEY `idx_paf_analysis` (`analysis_id`),
	KEY `idx_paf_sprint` (`sprint_id`),
	KEY `idx_paf_author` (`author_id`),
	KEY `FK1c4ixu6jy4lv0sa3yt73hp7u4` (`pr_id`),
	KEY `FKqn59qmatn3elg20lpl2nwduq4` (`task_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `points_review_messages` (
	`conversation_id` bigint NOT NULL,
	`created_at` timestamp NOT NULL,
	`id` bigint NOT NULL AUTO_INCREMENT,
	`author_id` varchar(36) NOT NULL,
	`content` varchar(2000) NOT NULL,
	PRIMARY KEY (`id`),
	KEY `FKokeaigsrq6gx809kdo59vqyxj` (`author_id`),
	KEY `FKk50bo0sxtk02adcai03vsy5s8` (`conversation_id`)
) ENGINE InnoDB,
  AUTO_INCREMENT 3,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `points_review_participants` (
	`conversation_id` bigint NOT NULL,
	`user_id` varchar(36) NOT NULL,
	PRIMARY KEY (`conversation_id`, `user_id`),
	KEY `FKdp0qacy98pxu7h0qxc710hejm` (`user_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `points_review_similar_tasks` (
	`conversation_id` bigint NOT NULL,
	`task_id` bigint NOT NULL,
	PRIMARY KEY (`conversation_id`, `task_id`),
	KEY `FKfnwvqw0yeavptmxlu3p98m9nn` (`task_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `project_analysis_file_lines` (
	`display_order` int NOT NULL,
	`line_number` int,
	`origin_pr_number` int,
	`original_line_number` int,
	`status` varchar(20) NOT NULL,
	`file_id` varchar(36) NOT NULL,
	`id` varchar(36) NOT NULL,
	`commit_sha` varchar(50),
	`author_github_username` varchar(100),
	`author_full_name` varchar(200),
	`commit_url` varchar(500),
	`origin_pr_url` varchar(500),
	`pr_file_url` varchar(500),
	`content` text,
	PRIMARY KEY (`id`),
	KEY `idx_pafl_file` (`file_id`),
	KEY `idx_pafl_line_number` (`line_number`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

-- ============================================
-- ADD COLUMNS
-- ============================================

ALTER TABLE `users` ADD COLUMN `discord_info_id` varchar(36) AFTER `workspace_id`;

-- ============================================
-- ADD INDEXES/KEYS
-- ============================================

ALTER TABLE `users` ADD UNIQUE KEY `UKqbmjqsh94myc7pr1g6rlujnax` (`discord_info_id`);

-- ============================================
-- ADD FOREIGN KEY CONSTRAINTS
-- ============================================

ALTER TABLE `users` ADD CONSTRAINT `FKic1wc4xc8inx85mxh7xhxaiwo` FOREIGN KEY (`discord_info_id`) REFERENCES `discord_users_info` (`id`);

ALTER TABLE `project_analyses` ADD CONSTRAINT `FK6fr91hlv60b8jcyk6i35p67ll` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

ALTER TABLE `project_analyses` ADD CONSTRAINT `FKdbycht2kmkoeorbof9cyg54mq` FOREIGN KEY (`started_by_id`) REFERENCES `users` (`id`);

ALTER TABLE `points_review_conversations` ADD CONSTRAINT `FK4ut5uq60i757re6wlwtk3j63` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

ALTER TABLE `points_review_conversations` ADD CONSTRAINT `FKk1o0rpsi4vtkutocvtia8spce` FOREIGN KEY (`initiator_id`) REFERENCES `users` (`id`);

ALTER TABLE `project_analysis_files` ADD CONSTRAINT `FK1c4ixu6jy4lv0sa3yt73hp7u4` FOREIGN KEY (`pr_id`) REFERENCES `pull_requests` (`id`);

ALTER TABLE `project_analysis_files` ADD CONSTRAINT `FK48wuq09bf72vkxgi832ur8is9` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

ALTER TABLE `project_analysis_files` ADD CONSTRAINT `FK6od1b2gd752vwk9rpnm6ncqw2` FOREIGN KEY (`sprint_id`) REFERENCES `sprints` (`id`);

ALTER TABLE `project_analysis_files` ADD CONSTRAINT `FKod998tiflfbtaq2xpdnm2na40` FOREIGN KEY (`analysis_id`) REFERENCES `project_analyses` (`id`);

ALTER TABLE `project_analysis_files` ADD CONSTRAINT `FKqn59qmatn3elg20lpl2nwduq4` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

ALTER TABLE `points_review_messages` ADD CONSTRAINT `FKk50bo0sxtk02adcai03vsy5s8` FOREIGN KEY (`conversation_id`) REFERENCES `points_review_conversations` (`id`);

ALTER TABLE `points_review_messages` ADD CONSTRAINT `FKokeaigsrq6gx809kdo59vqyxj` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

ALTER TABLE `points_review_participants` ADD CONSTRAINT `FKceeh6symm25dghddnfxaml7qx` FOREIGN KEY (`conversation_id`) REFERENCES `points_review_conversations` (`id`);

ALTER TABLE `points_review_participants` ADD CONSTRAINT `FKdp0qacy98pxu7h0qxc710hejm` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `points_review_similar_tasks` ADD CONSTRAINT `FKcda3au17q07o5wpp74tjdj4pi` FOREIGN KEY (`conversation_id`) REFERENCES `points_review_conversations` (`id`);

ALTER TABLE `points_review_similar_tasks` ADD CONSTRAINT `FKfnwvqw0yeavptmxlu3p98m9nn` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

ALTER TABLE `project_analysis_file_lines` ADD CONSTRAINT `FKocu8juk188hgn486i2sld5a5o` FOREIGN KEY (`file_id`) REFERENCES `project_analysis_files` (`id`);

-- ============================================
-- DROP STATEMENTS
-- ============================================
