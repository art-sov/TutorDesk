package com.art.tutordesk.lesson.repository;

import com.art.tutordesk.BaseIntegrationTest;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.payment.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/data-test.sql")
public class LessonStudentRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private LessonStudentRepository lessonStudentRepository;

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
    void deleteAllByStudentId_shouldRemoveAllStudentLessons() {
        Long studentIdToDelete = 1L;
        long initialCount = lessonStudentRepository.findAll().stream().filter(ls -> ls.getStudent().getId().equals(studentIdToDelete)).count();
        assertThat(initialCount).isEqualTo(4); // Student 1 has 3 lessons

        lessonStudentRepository.deleteAllByStudentId(studentIdToDelete);
        lessonStudentRepository.flush(); // Essential for @Modifying queries in tests

        long finalCount = lessonStudentRepository.findAll().stream().filter(ls -> ls.getStudent().getId().equals(studentIdToDelete)).count();
        assertThat(finalCount).isZero();

        // And other students' lessons should remain
        long otherStudentsLessonCount = lessonStudentRepository.findAll().stream().filter(ls -> !ls.getStudent().getId().equals(studentIdToDelete)).count();
        assertThat(otherStudentsLessonCount).isEqualTo(4);
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

    @Test
    void findByLessonIdAndStudentId_shouldReturnLessonStudent_whenFound() {
        Long lessonId = 1L;
        Long studentId = 1L;

        Optional<LessonStudent> result = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, studentId);

        assertThat(result).isPresent();
        assertThat(result.get().getLesson().getId()).isEqualTo(lessonId);
        assertThat(result.get().getStudent().getId()).isEqualTo(studentId);
    }

    @Test
    void findByLessonIdAndStudentId_shouldReturnEmptyOptional_whenNotFound() {
        Long nonExistentLessonId = 99L;
        Long nonExistentStudentId = 99L;

        Optional<LessonStudent> result = lessonStudentRepository.findByLessonIdAndStudentId(nonExistentLessonId, nonExistentStudentId);

        assertThat(result).isNotPresent();
    }
}