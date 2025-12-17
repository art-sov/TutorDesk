package com.art.tutordesk.lesson.repository;

import com.art.tutordesk.lesson.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    long countByLessonDateGreaterThanEqual(LocalDate startDate);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.lessonStudents ls LEFT JOIN FETCH ls.student WHERE l.lessonDate BETWEEN :startDate AND :endDate ORDER BY l.lessonDate ASC, l.startTime ASC")
    List<Lesson> findByLessonDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
