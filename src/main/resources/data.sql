INSERT INTO students (first_name, last_name, knowledge_level, country, phone_number, global_goal, age, price, currency, active) VALUES
('John', 'Doe', 'B2', 'USA', '+1-202-555-0177', 'Pass the TOEFL exam for university admission', 22, 25.00, 'USD', TRUE),
('Jane', 'Smith', 'C1', 'UK', '+44-20-7946-0958', 'Improve business English for career advancement', 29, 30.00, 'PLN', TRUE),
('Carlos', 'Ruiz', 'A2', 'Spain', '+34-91-555-0123', 'Learn basic conversation skills for upcoming travel', 35, 20.00, 'EUR', TRUE),
('Hanna', 'Petrenko', 'B1', 'Ukraine', '+7-495-555-0182', 'Be able to communicate with foreign colleagues', 26, 22.00, 'UAH', TRUE),
('Ihor', 'Karpenko', 'B1', 'Ukraine', '+8-067-343-01-82', 'Be able to communicate with foreign colleagues', 26, 22.00, 'UAH', FALSE),
('Mohammed', 'Ali', 'C2', 'Egypt', '+20-2-555-0145', 'Achieve near-native fluency for a translation job', 31, 28.00, 'USD', TRUE);

-- Inserting Lessons (dates are relative to CURRENT_DATE)
INSERT INTO lessons (lesson_date, start_time, topic) VALUES
(DATEADD('DAY', -2, CURRENT_DATE()), '10:00:00', 'English Basics - Part 1'),
(DATEADD('DAY', -2, CURRENT_DATE()), '11:30:00', 'Grammar Review - Nouns'),
(DATEADD('DAY', -2, CURRENT_DATE()), '14:00:00', 'Conversation Practice A'),
(DATEADD('DAY', -1, CURRENT_DATE()), '10:00:00', 'English Basics - Part 2'),
(DATEADD('DAY', -1, CURRENT_DATE()), '11:30:00', 'Grammar Review - Verbs'),
(DATEADD('DAY', -1, CURRENT_DATE()), '14:00:00', 'Conversation Practice B'),
(CURRENT_DATE(), '10:00:00', 'Current Events Discussion'),
(CURRENT_DATE(), '11:30:00', 'Pronunciation Workshop'),
(CURRENT_DATE(), '14:00:00', 'Business English Intro'),
(DATEADD('DAY', 1, CURRENT_DATE()), '10:00:00', 'Future Tenses Deep Dive'),
(DATEADD('DAY', 1, CURRENT_DATE()), '11:30:00', 'Idioms and Phrasal Verbs'),
(DATEADD('DAY', 1, CURRENT_DATE()), '14:00:00', 'Writing Skills: Email'),
(DATEADD('DAY', 2, CURRENT_DATE()), '10:00:00', 'Presentation Skills'),
(DATEADD('DAY', 2, CURRENT_DATE()), '11:30:00', 'TOEFL Preparation'),
(DATEADD('DAY', 2, CURRENT_DATE()), '14:00:00', 'General English Practice');

-- Inserting into lesson_student (lesson_id, student_id, payment_status)
-- Distribution: ~50% PAID, ~25% PARTIALLY_PAID, ~25% UNPAID
-- All students (IDs 1-5) are used, 2 students per lesson.
INSERT INTO lesson_student (lesson_id, student_id, payment_status) VALUES
-- Lessons 1-8: PAID (16 entries)
(1, 1, 'PAID'), (1, 2, 'PAID'),
(2, 3, 'PAID'), (2, 4, 'PAID'),
(3, 5, 'PAID'), (3, 1, 'PAID'),
(4, 2, 'PAID'), (4, 3, 'PAID'),
(5, 4, 'PAID'), (5, 5, 'PAID'),
(6, 1, 'PAID'), (6, 2, 'PAID'),
(7, 3, 'PAID'), (7, 4, 'PAID'),
(8, 5, 'PAID'), (8, 1, 'PAID'),
-- Lessons 9-11: PARTIALLY_PAID (6 entries)
(9, 2, 'PAID'), (9, 3, 'FREE'),
(10, 4, 'FREE'), (10, 5, 'FREE'),
(11, 1, 'UNPAID'), (11, 2, 'FREE'),
-- Lessons 12-15: UNPAID (8 entries)
(12, 3, 'PAID'), (12, 4, 'UNPAID'),
(13, 5, 'UNPAID'), (13, 1, 'PAID'),
(14, 2, 'UNPAID'), (14, 3, 'UNPAID'),
(15, 4, 'UNPAID'), (15, 5, 'UNPAID');

-- Inserting payments (for 4 out of 5 students)
INSERT INTO payments (payment_date, student_id, payment_method, amount, currency) VALUES
(CURRENT_DATE(), 1, 'CARD', 100.00, 'EUR'), -- John Doe
(CURRENT_DATE(), 2, 'CASH', 150.00, 'EUR'), -- Jane Smith
(CURRENT_DATE(), 3, 'CARD', 40.00, 'EUR'),  -- Carlos Ruiz
(CURRENT_DATE(), 4, 'PAYPAL', 30.00, 'EUR');   -- Hanna Petrenko

