package com.art.tutordesk.lesson;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PaymentStatusUtil {

    public PaymentStatus calculateAndSetLessonPaymentStatus(Set<LessonStudent> lessonStudents) {
        if (CollectionUtils.isEmpty(lessonStudents)) {
            return PaymentStatus.UNPAID;
        }

        Set<LessonStudent> attendingStudents = lessonStudents.stream()
                .filter(ls -> ls.getAttendanceStatus() != AttendanceStatus.ABSENT)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(attendingStudents)) {
            return PaymentStatus.PAID;
        }

        int totalStudents = attendingStudents.size();
        long paidCount = 0;
        long unpaidCount = 0;
        long freeCount = 0;

        for (LessonStudent ls : attendingStudents) {
            // NOTE: As per business logic, LessonStudent status can only be PAID, UNPAID, or FREE.
            if (ls.getPaymentStatus() == PaymentStatus.PAID) {
                paidCount++;
            } else if (ls.getPaymentStatus() == PaymentStatus.UNPAID) {
                unpaidCount++;
            } else if (ls.getPaymentStatus() == PaymentStatus.FREE) {
                freeCount++;
            }
        }

        // Rule 1: Lesson is FREE if ALL attending students are FREE
        if (freeCount == totalStudents) {
            return PaymentStatus.FREE;
        }

        // Rule 2: Lesson is PAID if all attending students are either PAID or FREE (and not all are FREE)
        if (unpaidCount == 0) {
            return PaymentStatus.PAID;
        }

        // Rule 3: Lesson is UNPAID if all non-FREE attending students are UNPAID
        if (paidCount == 0) {
            return PaymentStatus.UNPAID;
        }

        // Rule 4: Otherwise, it's a mix of UNPAID and (PAID or FREE), so it's PARTIALLY_PAID
        return PaymentStatus.PARTIALLY_PAID;
    }

}
