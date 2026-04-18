package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.student.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonStudentService {

    private final LessonStudentRepository lessonStudentRepository;

    public LessonStudent save(LessonStudent lessonStudent) {
        LessonStudent savedLessonStudent = lessonStudentRepository.save(lessonStudent);
        log.debug("LessonStudent saved: {id={}, lessonId={}, studentId={}, price={}, currency={}}",
                savedLessonStudent.getId(), savedLessonStudent.getLesson().getId(),
                savedLessonStudent.getStudent().getId(),
                savedLessonStudent.getPrice(), savedLessonStudent.getCurrency());
        return savedLessonStudent;
    }

    public LessonStudent buildLessonStudent(Student student, Lesson lesson) {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setLesson(lesson);
        lessonStudent.setStudent(student);
        lessonStudent.setCurrency(student.getCurrency());
        log.debug("LessonStudent built for student {} lesson {}.", student.getId(), lesson.getId());
        return lessonStudent;
    }

    public Optional<LessonStudent> findByLessonIdAndStudentId(Long lessonId, Long studentId) {
        return lessonStudentRepository.findByLessonIdAndStudentId(lessonId, studentId);
    }

    public void delete(LessonStudent ls) {
        lessonStudentRepository.delete(ls);
    }
}
