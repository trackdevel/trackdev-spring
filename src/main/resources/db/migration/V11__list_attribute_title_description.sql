-- Split string_value into title + description for list attribute items
-- Title is a short text, description supports Markdown content

ALTER TABLE `student_attribute_list_values`
  CHANGE COLUMN `string_value` `title` varchar(255),
  ADD COLUMN `description` text AFTER `title`;
