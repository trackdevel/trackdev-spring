-- Fix single quotes that were HTML-encoded as &#x27; in task names, descriptions, and comments

UPDATE tasks SET name = REPLACE(name, '&#x27;', '''') WHERE name LIKE '%&#x27;%';
UPDATE tasks SET description = REPLACE(description, '&#x27;', '''') WHERE description LIKE '%&#x27;%';
UPDATE comments SET content = REPLACE(content, '&#x27;', '''') WHERE content LIKE '%&#x27;%';
