package com.art.tutordesk.lesson.service;

import com.art.tutordesk.balance.BalanceTransactionService;
import com.art.tutordesk.balance.TransactionSource;
import com.art.tutordesk.balance.TransactionType;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonBalanceServiceTest {

    @Mock
    private BalanceTransactionService balanceTransactionService;

    @InjectMocks
    private LessonBalanceService lessonBalanceService;

    private LessonStudent lessonStudent;
    private Student student;
    private final BigDecimal price20 = new BigDecimal("20.00");
    private final BigDecimal price25 = new BigDecimal("25.00");

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setCurrency(Currency.USD);

        Lesson lesson = new Lesson();
        lesson.setId(10L);

        lessonStudent = new LessonStudent();
        lessonStudent.setId(100L);
        lessonStudent.setStudent(student);
        lessonStudent.setLesson(lesson);
        lessonStudent.setCurrency(Currency.USD);
        lessonStudent.setPrice(price20);
        lessonStudent.setStatus(LessonStudentStatus.SCHEDULED);
    }

    @Test
    void testNoChange_DoesNothing() {
        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price20, LessonStudentStatus.SCHEDULED);

        verifyNoInteractions(balanceTransactionService);
        assertEquals(price20, lessonStudent.getPrice());
        assertEquals(LessonStudentStatus.SCHEDULED, lessonStudent.getStatus());
    }

    @Test
    void testNonChargeableToNonChargeable_OnlyUpdatesFields() {
        // e.g. SCHEDULED -> CANCELED, price change should not trigger transaction
        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price25, LessonStudentStatus.CANCELED);

        verifyNoInteractions(balanceTransactionService);
        assertEquals(price25, lessonStudent.getPrice());
        assertEquals(LessonStudentStatus.CANCELED, lessonStudent.getStatus());
    }

    @Test
    void testNonChargeableToChargeable_CreatesCharge() {
        // SCHEDULED -> COMPLETED
        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price20, LessonStudentStatus.COMPLETED);

        verify(balanceTransactionService).createBalanceTransaction(
                eq(student),
                eq(TransactionType.LESSON_CHARGE),
                eq(Currency.USD),
                eq(TransactionSource.LESSON),
                eq(price20.negate()),
                eq(100L)
        );
        assertEquals(LessonStudentStatus.COMPLETED, lessonStudent.getStatus());
    }

    @Test
    void testChargeableToNonChargeable_CreatesReversal() {
        // COMPLETED -> CANCELED
        lessonStudent.setStatus(LessonStudentStatus.COMPLETED);

        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price20, LessonStudentStatus.CANCELED);

        verify(balanceTransactionService).createBalanceTransaction(
                eq(student),
                eq(TransactionType.LESSON_CHARGE_REVERSAL),
                eq(Currency.USD),
                eq(TransactionSource.LESSON),
                eq(price20),
                eq(100L)
        );
        assertEquals(LessonStudentStatus.CANCELED, lessonStudent.getStatus());
    }

    @Test
    void testChargeableToChargeablePriceChanged_CreatesReversalAndNewCharge() {
        // COMPLETED -> COMPLETED, price 20 -> 25 (e.g. lesson became individual)
        lessonStudent.setStatus(LessonStudentStatus.COMPLETED);

        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price25, LessonStudentStatus.COMPLETED);

        // Should reverse old price
        verify(balanceTransactionService).createBalanceTransaction(
                eq(student),
                eq(TransactionType.LESSON_CHARGE_REVERSAL),
                eq(Currency.USD),
                eq(TransactionSource.LESSON),
                eq(price20),
                eq(100L)
        );

        // Should apply new price
        verify(balanceTransactionService).createBalanceTransaction(
                eq(student),
                eq(TransactionType.LESSON_CHARGE),
                eq(Currency.USD),
                eq(TransactionSource.LESSON),
                eq(price25.negate()),
                eq(100L)
        );

        assertEquals(price25, lessonStudent.getPrice());
        assertEquals(LessonStudentStatus.COMPLETED, lessonStudent.getStatus());
    }

    @Test
    void testChargeableToChargeableNoPriceChange_DoesNothing() {
        // COMPLETED -> NOT_ATTENDED (both are chargeable, same price)
        lessonStudent.setStatus(LessonStudentStatus.COMPLETED);

        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price20, LessonStudentStatus.NOT_ATTENDED);

        verifyNoInteractions(balanceTransactionService);
        assertEquals(LessonStudentStatus.NOT_ATTENDED, lessonStudent.getStatus());
    }
}
