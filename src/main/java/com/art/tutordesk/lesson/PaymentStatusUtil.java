package com.art.tutordesk.lesson;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Component
public class PaymentStatusUtil {

    public PaymentStatus calculateAndSetLessonPaymentStatus(Set<LessonStudent> lessonStudents) {
        if (CollectionUtils.isEmpty(lessonStudents)) {
            return PaymentStatus.UNPAID;
        }

        int totalStudents = lessonStudents.size();
        long paidCount = 0;
        long unpaidCount = 0;
        long freeCount = 0;

        for (LessonStudent ls : lessonStudents) {
            // NOTE: As per business logic, LessonStudent status can only be PAID, UNPAID, or FREE.
            if (ls.getPaymentStatus() == PaymentStatus.PAID) {
                paidCount++;
            } else if (ls.getPaymentStatus() == PaymentStatus.UNPAID) {
                unpaidCount++;
            } else if (ls.getPaymentStatus() == PaymentStatus.FREE) {
                freeCount++;
            }
        }

        // Rule 1: Lesson is FREE if ALL students are FREE
        if (freeCount == totalStudents) {
            return PaymentStatus.FREE;
        }

        // Rule 2: Lesson is PAID if all students are either PAID or FREE (and not all are FREE)
        if (unpaidCount == 0) {
            return PaymentStatus.PAID;
        }

        // Rule 3: Lesson is UNPAID if all non-FREE students are UNPAID
        if (paidCount == 0) {
            return PaymentStatus.UNPAID;
        }

        // Rule 4: Otherwise, it's a mix of UNPAID and (PAID or FREE), so it's PARTIALLY_PAID
        return PaymentStatus.PARTIALLY_PAID;
    }

}
