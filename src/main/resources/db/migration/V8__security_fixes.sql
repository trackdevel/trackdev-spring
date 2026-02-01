-- Schema migration V8
-- Organized for proper execution order
-- Generated with Copilot (Claude Sonnet 4.5)

-- ============================================
-- CREATE TABLES (without foreign keys)
-- ============================================


-- ============================================  
-- ADD COLUMNS
-- ============================================


-- ============================================
-- MODIFY COLUMNS
-- ============================================

ALTER TABLE `github_users_info` MODIFY COLUMN `login` varchar(256) AFTER `id`;
ALTER TABLE `github_users_info` MODIFY COLUMN `github_token` varchar(512) AFTER `login`;
ALTER TABLE `users` MODIFY COLUMN `email` varchar(512) NOT NULL;

-- ============================================
-- ADD INDEXES/KEYS
-- ============================================


-- ============================================
-- ADD FOREIGN KEY CONSTRAINTS
-- ============================================


-- ============================================
-- DROP STATEMENTS
-- ============================================

ALTER TABLE `users` DROP COLUMN `random`;