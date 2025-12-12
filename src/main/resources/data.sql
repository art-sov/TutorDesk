INSERT INTO students (first_name, last_name, knowledge_level, country, phone_number, global_goal, age, price_individual, price_group, currency, active, created_at, updated_at) VALUES
('John', 'Doe', 'B2', 'USA', '+1-202-555-0177', 'Pass the TOEFL exam for university admission', 22, 25.00, 20.00, 'USD', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Jane', 'Smith', 'C1', 'UK', '+44-20-7946-0958', 'Improve business English for career advancement', 29, 30.00, 24.00, 'PLN', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Carlos', 'Ruiz', 'A2', 'Spain', '+34-91-555-0123', 'Learn basic conversation skills for upcoming travel', 35, 20.00, 16.00, 'EUR', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Hanna', 'Petrenko', 'B1', 'Ukraine', '+7-495-555-0182', 'Be able to communicate with foreign colleagues', 26, 22.00, 18.00, 'UAH', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Ihor', 'Karpenko', 'B1', 'Ukraine', '+8-067-343-01-82', 'Be able to communicate with foreign colleagues', 26, 22.00, 18.00, 'UAH', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Mohammed', 'Ali', 'C2', 'Egypt', '+20-2-555-0145', 'Achieve near-native fluency for a translation job', 31, 28.00, 22.00, 'USD', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

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

-- Inserting into lesson_student (lesson_id, student_id, payment_status, price, currency) VALUES
-- Distribution: ~50% PAID, ~25% PARTIALLY_PAID, ~25% UNPAID
-- All students (IDs 1-6) are used, 2 students per lesson. All prices are group prices.
INSERT INTO lesson_student (lesson_id, student_id, payment_status, price, currency) VALUES
-- Lessons 1-8: PAID (16 entries)
(1, 1, 'PAID', 20.00, 'USD'), (1, 2, 'PAID', 24.00, 'PLN'),
(2, 3, 'PAID', 16.00, 'EUR'), (2, 4, 'PAID', 18.00, 'UAH'),
(3, 5, 'PAID', 18.00, 'UAH'), (3, 6, 'PAID', 22.00, 'USD'),
(4, 2, 'PAID', 24.00, 'PLN'), (4, 3, 'PAID', 16.00, 'EUR'),
(5, 4, 'PAID', 18.00, 'UAH'), (5, 5, 'PAID', 18.00, 'UAH'),
(6, 1, 'PAID', 20.00, 'USD'), (6, 2, 'PAID', 24.00, 'PLN'),
(7, 3, 'PAID', 16.00, 'EUR'), (7, 4, 'PAID', 18.00, 'UAH'),
(8, 5, 'PAID', 18.00, 'UAH'), (8, 6, 'PAID', 22.00, 'USD'),
-- Lessons 9-11: PARTIALLY_PAID (6 entries)
(9, 2, 'PAID', 24.00, 'PLN'), (9, 3, 'FREE', 16.00, 'EUR'),
(10, 4, 'FREE', 18.00, 'UAH'), (10, 5, 'FREE', 18.00, 'UAH'),
(11, 1, 'UNPAID', 20.00, 'USD'), (11, 2, 'FREE', 24.00, 'PLN'),
-- Lessons 12-15: UNPAID (8 entries)
(12, 3, 'PAID', 16.00, 'EUR'), (12, 4, 'UNPAID', 18.00, 'UAH'),
(13, 5, 'UNPAID', 18.00, 'UAH'), (13, 6, 'PAID', 22.00, 'USD'),
(14, 2, 'UNPAID', 24.00, 'PLN'), (14, 3, 'UNPAID', 16.00, 'EUR'),
(15, 4, 'UNPAID', 18.00, 'UAH'), (15, 5, 'UNPAID', 18.00, 'UAH');

-- Inserting payments (for 4 out of 5 students)
INSERT INTO payments (payment_date, student_id, payment_method, amount, currency) VALUES
(CURRENT_DATE(), 1, 'CARD', 100.00, 'EUR'), -- John Doe
(CURRENT_DATE(), 2, 'CASH', 150.00, 'EUR'), -- Jane Smith
(CURRENT_DATE(), 3, 'CARD', 40.00, 'EUR'),  -- Carlos Ruiz
(CURRENT_DATE(), 4, 'PAYPAL', 30.00, 'EUR');   -- Hanna Petrenko

-- Inserting initial student_balance records
INSERT INTO student_balance (student_id, amount, currency, last_updated_at) VALUES
(1, -25.00, 'USD', CURRENT_TIMESTAMP()), -- John Doe
(2, -30.00, 'PLN', CURRENT_TIMESTAMP()), -- Jane Smith
(3, 20.00, 'EUR', CURRENT_TIMESTAMP()),   -- Carlos Ruiz
(4, -44.00, 'UAH', CURRENT_TIMESTAMP()),  -- Hanna Petrenko
(5, -44.00, 'UAH', CURRENT_TIMESTAMP()),  -- Ihor Karpenko
(6, 0.00, 'USD', CURRENT_TIMESTAMP());   -- Mohammed Ali

