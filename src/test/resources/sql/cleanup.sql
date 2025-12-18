-- Disabling constraints to allow for deletion in any order, then re-enabling.
-- This is often simpler than managing deletion order for complex schemas.
SET REFERENTIAL_INTEGRITY FALSE;

DELETE FROM lesson_student;
DELETE FROM payments;
DELETE FROM student_balance;
DELETE FROM lessons;
DELETE FROM students;

-- It's good practice to reset the sequences so that IDs start from 1 for each test run
ALTER TABLE lesson_student ALTER COLUMN id RESTART WITH 1;
ALTER TABLE payments ALTER COLUMN id RESTART WITH 1;
ALTER TABLE student_balance ALTER COLUMN id RESTART WITH 1;
ALTER TABLE lessons ALTER COLUMN id RESTART WITH 1;
ALTER TABLE students ALTER COLUMN id RESTART WITH 1;

SET REFERENTIAL_INTEGRITY TRUE;
