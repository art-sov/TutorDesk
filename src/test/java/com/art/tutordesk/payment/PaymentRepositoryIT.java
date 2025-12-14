package com.art.tutordesk.payment;

import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PaymentRepositoryIT {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Student student1;
    private Student student2;
    private Student student3; // Free student
    private Student student4;
    private Student student5; // Inactive student

    @BeforeEach
    void setUp() {
        student1 = entityManager.find(Student.class, 1L);
        student2 = entityManager.find(Student.class, 2L);
        student3 = entityManager.find(Student.class, 3L);
        student4 = entityManager.find(Student.class, 4L);
        student5 = entityManager.find(Student.class, 5L);
    }

    @Test
    void sumPayments_shouldReturnCorrectSumForStudentAndCurrency() {
        // Student 1, USD: Payments 1 (10.00 USD), 4 (20.00 USD) = 30.00 USD
        BigDecimal sum1 = paymentRepository.sumPayments(student1, Currency.USD);
        assertThat(sum1).isEqualByComparingTo(new BigDecimal("30.00"));

        // Student 2, EUR: Payments 2 (15.00 EUR), 6 (25.00 EUR) = 40.00 EUR
        BigDecimal sum2 = paymentRepository.sumPayments(student2, Currency.EUR);
        assertThat(sum2).isEqualByComparingTo(new BigDecimal("40.00"));

        // Student 3, PLN: No payments = 0.00 PLN
        BigDecimal sum3 = paymentRepository.sumPayments(student3, Currency.PLN);
        assertThat(sum3).isEqualByComparingTo(BigDecimal.ZERO);

        // Student 5 (Inactive), EUR: Payment 5 (30.00 EUR) = 30.00 EUR
        BigDecimal sum5 = paymentRepository.sumPayments(student5, Currency.EUR);
        assertThat(sum5).isEqualByComparingTo(new BigDecimal("30.00"));

        // Student 4, USD: Payment 3 (10.00 USD) = 10.00 USD
        BigDecimal sum4 = paymentRepository.sumPayments(student4, Currency.USD);
        assertThat(sum4).isEqualByComparingTo(new BigDecimal("10.00"));

        // Student 1, EUR: No EUR payments = 0.00 EUR
        BigDecimal sumNoMatch = paymentRepository.sumPayments(student1, Currency.EUR);
        assertThat(sumNoMatch).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void findByPaymentDateGreaterThanEqual_shouldReturnPaymentsOnOrAfterDate() {
        // Payments on or after '2025-01-05' (ID 3, 4, 5, 6)
        LocalDate startDate = LocalDate.of(2025, 1, 5);
        List<Payment> payments = paymentRepository.findByPaymentDateGreaterThanEqual(startDate);

        assertThat(payments).hasSize(4); // Payments with ID 3, 4, 5, 6
        assertThat(payments).extracting(Payment::getId).containsExactlyInAnyOrder(3L, 4L, 5L, 6L);

        // Payments on or after '2025-02-01' (ID 6)
        LocalDate laterDate = LocalDate.of(2025, 2, 1);
        List<Payment> laterPayments = paymentRepository.findByPaymentDateGreaterThanEqual(laterDate);

        assertThat(laterPayments).hasSize(1); // Payment with ID 6
        assertThat(laterPayments).extracting(Payment::getId).containsExactlyInAnyOrder(6L);

        // Payments on or after '2025-03-01' (no payments)
        LocalDate futureDate = LocalDate.of(2025, 3, 1);
        List<Payment> futurePayments = paymentRepository.findByPaymentDateGreaterThanEqual(futureDate);
        assertThat(futurePayments).isEmpty();
    }

    @Test
    void findByFilters_shouldReturnPaymentsMatchingAllCriteria() {
        LocalDate startDate = LocalDate.of(2025, 1, 1); // Covers payments from ID 2 onwards
        LocalDate endDate = LocalDate.of(2025, 1, 31); // Covers payments up to ID 5

        // Test 1: Filter by date range only
        List<Payment> paymentsDateRange = paymentRepository.findByFilters(startDate, endDate, null);
        assertThat(paymentsDateRange).hasSize(4); // Payments with ID 2, 3, 4, 5
        assertThat(paymentsDateRange).extracting(Payment::getId).containsExactlyInAnyOrder(2L, 3L, 4L, 5L);

        // Test 2: Filter by date range and specific students (Student 1 and 4)
        List<Long> studentIds = Arrays.asList(1L, 4L);
        List<Payment> paymentsDateAndStudents = paymentRepository.findByFilters(startDate, endDate, studentIds);
        assertThat(paymentsDateAndStudents).hasSize(2); // Payments ID 3 (Student 4), ID 4 (Student 1)
        assertThat(paymentsDateAndStudents).extracting(Payment::getId).containsExactlyInAnyOrder(3L, 4L);

        // Test 3: Filter by date range and non-existent student
        List<Long> nonExistentStudent = Collections.singletonList(99L);
        List<Payment> noPayments = paymentRepository.findByFilters(startDate, endDate, nonExistentStudent);
        assertThat(noPayments).isEmpty();

        // Test 4: Filter by date range including future payments
        LocalDate endDateFuture = LocalDate.of(2025, 3, 1);
        List<Payment> paymentsWithFuture = paymentRepository.findByFilters(startDate, endDateFuture, null);
        assertThat(paymentsWithFuture).hasSize(5); // Payments ID 2, 3, 4, 5, 6
        assertThat(paymentsWithFuture).extracting(Payment::getId).containsExactlyInAnyOrder(2L, 3L, 4L, 5L, 6L);

        // Test 5: Filter by an old date range, should return none
        LocalDate oldStartDate = LocalDate.of(2024, 1, 1);
        LocalDate oldEndDate = LocalDate.of(2024, 12, 1);
        List<Payment> oldPayments = paymentRepository.findByFilters(oldStartDate, oldEndDate, null);
        assertThat(oldPayments).isEmpty();
    }

    @Test
    void deleteAllByStudentId_shouldDeleteAllPaymentsForStudent() {
        // Pre-condition: Verify payments exist for student 1
        assertThat(paymentRepository.findAll().stream()
                .filter(p -> p.getStudent().getId().equals(1L)))
                .hasSize(2);

        // Action: Delete payments for student 1
        paymentRepository.deleteAllByStudentId(1L);
        entityManager.flush(); // Ensure changes are flushed to the database

        // Post-condition: Verify payments for student 1 are deleted
        assertThat(paymentRepository.findAll().stream()
                .filter(p -> p.getStudent().getId().equals(1L)))
                .isEmpty();

        // Verify payments for other students are not deleted (e.g., student 2)
        assertThat(paymentRepository.findAll().stream()
                .filter(p -> p.getStudent().getId().equals(2L)))
                .hasSize(2); // Payments ID 2 and 6
    }
}