package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentRepository;
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
}
