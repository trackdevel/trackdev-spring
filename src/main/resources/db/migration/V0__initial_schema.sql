-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.4.4 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.12.0.7122
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Dumping structure for table trackdev.comments
CREATE TABLE IF NOT EXISTS `comments` (
  `date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `taskId` bigint DEFAULT NULL,
  `authorId` varchar(36) DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh5st5bkq2g2sspr2kdf5dl7j5` (`authorId`),
  KEY `FKt3w846972daa8c2htbdy2g4a2` (`taskId`),
  CONSTRAINT `FKh5st5bkq2g2sspr2kdf5dl7j5` FOREIGN KEY (`authorId`) REFERENCES `users` (`id`),
  CONSTRAINT `FKt3w846972daa8c2htbdy2g4a2` FOREIGN KEY (`taskId`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.courses
CREATE TABLE IF NOT EXISTS `courses` (
  `startYear` int DEFAULT NULL,
  `language` varchar(5) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint DEFAULT NULL,
  `ownerId` varchar(36) DEFAULT NULL,
  `githubOrganization` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK99ycw96efvoqgi2wl53vcw9gk` (`ownerId`),
  KEY `FK5tckdihu5akp5nkxiacx1gfhi` (`subject_id`),
  CONSTRAINT `FK5tckdihu5akp5nkxiacx1gfhi` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `FK99ycw96efvoqgi2wl53vcw9gk` FOREIGN KEY (`ownerId`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.course_invites
CREATE TABLE IF NOT EXISTS `course_invites` (
  `acceptedAt` datetime(6) DEFAULT NULL,
  `courseId` bigint DEFAULT NULL,
  `createdAt` datetime(6) NOT NULL,
  `expiresAt` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `acceptedBy` varchar(36) DEFAULT NULL,
  `invitedBy` varchar(36) DEFAULT NULL,
  `token` varchar(64) NOT NULL,
  `email` varchar(128) NOT NULL,
  `status` enum('ACCEPTED','CANCELLED','EXPIRED','PENDING') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpb2xa5qbqpk8qvhlny59dkrq9` (`token`),
  KEY `FKtkchrxuo69nyn1vs66cwgl662` (`acceptedBy`),
  KEY `FK5s29kw5n6dmbt4eq240o6os9b` (`courseId`),
  KEY `FKbu73653na07nf65lxtum6705n` (`invitedBy`),
  CONSTRAINT `FK5s29kw5n6dmbt4eq240o6os9b` FOREIGN KEY (`courseId`) REFERENCES `courses` (`id`),
  CONSTRAINT `FKbu73653na07nf65lxtum6705n` FOREIGN KEY (`invitedBy`) REFERENCES `users` (`id`),
  CONSTRAINT `FKtkchrxuo69nyn1vs66cwgl662` FOREIGN KEY (`acceptedBy`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.course_students
CREATE TABLE IF NOT EXISTS `course_students` (
  `course_id` bigint NOT NULL,
  `student_id` varchar(36) NOT NULL,
  PRIMARY KEY (`course_id`,`student_id`),
  KEY `FKcedy62b1kx0ll1ggwkh25ubxh` (`student_id`),
  CONSTRAINT `FKcedy62b1kx0ll1ggwkh25ubxh` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj5fbpmgy0y0es0gvk0311jor3` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.emails
CREATE TABLE IF NOT EXISTS `emails` (
  `timestamp` datetime(6) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `destination` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.flyway_schema_history
CREATE TABLE IF NOT EXISTS `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.github_repos
CREATE TABLE IF NOT EXISTS `github_repos` (
  `webhookActive` bit(1) NOT NULL,
  `createdAt` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lastSyncAt` datetime(6) DEFAULT NULL,
  `projectId` bigint DEFAULT NULL,
  `webhookId` bigint DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `owner` varchar(200) DEFAULT NULL,
  `repoName` varchar(200) DEFAULT NULL,
  `accessToken` varchar(500) DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `webhookSecret` varchar(500) DEFAULT NULL,
  `webhookUrl` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm35tdidl5hj60g89iek30kdpy` (`projectId`),
  CONSTRAINT `FKm35tdidl5hj60g89iek30kdpy` FOREIGN KEY (`projectId`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.github_users_info
CREATE TABLE IF NOT EXISTS `github_users_info` (
  `id` varchar(36) NOT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `github_token` varchar(255) DEFAULT NULL,
  `html_url` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.points_reviews
CREATE TABLE IF NOT EXISTS `points_reviews` (
  `points` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2dii35jl989jtdyj0di6urdtc` (`task_id`),
  KEY `FKc9tf6shy68ifxogtk9ag0iuy8` (`user_id`),
  CONSTRAINT `FK2dii35jl989jtdyj0di6urdtc` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FKc9tf6shy68ifxogtk9ag0iuy8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `points_reviews_chk_1` CHECK ((`points` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.projects
CREATE TABLE IF NOT EXISTS `projects` (
  `nextTaskNumber` int DEFAULT NULL,
  `qualification` double DEFAULT NULL,
  `courseId` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcxqk67qijm09gpgig8a997mb0` (`slug`),
  KEY `FK2ye5s75ppfyno3viij85danu8` (`courseId`),
  CONSTRAINT `FK2ye5s75ppfyno3viij85danu8` FOREIGN KEY (`courseId`) REFERENCES `courses` (`id`),
  CONSTRAINT `projects_chk_1` CHECK ((`qualification` <= 10))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.projects_members
CREATE TABLE IF NOT EXISTS `projects_members` (
  `projects_id` bigint NOT NULL,
  `members_id` varchar(36) NOT NULL,
  PRIMARY KEY (`projects_id`,`members_id`),
  KEY `FK6ebvqqi6hao0mn5yqjkjqqrwl` (`members_id`),
  CONSTRAINT `FK2d29ofunhi7r0y87h18hjptfa` FOREIGN KEY (`projects_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `FK6ebvqqi6hao0mn5yqjkjqqrwl` FOREIGN KEY (`members_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.pr_notes
CREATE TABLE IF NOT EXISTS `pr_notes` (
  `level` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author_id` varchar(36) DEFAULT NULL,
  `pullRequest_id` varchar(36) DEFAULT NULL,
  `subject_id` varchar(36) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi2m0sddqck59d1etbdtlq1o1a` (`author_id`),
  KEY `FKpe45wbisjngwgpp1a87ypmpnr` (`pullRequest_id`),
  KEY `FK8nhlsjg4wo7ctk1kjkf2a5rly` (`subject_id`),
  CONSTRAINT `FK8nhlsjg4wo7ctk1kjkf2a5rly` FOREIGN KEY (`subject_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKi2m0sddqck59d1etbdtlq1o1a` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpe45wbisjngwgpp1a87ypmpnr` FOREIGN KEY (`pullRequest_id`) REFERENCES `pull_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.pull_requests
CREATE TABLE IF NOT EXISTS `pull_requests` (
  `merged` bit(1) DEFAULT NULL,
  `prNumber` int DEFAULT NULL,
  `createdAt` datetime(6) DEFAULT NULL,
  `updatedAt` datetime(6) DEFAULT NULL,
  `state` varchar(20) DEFAULT NULL,
  `nodeId` varchar(32) NOT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `repoFullName` varchar(200) DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa8oqol0kr7gh7ut27byc1wywb` (`author_id`),
  CONSTRAINT `FKa8oqol0kr7gh7ut27byc1wywb` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.pull_request_changes
CREATE TABLE IF NOT EXISTS `pull_request_changes` (
  `merged` bit(1) DEFAULT NULL,
  `prNumber` int DEFAULT NULL,
  `changedAt` timestamp NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(31) NOT NULL,
  `pullRequestId` varchar(36) DEFAULT NULL,
  `githubUser` varchar(100) DEFAULT NULL,
  `mergedBy` varchar(100) DEFAULT NULL,
  `repoFullName` varchar(200) DEFAULT NULL,
  `prTitle` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.role
CREATE TABLE IF NOT EXISTS `role` (
  `userType` tinyint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7hpbvn4q6p2jbr2uauyalhkdq` (`userType`),
  CONSTRAINT `role_chk_1` CHECK ((`userType` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.sprints
CREATE TABLE IF NOT EXISTS `sprints` (
  `status` tinyint DEFAULT NULL,
  `endDate` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `projectId` bigint DEFAULT NULL,
  `startDate` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb0h008f7uquegsu4d05jyf6rn` (`projectId`),
  CONSTRAINT `FKb0h008f7uquegsu4d05jyf6rn` FOREIGN KEY (`projectId`) REFERENCES `projects` (`id`),
  CONSTRAINT `sprints_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.sprints_activetasks
CREATE TABLE IF NOT EXISTS `sprints_activetasks` (
  `activeSprints_id` bigint NOT NULL,
  `activeTasks_id` bigint NOT NULL,
  KEY `FK2hrc8tk62vpn5tdt83eabekrx` (`activeTasks_id`),
  KEY `FKhl16tk2k7n1qeunixs0tt6i3l` (`activeSprints_id`),
  CONSTRAINT `FK2hrc8tk62vpn5tdt83eabekrx` FOREIGN KEY (`activeTasks_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FKhl16tk2k7n1qeunixs0tt6i3l` FOREIGN KEY (`activeSprints_id`) REFERENCES `sprints` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.sprint_changes
CREATE TABLE IF NOT EXISTS `sprint_changes` (
  `status` tinyint DEFAULT NULL,
  `changedAt` timestamp NULL DEFAULT NULL,
  `endDate` datetime(6) DEFAULT NULL,
  `entityId` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `startDate` datetime(6) DEFAULT NULL,
  `task_id` bigint DEFAULT NULL,
  `type` varchar(31) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc18hhtxltwhet54fm99ts8tok` (`task_id`),
  CONSTRAINT `FKc18hhtxltwhet54fm99ts8tok` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `sprint_changes_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.sprint_patterns
CREATE TABLE IF NOT EXISTS `sprint_patterns` (
  `courseId` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb0el78m6hl6p2fmholrk4gf84` (`courseId`),
  CONSTRAINT `FKb0el78m6hl6p2fmholrk4gf84` FOREIGN KEY (`courseId`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.sprint_pattern_items
CREATE TABLE IF NOT EXISTS `sprint_pattern_items` (
  `orderIndex` int DEFAULT NULL,
  `endDate` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sprintPatternId` bigint DEFAULT NULL,
  `startDate` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrl2bfyk3chf4onnt62s2iky44` (`sprintPatternId`),
  CONSTRAINT `FKrl2bfyk3chf4onnt62s2iky44` FOREIGN KEY (`sprintPatternId`) REFERENCES `sprint_patterns` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.subjects
CREATE TABLE IF NOT EXISTS `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ownerId` varchar(36) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `acronym` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfpayf125mxopij143r5xclwgw` (`ownerId`),
  CONSTRAINT `FKfpayf125mxopij143r5xclwgw` FOREIGN KEY (`ownerId`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.tasks
CREATE TABLE IF NOT EXISTS `tasks` (
  `estimationPoints` int DEFAULT NULL,
  `rank` int DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `taskNumber` int DEFAULT NULL,
  `type` tinyint DEFAULT NULL,
  `createdAt` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parentTaskId` bigint DEFAULT NULL,
  `projectId` bigint DEFAULT NULL,
  `taskKey` varchar(10) DEFAULT NULL,
  `assignee_id` varchar(36) DEFAULT NULL,
  `reporter_id` varchar(36) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKha8p7qolvi7cr2reyfm2cpsln` (`taskKey`),
  KEY `FKekr1dgiqktpyoip3qmp6lxsit` (`assignee_id`),
  KEY `FK3toapvqycdlf99yr2fjsbl0gr` (`parentTaskId`),
  KEY `FKqvrx3xl2fyrppj222wn0eh3tx` (`projectId`),
  KEY `FKbvjdsa9y725wovwlq4sjhodyk` (`reporter_id`),
  CONSTRAINT `FK3toapvqycdlf99yr2fjsbl0gr` FOREIGN KEY (`parentTaskId`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FKbvjdsa9y725wovwlq4sjhodyk` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKekr1dgiqktpyoip3qmp6lxsit` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqvrx3xl2fyrppj222wn0eh3tx` FOREIGN KEY (`projectId`) REFERENCES `projects` (`id`),
  CONSTRAINT `tasks_chk_1` CHECK ((`status` between 0 and 5)),
  CONSTRAINT `tasks_chk_2` CHECK ((`type` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.task_changes
CREATE TABLE IF NOT EXISTS `task_changes` (
  `changedAt` timestamp NULL DEFAULT NULL,
  `entityId` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(31) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `newValue` varchar(255) DEFAULT NULL,
  `oldValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.task_pull_requests
CREATE TABLE IF NOT EXISTS `task_pull_requests` (
  `task_id` bigint NOT NULL,
  `pull_request_id` varchar(36) NOT NULL,
  PRIMARY KEY (`task_id`,`pull_request_id`),
  KEY `FKrfmlvsimrjsxnxtia8wpms26c` (`pull_request_id`),
  CONSTRAINT `FKinv4r54e7vv1695yak8373wt4` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FKrfmlvsimrjsxnxtia8wpms26c` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.users
CREATE TABLE IF NOT EXISTS `users` (
  `capitalLetters` varchar(2) DEFAULT NULL,
  `changePassword` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `currentProject` bigint DEFAULT NULL,
  `lastLogin` datetime(6) DEFAULT NULL,
  `githubInfoId` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(128) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `recoveryCode` varchar(255) DEFAULT NULL,
  `random` varbinary(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKrqi41m9hby21b7k4jn4jjg8fl` (`githubInfoId`),
  CONSTRAINT `FKfi3wsm8hjjjhb3ig9tomgnhqb` FOREIGN KEY (`githubInfoId`) REFERENCES `github_users_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.users_roles
CREATE TABLE IF NOT EXISTS `users_roles` (
  `roles_id` bigint NOT NULL,
  `User_id` varchar(36) NOT NULL,
  PRIMARY KEY (`roles_id`,`User_id`),
  KEY `FKe6k7h92pkxjim6t1176b7h95x` (`User_id`),
  CONSTRAINT `FKbeupe7ttpx0k7tf46jfgq2nkf` FOREIGN KEY (`roles_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FKe6k7h92pkxjim6t1176b7h95x` FOREIGN KEY (`User_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table trackdev.work_logs
CREATE TABLE IF NOT EXISTS `work_logs` (
  `timeSeconds` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint DEFAULT NULL,
  `timeStamp` timestamp NULL DEFAULT NULL,
  `author_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk09kchrte8yopfx4jkkl0e8j2` (`author_id`),
  KEY `FKnr36yywrumho6jtiwxw25swb9` (`task_id`),
  CONSTRAINT `FKk09kchrte8yopfx4jkkl0e8j2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKnr36yywrumho6jtiwxw25swb9` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
