package com.art.tutordesk.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonStudentRepository extends JpaRepository<LessonStudent, Long> {
}
