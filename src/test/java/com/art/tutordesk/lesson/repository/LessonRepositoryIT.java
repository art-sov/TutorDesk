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
        assertThat(lessons).hasSize(4);

        // Verify sorting (by date, then time)
        assertThat(lessons.get(0).getId()).isEqualTo(1L); // 2025-01-01
        assertThat(lessons.get(1).getId()).isEqualTo(2L); // 2025-01-02
        assertThat(lessons.get(2).getId()).isEqualTo(4L); // 2025-01-05
        assertThat(lessons.get(3).getId()).isEqualTo(3L); // 2025-01-10
    }

    @Test
    void findAllWithStudentsSorted_shouldFetchStudentsEagerly() {
        List<Lesson> lessons = lessonRepository.findAllWithStudentsSorted();
        Lesson lesson1 = lessons.get(0); // 2025-01-01
        Lesson lesson2 = lessons.get(1); // 2025-01-02
        Lesson lesson3 = lessons.get(2); // 2025-01-05
        Lesson lesson4 = lessons.get(3); // 2025-01-10

        // No LazyInitializationException should be thrown when accessing students
        assertThat(lesson1.getLessonStudents()).hasSize(2);
        List<Long> lesson1StudentIds = lesson1.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(lesson1StudentIds).containsExactlyInAnyOrder(1L, 2L);

        assertThat(lesson2.getLessonStudents()).hasSize(2);
        List<Long> lesson2StudentIds = lesson2.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(lesson2StudentIds).containsExactlyInAnyOrder(3L, 4L);

        assertThat(lesson3.getLessonStudents()).hasSize(1);
        List<Long> lesson3StudentIds = lesson3.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(lesson3StudentIds).containsExactlyInAnyOrder(1L);

        assertThat(lesson4.getLessonStudents()).hasSize(1);
        List<Long> lesson4StudentIds = lesson4.getLessonStudents().stream()
                .map(ls -> ls.getStudent().getId())
                .collect(Collectors.toList());
        assertThat(lesson4StudentIds).containsExactlyInAnyOrder(1L);
    }

    @Test
    void countByLessonDateGreaterThanEqual_shouldReturnCorrectCount() {
        long countAll = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 1));
        long countThree = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 2));
        long countTwo = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 3));
        long countOne = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 6));
        long countNone = lessonRepository.countByLessonDateGreaterThanEqual(LocalDate.of(2025, 1, 11));

        assertThat(countAll).isEqualTo(4);
        assertThat(countThree).isEqualTo(3);
        assertThat(countTwo).isEqualTo(2);
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