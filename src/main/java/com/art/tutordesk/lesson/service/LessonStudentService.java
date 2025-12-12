package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.student.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonStudentService {

    private final LessonStudentRepository lessonStudentRepository;

    @Autowired
    public LessonStudentService(LessonStudentRepository lessonStudentRepository) {
        this.lessonStudentRepository = lessonStudentRepository;
    }

    public LessonStudent save(LessonStudent lessonStudent) {
        return lessonStudentRepository.save(lessonStudent);
    }

    public LessonStudent buildLessonStudent(Student student, Lesson lesson, PaymentStatus paymentStatus) {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setLesson(lesson);
        lessonStudent.setStudent(student);
        lessonStudent.setPaymentStatus(paymentStatus);
        // Price and currency will be set in the calling service, which has more context.
        lessonStudent.setCurrency(student.getCurrency());
        return lessonStudent;
    }
}
