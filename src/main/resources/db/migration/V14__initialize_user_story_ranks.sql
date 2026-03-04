-- Initialize rank values for existing USER_STORY tasks.
-- type = 0 is USER_STORY (JPA ordinal for TaskType enum).
-- Assigns ranks per-project ordered by id (creation order) with gap of 65536.

SET @row_number = 0;
SET @current_project = 0;

UPDATE tasks t
INNER JOIN (
    SELECT id,
           project_id,
           @row_number := IF(@current_project = project_id, @row_number + 1, 1) AS row_num,
           @current_project := project_id AS proj
    FROM tasks
    WHERE type = 0 AND parent_task_id IS NULL
    ORDER BY project_id, id
) ranked ON t.id = ranked.id
SET t.`rank` = ranked.row_num * 65536;
