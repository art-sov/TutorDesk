package com.art.tutordesk.lesson;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentStatusUtil {

    /**
     * Calculates payment statuses for a set of lesson-student associations based on a list of payments.
     * Logic:
     * 1. Groups lessons and payments by currency.
     * 2. For each currency, calculates the total amount paid.
     * 3. Sorts "chargeable" lessons (COMPLETED, NOT_ATTENDED) chronologically.
     * 4. Marks lessons as PAID if the remaining balance covers the price. 0.00 price is FREE.
     */
    public Map<Long, PaymentStatus> calculatePaymentStatuses(List<LessonStudent> lessons, List<Payment> payments) {
        if (CollectionUtils.isEmpty(lessons)) {
            return Map.of();
        }

        Map<Long, PaymentStatus> result = new HashMap<>();

        // Group payments by currency and calculate total paid for each
        Map<Currency, BigDecimal> totalPaidByCurrency = payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getCurrency,
                        Collectors.mapping(Payment::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Group lessons by currency
        Map<Currency, List<LessonStudent>> lessonsByCurrency = lessons.stream()
                .collect(Collectors.groupingBy(LessonStudent::getCurrency));

        for (Map.Entry<Currency, List<LessonStudent>> entry : lessonsByCurrency.entrySet()) {
            Currency currency = entry.getKey();
            BigDecimal remainingBalance = totalPaidByCurrency.getOrDefault(currency, BigDecimal.ZERO);

            List<LessonStudent> sortedLessons = entry.getValue().stream()
                    .sorted(Comparator.comparing((LessonStudent ls) -> ls.getLesson().getLessonDate())
                            .thenComparing(LessonStudent::getId))
                    .toList();

            for (LessonStudent ls : sortedLessons) {
                // Only COMPLETED or NOT_ATTENDED lessons are chargeable
                if (ls.getStatus() != LessonStudentStatus.COMPLETED && ls.getStatus() != LessonStudentStatus.NOT_ATTENDED) {
                    // For reporting, we might want to mark SCHEDULED/CANCELED lessons as FREE or UNKNOWN,
                    // but according to the business logic, they don't affect balance.
                    // Let's mark them as FREE if price is 0, or just skip them if they shouldn't be in the report.
                    // The user asked for "lessons that are subject to payment".
                    continue;
                }

                BigDecimal price = ls.getPrice();
                if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
                    result.put(ls.getId(), PaymentStatus.FREE);
                } else if (remainingBalance.compareTo(price) >= 0) {
                    remainingBalance = remainingBalance.subtract(price);
                    result.put(ls.getId(), PaymentStatus.PAID);
                } else {
                    result.put(ls.getId(), PaymentStatus.UNPAID);
                }
            }
        }

        return result;
    }

    /**
     * Calculates the overall payment status of a lesson based on individual student payment statuses.
     */
    public PaymentStatus calculateOverallLessonPaymentStatus(Collection<PaymentStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return PaymentStatus.UNPAID;
        }

        long paidCount = statuses.stream().filter(s -> s == PaymentStatus.PAID).count();
        long unpaidCount = statuses.stream().filter(s -> s == PaymentStatus.UNPAID).count();
        long freeCount = statuses.stream().filter(s -> s == PaymentStatus.FREE).count();
        int total = statuses.size();

        // 1. If all FREE, then overall is Free
        if (freeCount == total) {
            return PaymentStatus.FREE;
        }

        // 2. If all PAID or FREE (and not all FREE), then overall is Paid
        if (unpaidCount == 0) {
            return PaymentStatus.PAID;
        }

        // 3. If all UNPAID or mixture of UNPAID and FREE, then overall is Unpaid
        if (paidCount == 0) {
            return PaymentStatus.UNPAID;
        }

        // 4. If there is PAID and at least one UNPAID, then overall is Partial Paid
        return PaymentStatus.PARTIALLY_PAID;
    }

}
