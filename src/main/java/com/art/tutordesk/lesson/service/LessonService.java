package com.art.tutordesk.lesson.service;

import com.art.tutordesk.events.LessonStudentCreatedEvent;
import com.art.tutordesk.events.LessonStudentDeletedEvent;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonRepository;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.LessonListDTO;
import com.art.tutordesk.lesson.LessonProfileDTO;
import com.art.tutordesk.lesson.mapper.LessonMapper;
import com.art.tutordesk.student.StudentService;
import com.art.tutordesk.student.Student;
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
    private final ApplicationEventPublisher eventPublisher;
    private final LessonMapper lessonMapper;

    public List<LessonListDTO> getAllLessonsSorted() {
        List<Lesson> lessons = lessonRepository.findAllWithStudentsSorted();
        return lessons.stream()
                .map(lessonMapper::toLessonListDTO)
                .collect(Collectors.toList());
    }

    public LessonProfileDTO getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
        return lessonMapper.toLessonProfileDTO(lesson);
    }

    @Transactional
    public Lesson saveLesson(Lesson lesson, List<Long> selectedStudentIds) {
        Lesson savedLesson = lessonRepository.save(lesson);
        associateStudentsWithLesson(savedLesson, selectedStudentIds, Map.of());
        return savedLesson;
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson, List<Long> selectedStudentIds) {
        Lesson existingLesson = lessonRepository.findById(lesson.getId())
                .orElseThrow(() -> new RuntimeException("Lesson not found for update with id: " + lesson.getId()));

        Map<Long, PaymentStatus> existingStudentPaymentStatuses = existingLesson.getLessonStudents().stream()
                .collect(Collectors.toMap(
                        lessonStudent -> lessonStudent.getStudent().getId(),
                        LessonStudent::getPaymentStatus,
                        (oldValue, newValue) -> oldValue
                ));

        existingLesson.setLessonDate(lesson.getLessonDate());
        existingLesson.setStartTime(lesson.getStartTime());
        existingLesson.setTopic(lesson.getTopic());

        existingLesson.getLessonStudents().forEach(lessonStudent ->
                eventPublisher.publishEvent(new LessonStudentDeletedEvent(lessonStudent)));

        existingLesson.getLessonStudents().clear();
        lessonRepository.flush();

        associateStudentsWithLesson(existingLesson, selectedStudentIds, existingStudentPaymentStatuses);

        return existingLesson;
    }

    private void associateStudentsWithLesson(Lesson lesson, List<Long> studentIds, Map<Long, PaymentStatus> existingStatuses) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }

        List<Student> selectedStudents = studentService.getStudentsByIds(studentIds);
        boolean isGroupLesson = selectedStudents.size() > 1;

        for (Student student : selectedStudents) {
            PaymentStatus paymentStatus = existingStatuses.getOrDefault(student.getId(), PaymentStatus.UNPAID);
            LessonStudent lessonStudent = lessonStudentService.buildLessonStudent(student, lesson, paymentStatus);

            java.math.BigDecimal price = isGroupLesson ? student.getPriceGroup() : student.getPriceIndividual();
            lessonStudent.setPrice(price);

            if (price.compareTo(java.math.BigDecimal.ZERO) == 0) {
                lessonStudent.setPaymentStatus(PaymentStatus.FREE);
            }

            LessonStudent savedLessonStudent = lessonStudentService.save(lessonStudent);
            lesson.getLessonStudents().add(savedLessonStudent);
            eventPublisher.publishEvent(new LessonStudentCreatedEvent(savedLessonStudent));
        }
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
