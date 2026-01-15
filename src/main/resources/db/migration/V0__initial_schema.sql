-- --------------------------------------------------------
-- Trackdev Initial Schema - Part 1: Table Creation
-- All tables without foreign key constraints
-- --------------------------------------------------------

-- Table: comments
CREATE TABLE IF NOT EXISTS `comments` (
  `date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn2na60ukhs76ibtpt9burkm27` (`author_id`),
  KEY `FKi7pp0331nbiwd2844kg78kfwb` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: courses
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: courses_students
CREATE TABLE IF NOT EXISTS `courses_students` (
  `course_id` bigint NOT NULL,
  `student_id` varchar(36) NOT NULL,
  PRIMARY KEY (`course_id`,`student_id`),
  KEY `FK9u2re1bdq1hmsyds5rfoliq35` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: course_invites
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
  `status` enum('ACCEPTED','CANCELLED','EXPIRED','PENDING') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpb2xa5qbqpk8qvhlny59dkrq9` (`token`),
  KEY `FK3i71gu2e7a351pqjkiht51cea` (`accepted_by_id`),
  KEY `FKtm14w3jgoj6ak131ptn269b59` (`course_id`),
  KEY `FKp3dpxm1xi8ny8gdiivmwrrjk3` (`invited_by_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: emails
CREATE TABLE IF NOT EXISTS `emails` (
  `timestamp` datetime(6) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `destination` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: github_repos
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: github_users_info
CREATE TABLE IF NOT EXISTS `github_users_info` (
  `id` varchar(36) NOT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `github_token` varchar(255) DEFAULT NULL,
  `html_url` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: points_reviews
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

-- Table: projects
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: projects_members
CREATE TABLE IF NOT EXISTS `projects_members` (
  `project_id` bigint NOT NULL,
  `user_id` varchar(36) NOT NULL,
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `FK1aaecn2fdeir463r9ppdsje67` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: pr_notes
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

-- Table: pull_requests
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

-- Table: pull_request_changes
CREATE TABLE IF NOT EXISTS `pull_request_changes` (
  `merged` bit(1) DEFAULT NULL,
  `pr_number` int DEFAULT NULL,
  `changed_at` timestamp NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(31) NOT NULL,
  `pull_request_id` varchar(36) DEFAULT NULL,
  `github_user` varchar(100) DEFAULT NULL,
  `merged_by` varchar(100) DEFAULT NULL,
  `repo_full_name` varchar(200) DEFAULT NULL,
  `pr_title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: role
CREATE TABLE IF NOT EXISTS `role` (
  `user_type` tinyint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkl1an1fehf1xcgpo3wrrbojbh` (`user_type`),
  CONSTRAINT `role_chk_1` CHECK ((`user_type` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: sprints
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: sprints_active_tasks
CREATE TABLE IF NOT EXISTS `sprints_active_tasks` (
  `sprint_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  KEY `FKfkmb83ugy72xn1sk487lkljbr` (`task_id`),
  KEY `FKm4wqkk1em38y8983rlm6vpjs3` (`sprint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: sprint_changes
CREATE TABLE IF NOT EXISTS `sprint_changes` (
  `status` tinyint DEFAULT NULL,
  `changed_at` timestamp NULL DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `entityid` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `start_date` datetime(6) DEFAULT NULL,
  `task_id` bigint DEFAULT NULL,
  `type` varchar(31) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc18hhtxltwhet54fm99ts8tok` (`task_id`),
  CONSTRAINT `sprint_changes_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: sprint_patterns
CREATE TABLE IF NOT EXISTS `sprint_patterns` (
  `course_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8mqggoll94kxo6kgkil004ejo` (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: sprint_pattern_items
CREATE TABLE IF NOT EXISTS `sprint_pattern_items` (
  `order_index` int DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sprint_pattern_id` bigint DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7m06m912ideqrimd92m7jcqye` (`sprint_pattern_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: subjects
CREATE TABLE IF NOT EXISTS `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` varchar(36) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `acronym` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjrjxaoibkgf8f8wtdy2y1dbb8` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: tasks
CREATE TABLE IF NOT EXISTS `tasks` (
  `estimation_points` int DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: tasks_pull_requests
CREATE TABLE IF NOT EXISTS `tasks_pull_requests` (
  `task_id` bigint NOT NULL,
  `pull_request_id` varchar(36) NOT NULL,
  PRIMARY KEY (`task_id`,`pull_request_id`),
  KEY `FKkkhm4xhv8iegsi0vnfkewg1vf` (`pull_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: task_changes
CREATE TABLE IF NOT EXISTS `task_changes` (
  `changed_at` timestamp NULL DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `entityid` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(31) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `new_value` varchar(255) DEFAULT NULL,
  `old_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: users
CREATE TABLE IF NOT EXISTS `users` (
  `capital_letters` varchar(2) DEFAULT NULL,
  `change_password` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `current_project` bigint DEFAULT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `github_info_id` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(128) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `recovery_code` varchar(255) DEFAULT NULL,
  `random` varbinary(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKkbpq45a8lit3ih8226owpcx52` (`github_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: users_roles
CREATE TABLE IF NOT EXISTS `users_roles` (
  `role_id` bigint NOT NULL,
  `user_id` varchar(36) NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `FK2o0jvgh89lemvvo17cbqvdxaa` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: work_logs
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

-- --------------------------------------------------------
-- Trackdev Initial Schema - Part 2: Foreign Key Constraints
-- All foreign key constraints to be applied after tables exist
-- --------------------------------------------------------

-- Constraints for comments
ALTER TABLE `comments`
  ADD CONSTRAINT `FKi7pp0331nbiwd2844kg78kfwb` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKn2na60ukhs76ibtpt9burkm27` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- Constraints for courses
ALTER TABLE `courses`
  ADD CONSTRAINT `FK3jkpttw97v8c2yuqx4prsm4m9` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FK5tckdihu5akp5nkxiacx1gfhi` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`);

-- Constraints for courses_students
ALTER TABLE `courses_students`
  ADD CONSTRAINT `FK9u2re1bdq1hmsyds5rfoliq35` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKcj1bvqj437mdtgllmwcd41f2u` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- Constraints for course_invites
ALTER TABLE `course_invites`
  ADD CONSTRAINT `FK3i71gu2e7a351pqjkiht51cea` FOREIGN KEY (`accepted_by_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKp3dpxm1xi8ny8gdiivmwrrjk3` FOREIGN KEY (`invited_by_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKtm14w3jgoj6ak131ptn269b59` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- Constraints for github_repos
ALTER TABLE `github_repos`
  ADD CONSTRAINT `FK9sxogdop2jjndemkqoib00n03` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- Constraints for points_reviews
ALTER TABLE `points_reviews`
  ADD CONSTRAINT `FK2dii35jl989jtdyj0di6urdtc` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKc9tf6shy68ifxogtk9ag0iuy8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- Constraints for projects
ALTER TABLE `projects`
  ADD CONSTRAINT `FKpefksuunh3nqudnug39tn30rh` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- Constraints for projects_members
ALTER TABLE `projects_members`
  ADD CONSTRAINT `FK1aaecn2fdeir463r9ppdsje67` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKkv2pepk8xy0u74k3l61if6efq` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- Constraints for pr_notes
ALTER TABLE `pr_notes`
  ADD CONSTRAINT `FK5rik8ir3ef26s7cim8b9v12dh` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`),
  ADD CONSTRAINT `FK8nhlsjg4wo7ctk1kjkf2a5rly` FOREIGN KEY (`subject_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKi2m0sddqck59d1etbdtlq1o1a` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- Constraints for pull_requests
ALTER TABLE `pull_requests`
  ADD CONSTRAINT `FKa8oqol0kr7gh7ut27byc1wywb` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

-- Constraints for sprints
ALTER TABLE `sprints`
  ADD CONSTRAINT `FKke5a9e380ibc0xugykeqaktp4` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- Constraints for sprints_active_tasks
ALTER TABLE `sprints_active_tasks`
  ADD CONSTRAINT `FKfkmb83ugy72xn1sk487lkljbr` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKm4wqkk1em38y8983rlm6vpjs3` FOREIGN KEY (`sprint_id`) REFERENCES `sprints` (`id`);

-- Constraints for sprint_changes
ALTER TABLE `sprint_changes`
  ADD CONSTRAINT `FKc18hhtxltwhet54fm99ts8tok` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);

-- Constraints for sprint_patterns
ALTER TABLE `sprint_patterns`
  ADD CONSTRAINT `FK8mqggoll94kxo6kgkil004ejo` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`);

-- Constraints for sprint_pattern_items
ALTER TABLE `sprint_pattern_items`
  ADD CONSTRAINT `FK7m06m912ideqrimd92m7jcqye` FOREIGN KEY (`sprint_pattern_id`) REFERENCES `sprint_patterns` (`id`);

-- Constraints for subjects
ALTER TABLE `subjects`
  ADD CONSTRAINT `FKjrjxaoibkgf8f8wtdy2y1dbb8` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

-- Constraints for tasks (includes self-referencing FK)
ALTER TABLE `tasks`
  ADD CONSTRAINT `FK76tiq4q248au3u79a8nkexoth` FOREIGN KEY (`parent_task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKbvjdsa9y725wovwlq4sjhodyk` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKekr1dgiqktpyoip3qmp6lxsit` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKsfhn82y57i3k9uxww1s007acc` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`);

-- Constraints for tasks_pull_requests
ALTER TABLE `tasks_pull_requests`
  ADD CONSTRAINT `FK12y3efvrepjiu5u4bxnmyfiik` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  ADD CONSTRAINT `FKkkhm4xhv8iegsi0vnfkewg1vf` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`);

-- Constraints for users
ALTER TABLE `users`
  ADD CONSTRAINT `FK30gxix6jj5jq40rqgeefs4ovs` FOREIGN KEY (`github_info_id`) REFERENCES `github_users_info` (`id`);

-- Constraints for users_roles
ALTER TABLE `users_roles`
  ADD CONSTRAINT `FK2o0jvgh89lemvvo17cbqvdxaa` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKt4v0rrweyk393bdgt107vdx0x` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`);

-- Constraints for work_logs
ALTER TABLE `work_logs`
  ADD CONSTRAINT `FKk09kchrte8yopfx4jkkl0e8j2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKnr36yywrumho6jtiwxw25swb9` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);
