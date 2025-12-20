package com.art.tutordesk.lesson.service;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonMapper;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final StudentService studentService;
    private final LessonStudentService lessonStudentService;
    private final BalanceService balanceService;
    private final LessonMapper lessonMapper;

    public List<LessonListDTO> getLessonsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Lesson> lessons = lessonRepository.findByLessonDateBetween(startDate, endDate);
        log.debug("Found {} lessons between {} and {}", lessons.size(), startDate, endDate);
        return lessons.stream()
                .map(lessonMapper::toLessonListDTO)
                .collect(Collectors.toList());
    }

    public LessonProfileDTO getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with id: {}", id);
                    return new RuntimeException("Lesson not found with id: " + id);
                });
        return lessonMapper.toLessonProfileDTO(lesson);
    }

    @Transactional
    public Lesson saveLesson(Lesson lesson, List<Long> selectedStudentIds) {
        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Lesson created: {id={}, date={}, startTime={}, topic={}} with {} students.",
                savedLesson.getId(), savedLesson.getLessonDate(), savedLesson.getStartTime(),
                savedLesson.getTopic(), selectedStudentIds != null ? selectedStudentIds.size() : 0);
        List<Student> students = associateStudentsWithLesson(savedLesson, selectedStudentIds, Map.of());
        students.forEach(student -> balanceService.resyncPaymentStatus(student.getId()));
        return savedLesson;
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson, List<Long> newStudentIds) {
        Lesson existingLesson = lessonRepository.findById(lesson.getId())
                .orElseThrow(() -> {
                    log.warn("Lesson not found for update with id: {}", lesson.getId());
                    return new RuntimeException("Lesson not found for update with id: " + lesson.getId());
                });

        log.info("Updating lesson {}: from date={} topic='{}' to date={} topic='{}'",
                lesson.getId(), existingLesson.getLessonDate(), existingLesson.getTopic(),
                lesson.getLessonDate(), lesson.getTopic());

        Set<Student> allAffectedStudents = new HashSet<>();
        existingLesson.getLessonStudents().forEach(ls -> allAffectedStudents.add(ls.getStudent()));

        Map<Long, PaymentStatus> existingStudentPaymentStatuses = existingLesson.getLessonStudents().stream()
                .collect(Collectors.toMap(
                        lessonStudent -> lessonStudent.getStudent().getId(),
                        LessonStudent::getPaymentStatus,
                        (oldValue, newValue) -> oldValue
                ));

        // Reverse debits for all students who were in the lesson using changeBalance
        existingLesson.getLessonStudents().forEach(ls ->
                balanceService.changeBalance(ls.getStudent().getId(), ls.getCurrency(), ls.getPrice()));

        existingLesson.setLessonDate(lesson.getLessonDate());
        existingLesson.setStartTime(lesson.getStartTime());
        existingLesson.setTopic(lesson.getTopic());

        existingLesson.getLessonStudents().clear();
        lessonRepository.flush(); // Ensure the removal is processed before adding new associations

        List<Student> newStudents = associateStudentsWithLesson(existingLesson, newStudentIds, existingStudentPaymentStatuses);
        allAffectedStudents.addAll(newStudents);
        log.info("Lesson {} updated with {} students. Affected student IDs: {}",
                lesson.getId(), newStudentIds != null ? newStudentIds.size() : 0,
                allAffectedStudents.stream().map(Student::getId).collect(Collectors.toList()));

        // Resync status for all affected students (both old and new)
        allAffectedStudents.forEach(student -> balanceService.resyncPaymentStatus(student.getId()));

        return existingLesson;
    }

    private List<Student> associateStudentsWithLesson(Lesson lesson, List<Long> studentIds, Map<Long, PaymentStatus> existingStatuses) {
        if (CollectionUtils.isEmpty(studentIds)) {
            log.debug("No students selected for lesson {}. No associations to create.", lesson.getId());
            return List.of();
        }

        List<Student> selectedStudents = studentService.getStudentsByIds(studentIds);
        boolean isGroupLesson = selectedStudents.size() > 1;

        for (Student student : selectedStudents) {
            PaymentStatus paymentStatus = existingStatuses.getOrDefault(student.getId(), PaymentStatus.UNPAID);
            LessonStudent lessonStudent = lessonStudentService.buildLessonStudent(student, lesson, paymentStatus);

            java.math.BigDecimal price = isGroupLesson ? student.getPriceGroup() : student.getPriceIndividual();
            lessonStudent.setPrice(price);
            lessonStudent.setCurrency(student.getCurrency()); // Ensure currency is set

            if (price.compareTo(java.math.BigDecimal.ZERO) == 0) {
                lessonStudent.setPaymentStatus(PaymentStatus.FREE);
            } else {
                lessonStudent.setPaymentStatus(paymentStatus);
            }

            LessonStudent savedLessonStudent = lessonStudentService.save(lessonStudent);
            lesson.getLessonStudents().add(savedLessonStudent);
            // Debit the balance for the new lesson association using changeBalance
            balanceService.changeBalance(student.getId(), savedLessonStudent.getCurrency(), savedLessonStudent.getPrice().negate());
            log.debug("Associated student {} with lesson {} at price {} {}. Initial status: {}.",
                    student.getId(), lesson.getId(), savedLessonStudent.getPrice(),
                    savedLessonStudent.getCurrency(), savedLessonStudent.getPaymentStatus());
        }
        return selectedStudents;
    }

    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> {
            log.warn("Lesson not found for deletion with id: {}", id);
            return new RuntimeException("Lesson not found for deletion with id: " + id);
        });
        log.info("Deleting lesson: {id={}, date={}, startTime={}, topic='{}'}",
                lesson.getId(), lesson.getLessonDate(), lesson.getStartTime(), lesson.getTopic());

        Set<Student> affectedStudents = lesson.getLessonStudents().stream()
                .map(LessonStudent::getStudent)
                .collect(Collectors.toSet());

        // Reverse the debit for each student in the lesson using changeBalance
        lesson.getLessonStudents().forEach(ls ->
                balanceService.changeBalance(ls.getStudent().getId(), ls.getCurrency(), ls.getPrice()));

        lessonRepository.deleteById(id);
        log.info("Lesson with ID {} deleted.", id);

        // After deletion, resync the status for all affected students
        affectedStudents.forEach(student -> balanceService.resyncPaymentStatus(student.getId()));
    }
}

