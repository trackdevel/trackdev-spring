-- ============================================================
-- TrackDev Database Schema
-- Version 2: Tables created first, then constraints added
-- ============================================================

-- ============================================================
-- PART 1: CREATE TABLES (without foreign key constraints)
-- ============================================================

-- Independent tables (no foreign keys)

CREATE TABLE IF NOT EXISTS `workspaces` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role` (
  `user_type` tinyint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkl1an1fehf1xcgpo3wrrbojbh` (`user_type`),
  CONSTRAINT `role_chk_1` CHECK ((`user_type` between 0 and 3))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `emails` (
  `timestamp` datetime(6) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `destination` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `github_users_info` (
  `id` varchar(36) NOT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `github_token` varchar(255) DEFAULT NULL,
  `html_url` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tables with foreign keys (FK constraints removed, indexes kept)

CREATE TABLE IF NOT EXISTS `users` (
  `capital_letters` varchar(2) DEFAULT NULL,
  `change_password` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `current_project` bigint DEFAULT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `github_info_id` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `username` varchar(50) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(128) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `recovery_code` varchar(255) DEFAULT NULL,
  `random` varbinary(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKkbpq45a8lit3ih8226owpcx52` (`github_info_id`),
  KEY `FK1hj24ju99ddbjpnv66ycum2k6` (`workspace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `users_roles` (
  `role_id` bigint NOT NULL,
  `user_id` varchar(36) NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `FK2o0jvgh89lemvvo17cbqvdxaa` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_activity_access` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_accessed_at` timestamp NULL DEFAULT NULL,
  `user_id` varchar(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpwmtdx218xgj34yhb4caj0831` (`user_id`),
  KEY `idx_user_activity_access_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `workspace_id` bigint DEFAULT NULL,
  `owner_id` varchar(36) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `acronym` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjrjxaoibkgf8f8wtdy2y1dbb8` (`owner_id`),
  KEY `FK1out12irmomck9xdvbomo1uqt` (`workspace_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `courses` (
  `start_year` int DEFAULT NULL,
  `language` varchar(5) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint DEFAULT NULL,
  `owner_id` varchar(36) DEFAULT NULL,
  `github_organization` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3jkpttw97v8c2yuqx4prsm4m9` (`owner_id`),
  KEY `FK5tckdihu5akp5nkxiacx1gfhi` (`subject_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `courses_students` (
  `course_id` bigint NOT NULL,
  `student_id` varchar(36) NOT NULL,
  PRIMARY KEY (`course_id`,`student_id`),
  KEY `FK9u2re1bdq1hmsyds5rfoliq35` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `course_invites` (
  `accepted_at` datetime(6) DEFAULT NULL,
  `course_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accepted_by` varchar(36) DEFAULT NULL,
  `accepted_by_id` varchar(36) DEFAULT NULL,
  `invited_by` varchar(36) DEFAULT NULL,
  `invited_by_id` varchar(36) NOT NULL,
  `token` varchar(64) NOT NULL,
  `email` varchar(128) NOT NULL,
  `full_name` varchar(200) DEFAULT NULL,
  `status` enum('ACCEPTED','CANCELLED','EXPIRED','PENDING') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpb2xa5qbqpk8qvhlny59dkrq9` (`token`),
  KEY `FK3i71gu2e7a351pqjkiht51cea` (`accepted_by_id`),
  KEY `FKtm14w3jgoj6ak131ptn269b59` (`course_id`),
  KEY `FKp3dpxm1xi8ny8gdiivmwrrjk3` (`invited_by_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sprint_patterns` (
  `course_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8mqggoll94kxo6kgkil004ejo` (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sprint_pattern_items` (
  `order_index` int DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sprint_pattern_id` bigint DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7m06m912ideqrimd92m7jcqye` (`sprint_pattern_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `projects` (
  `next_task_number` int DEFAULT NULL,
  `qualification` double DEFAULT NULL,
  `course_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcxqk67qijm09gpgig8a997mb0` (`slug`),
  KEY `FKpefksuunh3nqudnug39tn30rh` (`course_id`),
  CONSTRAINT `projects_chk_1` CHECK ((`qualification` <= 10))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `projects_members` (
  `project_id` bigint NOT NULL,
  `user_id` varchar(36) NOT NULL,
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `FK1aaecn2fdeir463r9ppdsje67` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `github_repos` (
  `webhook_active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_sync_at` datetime(6) DEFAULT NULL,
  `project_id` bigint DEFAULT NULL,
  `webhook_id` bigint DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `owner` varchar(200) DEFAULT NULL,
  `repo_name` varchar(200) DEFAULT NULL,
  `access_token` varchar(500) DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `webhook_secret` varchar(500) DEFAULT NULL,
  `webhook_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9sxogdop2jjndemkqoib00n03` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sprints` (
  `status` tinyint DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKke5a9e380ibc0xugykeqaktp4` (`project_id`),
  CONSTRAINT `sprints_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tasks` (
  `estimation_points` int DEFAULT NULL,
  `frozen` bit(1) DEFAULT NULL,
  `rank` int DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `task_number` int DEFAULT NULL,
  `type` tinyint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_task_id` bigint DEFAULT NULL,
  `project_id` bigint DEFAULT NULL,
  `task_key` varchar(10) DEFAULT NULL,
  `assignee_id` varchar(36) DEFAULT NULL,
  `reporter_id` varchar(36) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhvssn5g17tcsicd74yodxa923` (`task_key`),
  KEY `FKekr1dgiqktpyoip3qmp6lxsit` (`assignee_id`),
  KEY `FK76tiq4q248au3u79a8nkexoth` (`parent_task_id`),
  KEY `FKsfhn82y57i3k9uxww1s007acc` (`project_id`),
  KEY `FKbvjdsa9y725wovwlq4sjhodyk` (`reporter_id`),
  CONSTRAINT `tasks_chk_1` CHECK ((`status` between 0 and 5)),
  CONSTRAINT `tasks_chk_2` CHECK ((`type` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=562 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sprints_active_tasks` (
  `sprint_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  KEY `FKfkmb83ugy72xn1sk487lkljbr` (`task_id`),
  KEY `FKm4wqkk1em38y8983rlm6vpjs3` (`sprint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sprint_changes` (
  `status` tinyint DEFAULT NULL,
  `changed_at` timestamp NULL DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sprint_id` bigint DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `task_id` bigint DEFAULT NULL,
  `type` varchar(31) NOT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr8lyp68airaqemivd5rxysie9` (`author_id`),
  KEY `FKkve1jbrh4ojjqeji7rb5ei74` (`sprint_id`),
  KEY `FKc18hhtxltwhet54fm99ts8tok` (`task_id`),
  CONSTRAINT `sprint_changes_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `task_changes` (
  `changed_at` timestamp NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `type` varchar(31) NOT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `new_value` varchar(255) DEFAULT NULL,
  `old_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK30q3x8cqbvq3aht17qge3gqf0` (`author_id`),
  KEY `FKikakdb0pioe4pxplfnpb6xqjw` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `comments` (
  `date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn2na60ukhs76ibtpt9burkm27` (`author_id`),
  KEY `FKi7pp0331nbiwd2844kg78kfwb` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `points_reviews` (
  `points` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2dii35jl989jtdyj0di6urdtc` (`task_id`),
  KEY `FKc9tf6shy68ifxogtk9ag0iuy8` (`user_id`),
  CONSTRAINT `points_reviews_chk_1` CHECK ((`points` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `work_logs` (
  `time_seconds` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `time_stamp` timestamp NULL DEFAULT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk09kchrte8yopfx4jkkl0e8j2` (`author_id`),
  KEY `FKnr36yywrumho6jtiwxw25swb9` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pull_requests` (
  `merged` bit(1) DEFAULT NULL,
  `pr_number` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `state` varchar(20) DEFAULT NULL,
  `node_id` varchar(32) NOT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `repo_full_name` varchar(200) DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa8oqol0kr7gh7ut27byc1wywb` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tasks_pull_requests` (
  `task_id` bigint NOT NULL,
  `pull_request_id` varchar(36) NOT NULL,
  PRIMARY KEY (`task_id`,`pull_request_id`),
  KEY `FKkkhm4xhv8iegsi0vnfkewg1vf` (`pull_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pull_request_changes` (
  `merged` bit(1) DEFAULT NULL,
  `pr_number` int DEFAULT NULL,
  `changed_at` timestamp NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(31) NOT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `pull_request_id` varchar(36) DEFAULT NULL,
  `github_user` varchar(100) DEFAULT NULL,
  `merged_by` varchar(100) DEFAULT NULL,
  `repo_full_name` varchar(200) DEFAULT NULL,
  `pr_title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs9u9scgn4lmela0l93oauo988` (`author_id`),
  KEY `FKoyd3feeyemehcnrrbtx991iu7` (`pull_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pr_notes` (
  `level` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author_id` varchar(36) DEFAULT NULL,
  `pull_request_id` varchar(36) DEFAULT NULL,
  `subject_id` varchar(36) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi2m0sddqck59d1etbdtlq1o1a` (`author_id`),
  KEY `FK5rik8ir3ef26s7cim8b9v12dh` (`pull_request_id`),
  KEY `FK8nhlsjg4wo7ctk1kjkf2a5rly` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `reports` (
  `course_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` varchar(36) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `column_type` enum('SPRINTS','STUDENTS') DEFAULT NULL,
  `element` enum('TASK') DEFAULT NULL,
  `magnitude` enum('ESTIMATION_POINTS','PULL_REQUESTS') DEFAULT NULL,
  `row_type` enum('SPRINTS','STUDENTS') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkfeng1245vlaismuh0apsgvio` (`course_id`),
  KEY `FKkv9jna7htxa2xcpsr2aih1hqc` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `activities` (
  `created_at` timestamp NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `actor_id` varchar(36) NOT NULL,
  `message` varchar(500) DEFAULT NULL,
  `new_value` varchar(255) DEFAULT NULL,
  `old_value` varchar(255) DEFAULT NULL,
  `type` enum('COMMENT_ADDED','PR_LINKED','PR_MERGED','PR_STATE_CHANGED','PR_UNLINKED','TASK_ADDED_TO_SPRINT','TASK_ASSIGNED','TASK_CREATED','TASK_ESTIMATION_CHANGED','TASK_REMOVED_FROM_SPRINT','TASK_STATUS_CHANGED','TASK_UNASSIGNED','TASK_UPDATED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activity_project` (`project_id`),
  KEY `idx_activity_created_at` (`created_at`),
  KEY `FKmfjrc8jdvy0yrr7x67qmrxkue` (`actor_id`),
  KEY `FKl2c4tfjpf89kfw8pd3b4msbv3` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=564 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- PART 2: ADD FOREIGN KEY CONSTRAINTS
-- ============================================================

-- users constraints
ALTER TABLE `users`
  ADD CONSTRAINT `FK1hj24ju99ddbjpnv66ycum2k6` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`),
  ADD CONSTRAINT `FK30gxix6jj5jq40rqgeefs4ovs` FOREIGN KEY (`github_info_id`) REFERENCES `github_users_info` (`id`);

-- users_roles constraints
ALTER TABLE `users_roles`
  ADD CONSTRAINT `FK2o0jvgh89lemvvo17cbqvdxaa` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKt4v0rrweyk393bdgt107vdx0x` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`);

-- user_activity_access constraints
ALTER TABLE `user_activity_access`
  ADD CONSTRAINT `FKtiiqfr64ll70k39dgvgkg25t8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- subjects constraints
ALTER TABLE `subjects`
  ADD CONSTRAINT `FK1out12irmomck9xdvbomo1uqt` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`),
  ADD CONSTRAINT `FKjrjxaoibkgf8f8wtdy2y1dbb8` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

-- courses constraints
ALTER TABLE `courses`
  ADD CONSTRAINT `FK3jkpttw97v8c2yuqx4prsm4m9` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FK5tckdihu5akp5nkxiacx1gfhi` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`);

-- courses_students constraints
ALTER TABLE `courses_students`
  ADD CONSTRAINT `FK9u2re1bdq1hmsyds5rfoliq35` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKcj1bvqj437mdtgllmwcd41f2u` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- course_invites constraints
ALTER TABLE `course_invites`
  ADD CONSTRAINT `FK3i71gu2e7a351pqjkiht51cea` FOREIGN KEY (`accepted_by_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKp3dpxm1xi8ny8gdiivmwrrjk3` FOREIGN KEY (`invited_by_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKtm14w3jgoj6ak131ptn269b59` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- sprint_patterns constraints
ALTER TABLE `sprint_patterns`
  ADD CONSTRAINT `FK8mqggoll94kxo6kgkil004ejo` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- sprint_pattern_items constraints
ALTER TABLE `sprint_pattern_items`
  ADD CONSTRAINT `FK7m06m912ideqrimd92m7jcqye` FOREIGN KEY (`sprint_pattern_id`) REFERENCES `sprint_patterns` (`id`);

-- projects constraints
ALTER TABLE `projects`
  ADD CONSTRAINT `FKpefksuunh3nqudnug39tn30rh` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- projects_members constraints
ALTER TABLE `projects_members`
  ADD CONSTRAINT `FK1aaecn2fdeir463r9ppdsje67` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKkv2pepk8xy0u74k3l61if6efq` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- github_repos constraints
ALTER TABLE `github_repos`
  ADD CONSTRAINT `FK9sxogdop2jjndemkqoib00n03` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- sprints constraints
ALTER TABLE `sprints`
  ADD CONSTRAINT `FKke5a9e380ibc0xugykeqaktp4` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- tasks constraints
ALTER TABLE `tasks`
  ADD CONSTRAINT `FK76tiq4q248au3u79a8nkexoth` FOREIGN KEY (`parent_task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKbvjdsa9y725wovwlq4sjhodyk` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKekr1dgiqktpyoip3qmp6lxsit` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKsfhn82y57i3k9uxww1s007acc` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- sprints_active_tasks constraints
ALTER TABLE `sprints_active_tasks`
  ADD CONSTRAINT `FKfkmb83ugy72xn1sk487lkljbr` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKm4wqkk1em38y8983rlm6vpjs3` FOREIGN KEY (`sprint_id`) REFERENCES `sprints` (`id`);

-- sprint_changes constraints
ALTER TABLE `sprint_changes`
  ADD CONSTRAINT `FKc18hhtxltwhet54fm99ts8tok` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKkve1jbrh4ojjqeji7rb5ei74` FOREIGN KEY (`sprint_id`) REFERENCES `sprints` (`id`),
  ADD CONSTRAINT `FKr8lyp68airaqemivd5rxysie9` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- task_changes constraints
ALTER TABLE `task_changes`
  ADD CONSTRAINT `FK30q3x8cqbvq3aht17qge3gqf0` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKikakdb0pioe4pxplfnpb6xqjw` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

-- comments constraints
ALTER TABLE `comments`
  ADD CONSTRAINT `FKi7pp0331nbiwd2844kg78kfwb` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKn2na60ukhs76ibtpt9burkm27` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- points_reviews constraints
ALTER TABLE `points_reviews`
  ADD CONSTRAINT `FK2dii35jl989jtdyj0di6urdtc` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKc9tf6shy68ifxogtk9ag0iuy8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- work_logs constraints
ALTER TABLE `work_logs`
  ADD CONSTRAINT `FKk09kchrte8yopfx4jkkl0e8j2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKnr36yywrumho6jtiwxw25swb9` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

-- pull_requests constraints
ALTER TABLE `pull_requests`
  ADD CONSTRAINT `FKa8oqol0kr7gh7ut27byc1wywb` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- tasks_pull_requests constraints
ALTER TABLE `tasks_pull_requests`
  ADD CONSTRAINT `FK12y3efvrepjiu5u4bxnmyfiik` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKkkhm4xhv8iegsi0vnfkewg1vf` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`);

-- pull_request_changes constraints
ALTER TABLE `pull_request_changes`
  ADD CONSTRAINT `FKoyd3feeyemehcnrrbtx991iu7` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`),
  ADD CONSTRAINT `FKs9u9scgn4lmela0l93oauo988` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- pr_notes constraints
ALTER TABLE `pr_notes`
  ADD CONSTRAINT `FK5rik8ir3ef26s7cim8b9v12dh` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`),
  ADD CONSTRAINT `FK8nhlsjg4wo7ctk1kjkf2a5rly` FOREIGN KEY (`subject_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKi2m0sddqck59d1etbdtlq1o1a` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- reports constraints
ALTER TABLE `reports`
  ADD CONSTRAINT `FKkfeng1245vlaismuh0apsgvio` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  ADD CONSTRAINT `FKkv9jna7htxa2xcpsr2aih1hqc` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

-- activities constraints
ALTER TABLE `activities`
  ADD CONSTRAINT `FKl2c4tfjpf89kfw8pd3b4msbv3` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKmfjrc8jdvy0yrr7x67qmrxkue` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKsp1gle1x16hi1viq0vjx26hmf` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);
