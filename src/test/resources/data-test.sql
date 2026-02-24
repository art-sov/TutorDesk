-- Test data for integration tests
INSERT INTO students (id, first_name, last_name, knowledge_level, price_individual, price_group, currency, active, created_at, updated_at) VALUES
(1, 'Test', 'Student1', 'B1', 25.00, 20.00, 'USD', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(2, 'Test', 'Student2', 'C1', 30.00, 24.00, 'EUR', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(3, 'Test', 'StudentFree', 'A1', 0.00, 0.00, 'PLN', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(4, 'Test', 'Student4', 'B2', 20.00, 18.00, 'USD', TRUE, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(5, 'Test', 'StudentInactive', 'C2', 35.00, 30.00, 'EUR', FALSE, '2025-01-01 10:00:00', '2025-01-01 10:00:00');

INSERT INTO lessons (id, lesson_date) VALUES
(1, '2025-01-01'),
(2, '2025-01-02'),
(3, '2025-01-10'),
(4, '2025-01-05'),
(5, '2025-02-01');

INSERT INTO lesson_student (id, lesson_id, student_id, price, currency, status) VALUES
(1, 1, 1, 25.00, 'USD', 'SCHEDULED'),   -- Student 1
(2, 1, 2, 30.00, 'EUR', 'SCHEDULED'),   -- Student 2
(3, 2, 3, 0.00, 'PLN', 'SCHEDULED'),     -- Student 3
(4, 2, 4, 20.00, 'USD', 'SCHEDULED'),   -- Student 4
(5, 3, 1, 25.00, 'USD', 'SCHEDULED'),     -- Student 1, future lesson
(6, 4, 1, 30.00, 'EUR', 'SCHEDULED'),    -- Student 1, past lesson, different currency
(7, 5, 1, 20.00, 'USD', 'COMPLETED'),    -- Student 1, completed group lesson
(8, 5, 2, 24.00, 'EUR', 'COMPLETED');    -- Student 2, completed group lesson

INSERT INTO payments (id, payment_date, student_id, payment_method, amount, currency, created_at, updated_at) VALUES
(1, '2024-12-20', 1, 'CARD', 10.00, 'USD', '2025-01-01 10:00:00', '2025-01-01 10:00:00'), -- payment for Student 1
(2, '2025-01-01', 2, 'CASH', 15.00, 'EUR', '2025-01-01 10:00:00', '2025-01-01 10:00:00'), -- For Student 2
(3, '2025-01-05', 4, 'PAYPAL', 10.00, 'USD', '2025-01-01 10:00:00', '2025-01-01 10:00:00'), -- For Student 4
(4, '2025-01-10', 1, 'CARD', 20.00, 'USD', '2025-01-01 10:00:00', '2025-01-01 10:00:00'), -- Newer payment for Student 1
(5, '2025-01-15', 5, 'CASH', 30.00, 'EUR', '2025-01-01 10:00:00', '2025-01-01 10:00:00'), -- Payment for inactive student
(6, '2025-02-01', 2, 'CARD', 25.00, 'EUR', '2025-01-01 10:00:00', '2025-01-01 10:00:00'); -- Future payment for Student 2

INSERT INTO balance_transactions (id, student_id, transaction_datetime, type, amount, currency, source_entity, source_id) VALUES
(1, 1, '2025-01-01 10:00:00', 'PAYMENT_RECEIVED', 10.00, 'USD', 'PAYMENT', 1),
(2, 2, '2025-01-01 10:00:00', 'PAYMENT_RECEIVED', 15.00, 'EUR', 'PAYMENT', 2),
(3, 4, '2025-01-01 10:00:00', 'PAYMENT_RECEIVED', 10.00, 'USD', 'PAYMENT', 3),
(4, 1, '2025-01-01 10:00:00', 'PAYMENT_RECEIVED', 20.00, 'USD', 'PAYMENT', 4),
(5, 5, '2025-01-01 10:00:00', 'PAYMENT_RECEIVED', 30.00, 'EUR', 'PAYMENT', 5),
(6, 2, '2025-01-01 10:00:00', 'PAYMENT_RECEIVED', 25.00, 'EUR', 'PAYMENT', 6),
(7, 1, '2025-02-01 10:00:00', 'LESSON_CHARGE', 20.00, 'USD', 'LESSON', 7),
(8, 2, '2025-02-01 10:00:00', 'LESSON_CHARGE', 24.00, 'EUR', 'LESSON', 8);

ALTER TABLE balance_transactions ALTER COLUMN id RESTART WITH 9;