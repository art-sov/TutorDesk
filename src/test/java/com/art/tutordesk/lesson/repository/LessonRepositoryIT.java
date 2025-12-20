package com.art.tutordesk.lesson.repository;

import com.art.tutordesk.lesson.Lesson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/data-test.sql")
public class LessonRepositoryIT {

    @Autowired
    private LessonRepository lessonRepository;

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