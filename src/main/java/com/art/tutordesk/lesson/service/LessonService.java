package com.art.tutordesk.lesson.service;

import com.art.tutordesk.events.LessonStudentCreatedEvent;
import com.art.tutordesk.events.LessonStudentDeletedEvent;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonRepository;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.PaymentStatusUtil;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final PaymentStatusUtil paymentStatusUtil;
    private final ApplicationEventPublisher eventPublisher;

    public List<Lesson> getAllLessonsSorted() {
        List<Lesson> lessons = lessonRepository.findAllWithStudentsSorted();
        lessons.forEach(lesson -> {
            PaymentStatus paymentStatus = paymentStatusUtil.calculateAndSetLessonPaymentStatus(lesson.getLessonStudents());
            lesson.setPaymentStatus(paymentStatus);
        });
        return lessons;
    }

    public Lesson getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
        PaymentStatus paymentStatus = paymentStatusUtil.calculateAndSetLessonPaymentStatus(lesson.getLessonStudents());
        lesson.setPaymentStatus(paymentStatus);
        return lesson;
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
                LessonStudent lessonStudent = lessonStudentService.createLessonStudent(student, savedLesson, PaymentStatus.UNPAID);
                eventPublisher.publishEvent(new LessonStudentCreatedEvent(lessonStudent));
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
                        lessonStudent -> lessonStudent.getStudent().getId(),
                        LessonStudent::getPaymentStatus,
                        (oldValue, newValue) -> oldValue // Handle duplicate student IDs if somehow present, keep old status
                ));

        // 2. Update fields from the provided lesson object
        existingLesson.setLessonDate(lesson.getLessonDate());
        existingLesson.setStartTime(lesson.getStartTime());
        existingLesson.setTopic(lesson.getTopic());

        // 3. Publish deletion events before clearing the associations
        existingLesson.getLessonStudents().forEach(lessonStudent ->
                eventPublisher.publishEvent(new LessonStudentDeletedEvent(lessonStudent)));

        // Clear existing lessonStudents associations.
        // Due to orphanRemoval=true, these will be deleted from the database.
        existingLesson.getLessonStudents().clear();
        Lesson updatedLesson = lessonRepository.save(existingLesson); // Save the updated lesson and trigger deletion of old associations

        // 4. Associate new/re-associated students
        if (!CollectionUtils.isEmpty(selectedStudentIds)) {
            for (Long studentId : selectedStudentIds) {
                Student student = studentService.getStudentById(studentId);

                // Determine payment status: use existing if student was already associated, otherwise UNPAID
                PaymentStatus paymentStatus = existingStudentPaymentStatuses.getOrDefault(studentId, PaymentStatus.UNPAID);

                // Create and save LessonStudent association
                LessonStudent newLessonStudent = lessonStudentService.createLessonStudent(student, updatedLesson, paymentStatus);
                eventPublisher.publishEvent(new LessonStudentCreatedEvent(newLessonStudent));
            }
        }

        return updatedLesson;
    }

    @Transactional
    public void deleteLesson(Long id) {
        // First, fetch the lesson to get its students for event publishing
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found for deletion with id: " + id));

        // Publish deletion events for each student in the lesson
        lesson.getLessonStudents().forEach(lessonStudent ->
                eventPublisher.publishEvent(new LessonStudentDeletedEvent(lessonStudent)));

        lessonRepository.deleteById(id);
    }

    //todo refactoring this service
}
