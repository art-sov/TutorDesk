-- Test data for integration tests
INSERT INTO students (id, first_name, last_name, knowledge_level, country, price_individual, price_group, currency, active, created_at, updated_at) VALUES
(1, 'Test', 'Student1', 'B1', 'USA', 25.00, 20.00, 'USD', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(2, 'Test', 'Student2', 'C1', 'UK', 30.00, 24.00, 'EUR', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(3, 'Test', 'StudentFree', 'A1', 'DE', 0.00, 0.00, 'PLN', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(4, 'Test', 'Student4', 'B2', 'USA', 20.00, 18.00, 'USD', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(5, 'Test', 'StudentInactive', 'C2', 'FR', 35.00, 30.00, 'EUR', FALSE, '2025-01-01 10:00:00', '2025-01-01 10:00:00');

INSERT INTO lessons (id, lesson_date, start_time, topic) VALUES
(1, '2025-01-01', '10:00:00', 'Group Lesson A'),
(2, '2025-01-02', '11:00:00', 'Group Lesson B');

INSERT INTO lesson_student (id, lesson_id, student_id, payment_status, price, currency) VALUES
(1, 1, 1, 'UNPAID', 25.00, 'USD'),
(2, 1, 2, 'UNPAID', 30.00, 'EUR'),
(3, 2, 3, 'FREE', 0.00, 'PLN'),
(4, 2, 4, 'UNPAID', 20.00, 'USD');

INSERT INTO payments (id, payment_date, student_id, payment_method, amount, currency) VALUES
(1, '2025-01-01', 1, 'CARD', 10.00, 'USD'),
(2, '2025-01-01', 2, 'CASH', 15.00, 'EUR'),
(3, '2025-01-01', 4, 'PAYPAL', 10.00, 'USD');

INSERT INTO student_balance (id, student_id, amount, currency, last_updated_at) VALUES
(1, 1, -15.00, 'USD', '2025-01-01 10:00:00'), -- 25.00 lesson - 10.00 payment
(2, 2, -15.00, 'EUR', '2025-01-01 10:00:00'), -- 30.00 lesson - 15.00 payment
(3, 3, 0.00, 'PLN', '2025-01-01 10:00:00'),  -- Free student
(4, 4, -10.00, 'USD', '2025-01-01 10:00:00'); -- 20.00 lesson - 10.00 payment
