package com.art.tutordesk.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/data-test.sql")
public class StudentRepositoryIT {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void whenFindAllByActiveTrue_thenReturnsOnlyActiveStudents() {
        List<Student> activeStudents = studentRepository.findAllByActiveTrueOrderByIdAsc();

        assertThat(activeStudents).isNotNull();
        assertThat(activeStudents).hasSize(4);
        assertThat(activeStudents.stream().allMatch(Student::isActive)).isTrue();
    }

    @Test
    void whenFindAllByActiveTrue_thenReturnsStudentsInIdAscOrder() {
        List<Student> activeStudents = studentRepository.findAllByActiveTrueOrderByIdAsc();

        assertThat(activeStudents).isNotNull();
        assertThat(activeStudents).hasSize(4);
        List<Long> ids = activeStudents.stream().map(Student::getId).collect(Collectors.toList());
        assertThat(ids).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void whenCountByActiveTrue_thenReturnsCorrectCount() {
        long count = studentRepository.countByActiveTrue();

        assertThat(count).isEqualTo(4);
    }

    @Test
    void whenFindAllByIdIn_thenReturnsMatchingStudents() {
        List<Long> idsToFind = Arrays.asList(1L, 3L, 5L);

        List<Student> foundStudents = studentRepository.findAllByIdIn(idsToFind);

        assertThat(foundStudents).isNotNull();
        assertThat(foundStudents).hasSize(3);
        List<Long> foundIds = foundStudents.stream().map(Student::getId).collect(Collectors.toList());
        assertThat(foundIds).containsExactlyInAnyOrder(1L, 3L, 5L);
    }

    @Test
    void whenFindAllByIdIn_withNonExistentIds_thenReturnsOnlyMatchingStudents() {
        List<Long> idsToFind = Arrays.asList(1L, 99L, 3L, 100L);

        List<Student> foundStudents = studentRepository.findAllByIdIn(idsToFind);

        assertThat(foundStudents).isNotNull();
        assertThat(foundStudents).hasSize(2);
        List<Long> foundIds = foundStudents.stream().map(Student::getId).collect(Collectors.toList());
        assertThat(foundIds).containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void whenFindAllByIdIn_withEmptyList_thenReturnsEmptyList() {
        List<Long> idsToFind = List.of();

        List<Student> foundStudents = studentRepository.findAllByIdIn(idsToFind);

        assertThat(foundStudents).isNotNull();
        assertThat(foundStudents).isEmpty();
    }
}