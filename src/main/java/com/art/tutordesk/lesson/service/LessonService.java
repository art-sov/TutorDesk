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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final StudentService studentService;
    private final LessonStudentService lessonStudentService;


    public List<Lesson> getAllLessonsSorted() {
        return lessonRepository.findAllWithStudentsSorted();
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
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
                LessonStudent lessonStudent = createLessonStudent(student, savedLesson, PaymentStatus.UNPAID); // Call overloaded method
                lessonStudentService.save(lessonStudent);
            }
        }

        // Refresh the saved lesson to ensure its lessonStudents collection is up-to-date in the current session
        // This might be necessary if the save logic within lessonStudentService doesn't automatically update the lesson's collection
        return lessonRepository.findById(savedLesson.getId()).orElse(savedLesson);
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson, List<Long> selectedStudentIds) {
        // 1. Retrieve the existing lesson
        Lesson existingLesson = lessonRepository.findById(lesson.getId())
                .orElseThrow(() -> new RuntimeException("Lesson not found for update with id: " + lesson.getId()));

        // Store current payment statuses for existing lessonStudents before clearing
        Map<Long, PaymentStatus> existingStudentPaymentStatuses = existingLesson.getLessonStudents().stream()
                .collect(Collectors.toMap(
                        ls -> ls.getStudent().getId(),
                        LessonStudent::getPaymentStatus,
                        (oldValue, newValue) -> oldValue // Handle duplicate student IDs if somehow present, keep old status
                ));

        // 2. Update fields from the provided lesson object
        existingLesson.setLessonDate(lesson.getLessonDate());
        existingLesson.setStartTime(lesson.getStartTime());
        existingLesson.setTopic(lesson.getTopic());

        // 3. Clear existing lessonStudents associations.
        // Due to orphanRemoval=true, these will be deleted from the database.
        existingLesson.getLessonStudents().clear(); // This will trigger deletion
        Lesson updatedLesson = lessonRepository.save(existingLesson); // Save the updated lesson and trigger deletion of old associations

        // 4. Associate new/re-associated students
        if (!CollectionUtils.isEmpty(selectedStudentIds)) {
            for (Long studentId : selectedStudentIds) {
                Student student = studentService.getStudentById(studentId);
                
                // Determine payment status: use existing if student was already associated, otherwise UNPAID
                PaymentStatus paymentStatus = existingStudentPaymentStatuses.getOrDefault(studentId, PaymentStatus.UNPAID);

                // Create and save LessonStudent association
                LessonStudent lessonStudent = createLessonStudent(student, updatedLesson, paymentStatus);
                lessonStudentService.save(lessonStudent);
                
                // Add to the lesson's collection for consistency within the transaction
                updatedLesson.getLessonStudents().add(lessonStudent);
            }
        }

        return updatedLesson;
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    private LessonStudent createLessonStudent(Student student, Lesson savedLesson, PaymentStatus paymentStatus) {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setLesson(savedLesson);
        lessonStudent.setStudent(student);
        lessonStudent.setPaymentStatus(paymentStatus);
        return lessonStudent;
    }

    // Overload for saveLesson to maintain consistency
    private LessonStudent createLessonStudent(Student student, Lesson savedLesson) {
        return createLessonStudent(student, savedLesson, PaymentStatus.UNPAID);
    }
}
