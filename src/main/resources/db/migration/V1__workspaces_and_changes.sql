CREATE TABLE IF NOT EXISTS `workspaces` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`name` varchar(100),
	PRIMARY KEY (`id`)
) ENGINE InnoDB,
  AUTO_INCREMENT 3,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `role` DROP CHECK `role_chk_1`, ADD CONSTRAINT `role_chk_1` CHECK (`user_type` BETWEEN 0 AND 3);
ALTER TABLE `users` ADD COLUMN `workspace_id` bigint AFTER `last_login`, ADD KEY `FK1hj24ju99ddbjpnv66ycum2k6` (`workspace_id`), ADD CONSTRAINT `FK1hj24ju99ddbjpnv66ycum2k6` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`);
ALTER TABLE `subjects` ADD COLUMN `workspace_id` bigint AFTER `id`, ADD KEY `FK1out12irmomck9xdvbomo1uqt` (`workspace_id`), ADD CONSTRAINT `FK1out12irmomck9xdvbomo1uqt` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`);
ALTER TABLE `pull_request_changes` ADD COLUMN `author_id` varchar(36) AFTER `type`, ADD KEY `FKs9u9scgn4lmela0l93oauo988` (`author_id`), ADD KEY `FKoyd3feeyemehcnrrbtx991iu7` (`pull_request_id`), ADD CONSTRAINT `FKoyd3feeyemehcnrrbtx991iu7` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`), ADD CONSTRAINT `FKs9u9scgn4lmela0l93oauo988` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);
ALTER TABLE `sprint_changes` DROP COLUMN `entity_id`, DROP COLUMN `entityid`, DROP COLUMN `author`, ADD COLUMN `sprint_id` bigint AFTER `id`, ADD COLUMN `author_id` varchar(36) AFTER `type`, ADD KEY `FKr8lyp68airaqemivd5rxysie9` (`author_id`), ADD KEY `FKkve1jbrh4ojjqeji7rb5ei74` (`sprint_id`), ADD CONSTRAINT `FKkve1jbrh4ojjqeji7rb5ei74` FOREIGN KEY (`sprint_id`) REFERENCES `sprints` (`id`), ADD CONSTRAINT `FKr8lyp68airaqemivd5rxysie9` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);
ALTER TABLE `task_changes` DROP COLUMN `entity_id`, DROP COLUMN `entityid`, DROP COLUMN `author`, ADD COLUMN `task_id` bigint AFTER `id`, ADD COLUMN `author_id` varchar(36) AFTER `type`, ADD KEY `FK30q3x8cqbvq3aht17qge3gqf0` (`author_id`), ADD KEY `FKikakdb0pioe4pxplfnpb6xqjw` (`task_id`), ADD CONSTRAINT `FK30q3x8cqbvq3aht17qge3gqf0` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`), ADD CONSTRAINT `FKikakdb0pioe4pxplfnpb6xqjw` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`);
