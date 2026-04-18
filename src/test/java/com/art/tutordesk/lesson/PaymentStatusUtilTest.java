package com.art.tutordesk.lesson;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentStatusUtilTest {

    private PaymentStatusUtil util;

    @BeforeEach
    void setUp() {
        util = new PaymentStatusUtil();
    }

    @Test
    void calculatePaymentStatuses_EmptyInput() {
        Map<Long, PaymentStatus> result = util.calculatePaymentStatuses(Collections.emptyList(), Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void calculatePaymentStatuses_ComplexScenario() {
        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setLessonDate(LocalDate.of(2025, 1, 1));
        
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setLessonDate(LocalDate.of(2025, 1, 2));

        LessonStudent ls1 = createLessonStudent(101L, lesson1, new BigDecimal("25.00"), LessonStudentStatus.COMPLETED);
        LessonStudent ls2 = createLessonStudent(102L, lesson2, new BigDecimal("25.00"), LessonStudentStatus.COMPLETED);
        LessonStudent lsFree = createLessonStudent(103L, lesson1, BigDecimal.ZERO, LessonStudentStatus.COMPLETED);
        LessonStudent lsScheduled = createLessonStudent(104L, lesson2, new BigDecimal("25.00"), LessonStudentStatus.SCHEDULED);

        Payment p1 = new Payment();
        p1.setAmount(new BigDecimal("30.00"));
        p1.setCurrency(Currency.USD);

        List<LessonStudent> lessons = Arrays.asList(ls1, ls2, lsFree, lsScheduled);
        List<Payment> payments = Collections.singletonList(p1);

        Map<Long, PaymentStatus> result = util.calculatePaymentStatuses(lessons, payments);

        assertEquals(PaymentStatus.PAID, result.get(101L)); // Covered by $30 payment
        assertEquals(PaymentStatus.UNPAID, result.get(102L)); // $5 left is not enough for $25
        assertEquals(PaymentStatus.FREE, result.get(103L)); // Price is 0
        assertFalse(result.containsKey(104L)); // SCHEDULED is skipped
    }

    @Test
    void calculateOverallLessonPaymentStatus_Rules() {
        assertEquals(PaymentStatus.FREE, util.calculateOverallLessonPaymentStatus(List.of(PaymentStatus.FREE, PaymentStatus.FREE)));
        assertEquals(PaymentStatus.PAID, util.calculateOverallLessonPaymentStatus(List.of(PaymentStatus.PAID, PaymentStatus.FREE)));
        assertEquals(PaymentStatus.UNPAID, util.calculateOverallLessonPaymentStatus(List.of(PaymentStatus.UNPAID, PaymentStatus.FREE)));
        assertEquals(PaymentStatus.PARTIALLY_PAID, util.calculateOverallLessonPaymentStatus(List.of(PaymentStatus.PAID, PaymentStatus.UNPAID)));
        assertEquals(PaymentStatus.UNPAID, util.calculateOverallLessonPaymentStatus(Collections.emptyList()));
    }

    private LessonStudent createLessonStudent(Long id, Lesson l, BigDecimal price, LessonStudentStatus status) {
        LessonStudent ls = new LessonStudent();
        ls.setId(id);
        ls.setLesson(l);
        ls.setPrice(price);
        ls.setCurrency(Currency.USD);
        ls.setStatus(status);
        return ls;
    }
}
