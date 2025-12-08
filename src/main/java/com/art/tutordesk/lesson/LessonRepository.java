package com.art.tutordesk.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.lessonStudents ORDER BY l.lessonDate, l.startTime")
    List<Lesson> findAllWithStudentsSorted();
}
