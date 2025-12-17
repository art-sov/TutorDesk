package com.art.tutordesk.lesson.repository;

import com.art.tutordesk.lesson.Lesson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class LessonRepositoryIT {

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    void findAllWithStudentsSorted_shouldReturnAllLessonsSorted() {
        List<Lesson> lessons = lessonRepository.findAllWithStudentsSorted();

        assertThat(lessons).isNotNull();
        assertThat(lessons).hasSize(2);

        // Verify sorting (by date, then time)
        assertThat(lessons.get(0).getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(lessons.get(0).getId()).isEqualTo(1L);
        assertThat(lessons.get(1).getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 2));
        assertThat(lessons.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void findAllWithStudentsSorted_shouldFetchStudentsEagerly() {
        List<Lesson> lessons = lessonRepository.findAllWithStudentsSorted();
        Lesson firstLesson = lessons.get(0);
        Lesson secondLesson = lessons.get(1);

        // No LazyInitializationException should be thrown when accessing students
        assertThat(firstLesson.getLessonStudents()).hasSize(2);
        List<Long> firstLessonStudentIds = firstLesson.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(firstLessonStudentIds).containsExactlyInAnyOrder(1L, 2L);

        assertThat(secondLesson.getLessonStudents()).hasSize(2);
        List<Long> secondLessonStudentIds = secondLesson.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(secondLessonStudentIds).containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void countByLessonDateGreaterThanEqual_shouldReturnCorrectCount() {
        long countAll = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 1));
        long countOne = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 2));
        long countNone = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 3));

        assertThat(countAll).isEqualTo(2);
        assertThat(countOne).isEqualTo(1);
        assertThat(countNone).isZero();
    }

    @Test
    void findByLessonDateBetween_shouldReturnCorrectLessons() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 2);

        List<Lesson> lessons = lessonRepository.findByLessonDateBetween(startDate, endDate);

        assertThat(lessons).isNotNull();
        assertThat(lessons).hasSize(2);
        assertThat(lessons.get(0).getId()).isEqualTo(1L);
        assertThat(lessons.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void findByLessonDateBetween_shouldReturnSingleLesson() {
        LocalDate startDate = LocalDate.of(2025, 1, 2);
        LocalDate endDate = LocalDate.of(2025, 1, 2);

        List<Lesson> lessons = lessonRepository.findByLessonDateBetween(startDate, endDate);

        assertThat(lessons).isNotNull();
        assertThat(lessons).hasSize(1);
        assertThat(lessons.getFirst().getId()).isEqualTo(2L);
    }

    @Test
    void findByLessonDateBetween_shouldReturnEmptyListForNoMatches() {
        LocalDate startDate = LocalDate.of(2025, 2, 1);
        LocalDate endDate = LocalDate.of(2025, 2, 28);

        List<Lesson> lessons = lessonRepository.findByLessonDateBetween(startDate, endDate);

        assertThat(lessons).isNotNull();
        assertThat(lessons).isEmpty();
    }

    @Test
    void findByLessonDateBetween_shouldFetchStudentsEagerly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 1);

        List<Lesson> lessons = lessonRepository.findByLessonDateBetween(startDate, endDate);
        Lesson lesson = lessons.getFirst();

        // No LazyInitializationException should be thrown when accessing students
        assertThat(lesson.getLessonStudents()).hasSize(2);
        List<Long> studentIds = lesson.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(studentIds).containsExactlyInAnyOrder(1L, 2L);
    }
}