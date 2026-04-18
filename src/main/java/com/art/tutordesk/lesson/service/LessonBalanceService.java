package com.art.tutordesk.lesson.service;

import com.art.tutordesk.balance.BalanceTransactionService;
import com.art.tutordesk.balance.TransactionSource;
import com.art.tutordesk.balance.TransactionType;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonBalanceService {

    private final BalanceTransactionService balanceTransactionService;

    /**
     * Handles balance adjustments when a student's price or status in a lesson changes.
     * Logic:
     * 1. If the student was already in a chargeable state (COMPLETED/NOT_ATTENDED), and the status
     *    is changing to a non-chargeable state OR the price is changing, reverse the old charge.
     * 2. If the student is moving into a chargeable state (or staying in one with a new price), apply the charge.
     */
    public void adjustBalanceForPriceAndStatusChange(LessonStudent lessonStudent, BigDecimal newPrice, LessonStudentStatus newStatus) {
        LessonStudentStatus oldStatus = lessonStudent.getStatus();
        BigDecimal oldPrice = lessonStudent.getPrice();
        boolean priceChanged = (oldPrice == null) || (oldPrice.compareTo(newPrice) != 0);
        boolean statusChanged = oldStatus != newStatus;

        if (!priceChanged && !statusChanged) {
            return;
        }

        Student student = lessonStudent.getStudent();
        Long lessonStudentId = lessonStudent.getId();
        Currency currency = lessonStudent.getCurrency();

        log.info("Adjusting balance for student {} in lesson {}: status {} -> {}, price {} -> {}",
                student.getId(), lessonStudent.getLesson().getId(), oldStatus, newStatus, oldPrice, newPrice);

        // Case 1: Status was already chargeable
        if (isChargeable(oldStatus)) {
            // If exiting chargeable state OR price changed, we must reverse the old charge
            if (!isChargeable(newStatus) || priceChanged) {
                balanceTransactionService.createBalanceTransaction(student, TransactionType.LESSON_CHARGE_REVERSAL,
                        currency, TransactionSource.LESSON, oldPrice, lessonStudentId);
                log.info("Recorded LESSON_CHARGE_REVERSAL for student {} (old price {})", student.getId(), oldPrice);
            }
            // If staying in chargeable state AND price changed, apply new charge
            if (isChargeable(newStatus) && priceChanged) {
                balanceTransactionService.createBalanceTransaction(student, TransactionType.LESSON_CHARGE,
                        currency, TransactionSource.LESSON, newPrice.negate(), lessonStudentId);
                log.info("Recorded LESSON_CHARGE for student {} with NEW price {}", student.getId(), newPrice);
            }
        }
        // Case 2: Status was NOT chargeable, but now it IS
        else if (isChargeable(newStatus)) {
            balanceTransactionService.createBalanceTransaction(student, TransactionType.LESSON_CHARGE,
                    currency, TransactionSource.LESSON, newPrice.negate(), lessonStudentId);
            log.info("Recorded LESSON_CHARGE for student {} with price {}", student.getId(), newPrice);
        }

        lessonStudent.setPrice(newPrice);
        lessonStudent.setStatus(newStatus);
    }

    public boolean isChargeable(LessonStudentStatus status) {
        return status == LessonStudentStatus.COMPLETED || status == LessonStudentStatus.NOT_ATTENDED;
    }
}
