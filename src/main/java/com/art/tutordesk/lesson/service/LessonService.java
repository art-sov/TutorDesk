package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonRepository;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.payment.PaymentStatus;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final StudentService studentService;
    private final LessonStudentService lessonStudentService;


    public List<Lesson> getAllLessonsSorted() {
        return lessonRepository.findAllWithStudentsSorted();
    }

    @Transactional
    public Lesson saveLesson(Lesson lesson, List<Long> selectedStudentIds) {
        // 1. Save the Lesson first to get its ID
        Lesson savedLesson = lessonRepository.save(lesson);

        // 2. Associate students if IDs are provided
        if (!CollectionUtils.isEmpty(selectedStudentIds)) {
            for (Long studentId : selectedStudentIds) {
                // todo Fetch each student. In a real app, optimize with a single query to fetch all students by IDs.
                Student student = studentService.getStudentById(studentId);

                // Create and save LessonStudent association
                LessonStudent lessonStudent = createLessonStudent(student, savedLesson);
                lessonStudentService.save(lessonStudent);
            }
        }

        // Refresh the saved lesson to ensure its lessonStudents collection is up-to-date in the current session
        // This might be necessary if the save logic within lessonStudentService doesn't automatically update the lesson's collection
        return lessonRepository.findById(savedLesson.getId()).orElse(savedLesson);
    }

    private LessonStudent createLessonStudent(Student student, Lesson savedLesson) {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setLesson(savedLesson);
        lessonStudent.setStudent(student);
        lessonStudent.setPaymentStatus(PaymentStatus.UNPAID); // Default status for new enrollments
        return lessonStudent;
    }
}
