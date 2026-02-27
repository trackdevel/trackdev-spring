-- Schema migration V12: LIST attribute support for Pull Requests
-- Adds pull_request_attribute_list_values table (mirrors student_attribute_list_values)

CREATE TABLE `pull_request_attribute_list_values` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`pull_request_id` varchar(36),
	`attribute_id` bigint,
	`order_index` int NOT NULL,
	`enum_value` varchar(100),
	`title` varchar(255),
	`description` text,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK_pralv_pr_attr_order` (`pull_request_id`, `attribute_id`, `order_index`),
	KEY `FK_pralv_attribute` (`attribute_id`)
) ENGINE InnoDB,
  CHARSET utf8mb4,
  COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `pull_request_attribute_list_values` ADD CONSTRAINT `FK_pralv_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `profile_attributes` (`id`);
ALTER TABLE `pull_request_attribute_list_values` ADD CONSTRAINT `FK_pralv_pr` FOREIGN KEY (`pull_request_id`) REFERENCES `pull_requests` (`id`);
