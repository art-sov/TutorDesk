package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.student.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonStudentService {

    private final LessonStudentRepository lessonStudentRepository;

    public LessonStudent save(LessonStudent lessonStudent) {
        LessonStudent savedLessonStudent = lessonStudentRepository.save(lessonStudent);
        log.debug("LessonStudent saved: {id={}, lessonId={}, studentId={}, paymentStatus={}, price={}, currency={}}",
                savedLessonStudent.getId(), savedLessonStudent.getLesson().getId(),
                savedLessonStudent.getStudent().getId(), savedLessonStudent.getPaymentStatus(),
                savedLessonStudent.getPrice(), savedLessonStudent.getCurrency());
        return savedLessonStudent;
    }

    public LessonStudent buildLessonStudent(Student student, Lesson lesson, PaymentStatus paymentStatus) {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setLesson(lesson);
        lessonStudent.setStudent(student);
        lessonStudent.setPaymentStatus(paymentStatus);
        // Price and currency will be set in the calling service, which has more context.
        lessonStudent.setCurrency(student.getCurrency());
        log.debug("LessonStudent built for student {} lesson {}.", student.getId(), lesson.getId());
        return lessonStudent;
    }
}
