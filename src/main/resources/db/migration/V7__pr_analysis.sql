-- ============================================
-- CREATE TABLES (without foreign keys)
-- ============================================

-- No CREATE TABLE statements in this migration


-- ============================================  
-- ADD COLUMNS
-- ============================================

ALTER TABLE `pull_requests` ADD COLUMN `additions` int FIRST;
ALTER TABLE `pull_requests` ADD COLUMN `changed_files` int AFTER `additions`;
ALTER TABLE `pull_requests` ADD COLUMN `deletions` int AFTER `changed_files`;
ALTER TABLE `pull_requests` ADD COLUMN `stats_fetched_at` timestamp NULL AFTER `created_at`;


-- ============================================
-- ADD INDEXES/KEYS
-- ============================================

ALTER TABLE `tasks` DROP CHECK `tasks_chk_1`;
ALTER TABLE `tasks` ADD CONSTRAINT `tasks_chk_1` CHECK (`status` BETWEEN 0 AND 4);


-- ============================================
-- ADD FOREIGN KEY CONSTRAINTS
-- ============================================

-- No foreign key constraints in this migration


-- ============================================
-- DROP STATEMENTS
-- ============================================

-- No standalone drop statements in this migration
