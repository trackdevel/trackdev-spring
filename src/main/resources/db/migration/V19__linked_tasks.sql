CREATE TABLE tasks_linked_tasks (
    task_id BIGINT NOT NULL,
    linked_task_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, linked_task_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (linked_task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
