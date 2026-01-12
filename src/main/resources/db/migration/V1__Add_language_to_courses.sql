-- Add language column to courses table
ALTER TABLE courses
ADD COLUMN language VARCHAR(5) DEFAULT 'en' NOT NULL;
