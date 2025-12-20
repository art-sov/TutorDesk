package com.art.tutordesk.balance;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private BalanceRepository balanceRepository;
    @Mock
    private LessonStudentRepository lessonStudentRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private BalanceService balanceService;

    private Student student;
    private Balance usdBalance;
    private Balance eurBalance;
    private LessonStudent lessonStudentPaid;
    private LessonStudent lessonStudentUnpaid;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");

        usdBalance = new Balance();
        usdBalance.setId(10L);
        usdBalance.setStudent(student);
        usdBalance.setCurrency(Currency.USD);
        usdBalance.setAmount(new BigDecimal("100.00"));

        eurBalance = new Balance();
        eurBalance.setId(11L);
        eurBalance.setStudent(student);
        eurBalance.setCurrency(Currency.EUR);
        eurBalance.setAmount(new BigDecimal("50.00"));

        Lesson mockLesson = new Lesson();
        mockLesson.setId(100L);

        lessonStudentPaid = new LessonStudent();
        lessonStudentPaid.setId(1L);
        lessonStudentPaid.setStudent(student);
        lessonStudentPaid.setPrice(new BigDecimal("50.00"));
        lessonStudentPaid.setCurrency(Currency.USD);
        lessonStudentPaid.setPaymentStatus(PaymentStatus.UNPAID);
        lessonStudentPaid.setLesson(mockLesson);

        lessonStudentUnpaid = new LessonStudent();
        lessonStudentUnpaid.setId(2L);
        lessonStudentUnpaid.setStudent(student);
        lessonStudentUnpaid.setPrice(new BigDecimal("70.00"));
        lessonStudentUnpaid.setCurrency(Currency.USD);
        lessonStudentUnpaid.setPaymentStatus(PaymentStatus.UNPAID);
        lessonStudentUnpaid.setLesson(mockLesson);
    }

    @Test
    void getAllBalancesForStudent_shouldReturnListOfBalances_whenBalancesExist() {
        when(balanceRepository.findByStudentId(1L)).thenReturn(Arrays.asList(usdBalance, eurBalance));

        List<Balance> result = balanceService.getAllBalancesForStudent(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(usdBalance));
        assertTrue(result.contains(eurBalance));
        verify(balanceRepository, times(1)).findByStudentId(1L);
    }

    @Test
    void getAllBalancesForStudent_shouldReturnEmptyList_whenNoBalancesExist() {
        when(balanceRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());

        List<Balance> result = balanceService.getAllBalancesForStudent(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(balanceRepository, times(1)).findByStudentId(1L);
    }

    @Test
    void changeBalance_shouldIncreaseExistingBalance() {
        when(balanceRepository.findByStudentIdAndCurrency(1L, Currency.USD))
                .thenReturn(Optional.of(usdBalance));

        balanceService.changeBalance(1L, Currency.USD, new BigDecimal("25.00"));

        assertEquals(new BigDecimal("125.00"), usdBalance.getAmount());
        verify(balanceRepository, times(1)).findByStudentIdAndCurrency(1L, Currency.USD);
        verify(balanceRepository, never()).save(any(Balance.class)); // No explicit save due to dirty checking
    }

    @Test
    void changeBalance_shouldDecreaseExistingBalance() {
        when(balanceRepository.findByStudentIdAndCurrency(1L, Currency.USD))
                .thenReturn(Optional.of(usdBalance));

        balanceService.changeBalance(1L, Currency.USD, new BigDecimal("-50.00"));

        assertEquals(new BigDecimal("50.00"), usdBalance.getAmount());
        verify(balanceRepository, times(1)).findByStudentIdAndCurrency(1L, Currency.USD);
        verify(balanceRepository, never()).save(any(Balance.class));
    }

    @Test
    void changeBalance_shouldCreateNewBalance_whenNotExists() {
        Balance newBalance = new Balance();
        newBalance.setStudent(student);
        newBalance.setCurrency(Currency.PLN);
        newBalance.setAmount(BigDecimal.ZERO);

        when(balanceRepository.findByStudentIdAndCurrency(1L, Currency.PLN))
                .thenReturn(Optional.empty());
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(balanceRepository.save(any(Balance.class))).thenReturn(newBalance);

        balanceService.changeBalance(1L, Currency.PLN, new BigDecimal("75.00"));

        assertEquals(new BigDecimal("75.00"), newBalance.getAmount());
        verify(balanceRepository, times(1)).findByStudentIdAndCurrency(1L, Currency.PLN);
        verify(studentRepository, times(1)).findById(1L);
        verify(balanceRepository, times(1)).save(any(Balance.class));
    }

    @Test
    void changeBalance_shouldDoNothing_whenDeltaIsZero() {
        balanceService.changeBalance(1L, Currency.USD, BigDecimal.ZERO);

        assertEquals(new BigDecimal("100.00"), usdBalance.getAmount()); // Amount should not change
        verify(balanceRepository, never()).findByStudentIdAndCurrency(anyLong(), any(Currency.class));
        verify(studentRepository, never()).findById(anyLong());
        verify(balanceRepository, never()).save(any(Balance.class));
    }

    @Test
    void changeBalance_shouldThrowException_whenStudentNotFoundForNewBalance() {
        when(balanceRepository.findByStudentIdAndCurrency(1L, Currency.PLN))
                .thenReturn(Optional.empty());
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                balanceService.changeBalance(1L, Currency.PLN, new BigDecimal("50.00")));

        assertEquals("Student not found with id: 1", exception.getMessage());
        verify(balanceRepository, times(1)).findByStudentIdAndCurrency(1L, Currency.PLN);
        verify(studentRepository, times(1)).findById(1L);
        verify(balanceRepository, never()).save(any(Balance.class));
    }

    @Test
    void resyncPaymentStatus_shouldUpdatePaymentStatusesCorrectly() {
        // Setup currencies available for the student
        Set<Currency> currencies = new HashSet<>(List.of(Currency.USD));
        when(lessonStudentRepository.findCurrenciesByStudentId(1L)).thenReturn(currencies);
        when(balanceRepository.findCurrenciesByStudentId(1L)).thenReturn(currencies);

        // Setup student
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Setup payment and lessons for USD
        when(paymentRepository.sumPayments(student, Currency.USD)).thenReturn(new BigDecimal("120.00")); // Enough to pay for both lessons
        when(lessonStudentRepository.findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                student, Currency.USD, PaymentStatus.FREE)).thenReturn(Arrays.asList(lessonStudentPaid, lessonStudentUnpaid));

        balanceService.resyncPaymentStatus(1L);

        // Verify lesson statuses are updated
        assertEquals(PaymentStatus.PAID, lessonStudentPaid.getPaymentStatus());
        assertEquals(PaymentStatus.PAID, lessonStudentUnpaid.getPaymentStatus());

        // Verify interactions with mocks
        verify(studentRepository, times(1)).findById(1L);
        verify(lessonStudentRepository, times(1)).findCurrenciesByStudentId(1L);
        verify(balanceRepository, times(1)).findCurrenciesByStudentId(1L);
        verify(paymentRepository, times(1)).sumPayments(student, Currency.USD);
        verify(lessonStudentRepository, times(1))
                .findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                        student, Currency.USD, PaymentStatus.FREE);
    }

    @Test
    void resyncPaymentStatus_shouldHandlePartialPayment() {
        // Setup currencies available for the student
        Set<Currency> currencies = new HashSet<>(List.of(Currency.USD));
        when(lessonStudentRepository.findCurrenciesByStudentId(1L)).thenReturn(currencies);
        when(balanceRepository.findCurrenciesByStudentId(1L)).thenReturn(currencies);

        // Setup student
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Setup payment and lessons for USD (enough for lessonStudentPaid, not for lessonStudentUnpaid)
        when(paymentRepository.sumPayments(student, Currency.USD)).thenReturn(new BigDecimal("60.00"));
        when(lessonStudentRepository.findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                student, Currency.USD, PaymentStatus.FREE)).thenReturn(Arrays.asList(lessonStudentPaid, lessonStudentUnpaid));

        balanceService.resyncPaymentStatus(1L);

        // Verify lesson statuses are updated
        assertEquals(PaymentStatus.PAID, lessonStudentPaid.getPaymentStatus());
        assertEquals(PaymentStatus.UNPAID, lessonStudentUnpaid.getPaymentStatus()); // Not enough credit

        // Verify interactions with mocks
        verify(studentRepository, times(1)).findById(1L);
        verify(lessonStudentRepository, times(1)).findCurrenciesByStudentId(1L);
        verify(balanceRepository, times(1)).findCurrenciesByStudentId(1L);
        verify(paymentRepository, times(1)).sumPayments(student, Currency.USD);
        verify(lessonStudentRepository, times(1))
                .findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                        student, Currency.USD, PaymentStatus.FREE);
    }

    @Test
    void resyncPaymentStatus_shouldHandleNoPayments() {
        // Setup currencies available for the student
        Set<Currency> currencies = new HashSet<>(List.of(Currency.USD));
        when(lessonStudentRepository.findCurrenciesByStudentId(1L)).thenReturn(currencies);
        when(balanceRepository.findCurrenciesByStudentId(1L)).thenReturn(currencies);

        // Setup student
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Setup no payments for USD
        when(paymentRepository.sumPayments(student, Currency.USD)).thenReturn(BigDecimal.ZERO);
        when(lessonStudentRepository.findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                student, Currency.USD, PaymentStatus.FREE)).thenReturn(Arrays.asList(lessonStudentPaid, lessonStudentUnpaid));

        balanceService.resyncPaymentStatus(1L);

        // Verify lesson statuses are updated (should remain UNPAID)
        assertEquals(PaymentStatus.UNPAID, lessonStudentPaid.getPaymentStatus());
        assertEquals(PaymentStatus.UNPAID, lessonStudentUnpaid.getPaymentStatus());

        // Verify interactions with mocks
        verify(studentRepository, times(1)).findById(1L);
        verify(lessonStudentRepository, times(1)).findCurrenciesByStudentId(1L);
        verify(balanceRepository, times(1)).findCurrenciesByStudentId(1L);
        verify(paymentRepository, times(1)).sumPayments(student, Currency.USD);
        verify(lessonStudentRepository, times(1))
                .findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                        student, Currency.USD, PaymentStatus.FREE);
    }

    @Test
    void resyncPaymentStatus_shouldThrowException_whenStudentNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                balanceService.resyncPaymentStatus(1L));

        assertEquals("Student not found with id: 1", exception.getMessage());
        verify(studentRepository, times(1)).findById(1L);
        verify(lessonStudentRepository, never()).findCurrenciesByStudentId(anyLong());
        verify(balanceRepository, never()).findCurrenciesByStudentId(anyLong());
        verify(paymentRepository, never()).sumPayments(any(Student.class), any(Currency.class));
        verify(lessonStudentRepository, never())
                .findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                        any(Student.class), any(Currency.class), any(PaymentStatus.class));
    }
}