package com.art.tutordesk.lesson;

/**
 * Represents the payment status.
 * Note: The PARTIALLY_PAID status is applicable only to the overall Lesson status,
 * not to individual LessonStudent records. A student's payment is either made (PAID) or not (UNPAID).
 */
public enum PaymentStatus {
    PAID,
    PARTIALLY_PAID,
    UNPAID,
    FREE
}
