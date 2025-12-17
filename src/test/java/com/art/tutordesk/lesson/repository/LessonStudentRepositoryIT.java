package com.art.tutordesk.lesson.repository;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class LessonStudentRepositoryIT {

    @Autowired
    private LessonStudentRepository lessonStudentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void findByLessonDateBetweenAndStudentIds_withSpecificStudent_shouldReturnMatchingLessons() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 10);
        List<Long> studentIds = Collections.singletonList(1L);

        List<LessonStudent> result = lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, studentIds);

        assertThat(result).hasSize(3);
        assertThat(result.stream().allMatch(ls -> ls.getStudent().getId() == 1L)).isTrue();
    }

    @Test
    void findByLessonDateBetweenAndStudentIds_withMultipleStudents_shouldReturnMatchingLessons() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 2);
        List<Long> studentIds = Arrays.asList(1L, 4L);

        List<LessonStudent> result = lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, studentIds);

        assertThat(result).hasSize(2);
        List<Long> foundStudentIds = result.stream().map(ls -> ls.getStudent().getId()).collect(Collectors.toList());
        assertThat(foundStudentIds).containsExactlyInAnyOrder(1L, 4L);
    }

    @Test
    void findByLessonDateBetweenAndStudentIds_withNullStudentIds_shouldReturnAllInDateRange() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 1);

        List<LessonStudent> result = lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, null);

        assertThat(result).hasSize(2);
        List<Long> foundStudentIds = result.stream().map(ls -> ls.getStudent().getId()).collect(Collectors.toList());
        assertThat(foundStudentIds).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void findAllByStudentAndCurrencyAndPaymentStatusNot_shouldReturnCorrectOrderedList() {
        Student student1 = studentRepository.findById(1L).orElseThrow();
        Currency currency = Currency.USD;
        PaymentStatus statusToExclude = PaymentStatus.FREE;

        List<LessonStudent> result = lessonStudentRepository.findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(student1, currency, statusToExclude);

        assertThat(result).hasSize(2);
        // Records for student 1 in USD are from 2025-01-01 (UNPAID) and 2025-01-10 (PAID)
        assertThat(result.get(0).getLesson().getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(result.get(1).getLesson().getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 10));
        assertThat(result.get(1).getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void findAllByStudentAndCurrencyAndPaymentStatusNot_excludingPaid_shouldReturnUnpaid() {
        Student student1 = studentRepository.findById(1L).orElseThrow();
        Currency currency = Currency.USD;
        PaymentStatus statusToExclude = PaymentStatus.PAID;

        List<LessonStudent> result = lessonStudentRepository.findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(student1, currency, statusToExclude);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(result.getFirst().getLesson().getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void deleteAllByStudentId_shouldRemoveAllStudentLessons() {
        Long studentIdToDelete = 1L;
        long initialCount = lessonStudentRepository.findAll().stream().filter(ls -> ls.getStudent().getId().equals(studentIdToDelete)).count();
        assertThat(initialCount).isEqualTo(3); // Student 1 has 3 lessons

        lessonStudentRepository.deleteAllByStudentId(studentIdToDelete);
        lessonStudentRepository.flush(); // Essential for @Modifying queries in tests

        long finalCount = lessonStudentRepository.findAll().stream().filter(ls -> ls.getStudent().getId().equals(studentIdToDelete)).count();
        assertThat(finalCount).isZero();

        // And other students' lessons should remain
        long otherStudentsLessonCount = lessonStudentRepository.findAll().stream().filter(ls -> !ls.getStudent().getId().equals(studentIdToDelete)).count();
        assertThat(otherStudentsLessonCount).isEqualTo(3);
    }

    @Test
    void findCurrenciesByStudentId_withMultipleCurrencies_shouldReturnDistinctSet() {
        Long studentId = 1L; // Student 1 has lessons in USD and EUR

        Set<Currency> currencies = lessonStudentRepository.findCurrenciesByStudentId(studentId);

        assertThat(currencies).isNotNull();
        assertThat(currencies).hasSize(2);
        assertThat(currencies).containsExactlyInAnyOrder(Currency.USD, Currency.EUR);
    }

    @Test
    void findCurrenciesByStudentId_withSingleCurrency_shouldReturnSingleElementSet() {
        Long studentId = 2L; // Student 2 only has lessons in EUR

        Set<Currency> currencies = lessonStudentRepository.findCurrenciesByStudentId(studentId);

        assertThat(currencies).isNotNull();
        assertThat(currencies).hasSize(1);
        assertThat(currencies).contains(Currency.EUR);
    }

    @Test
    void findCurrenciesByStudentId_withNoLessons_shouldReturnEmptySet() {
        Long studentId = 5L; // Inactive student with no lessons

        Set<Currency> currencies = lessonStudentRepository.findCurrenciesByStudentId(studentId);

        assertThat(currencies).isNotNull();
        assertThat(currencies).isEmpty();
    }
}