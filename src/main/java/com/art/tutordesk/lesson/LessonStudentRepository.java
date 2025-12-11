package com.art.tutordesk.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonStudentRepository extends JpaRepository<LessonStudent, Long> {

    @Query("SELECT ls FROM LessonStudent ls JOIN ls.lesson l " +
           "WHERE l.lessonDate BETWEEN :startDate AND :endDate " +
           "AND (:studentIds IS NULL OR ls.student.id IN :studentIds)")
    List<LessonStudent> findByLessonDateBetweenAndStudentIds(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("studentIds") List<Long> studentIds);

}
