package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentStatus;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.dto.LessonStudentUpdateDTO;
import com.art.tutordesk.lesson.dto.LessonUpdateForm;
import com.art.tutordesk.lesson.mapper.LessonMapper;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final StudentService studentService;
    private final LessonStudentService lessonStudentService;
    private final LessonMapper lessonMapper;
    private final LessonBalanceService lessonBalanceService;

    public List<LessonListDTO> getLessonsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Lesson> lessons = lessonRepository.findByLessonDateBetween(startDate, endDate);
        return lessons.stream()
                .map(lessonMapper::toLessonListDTO)
                .collect(Collectors.toList());
    }

    public LessonProfileDTO getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
        return lessonMapper.toLessonProfileDTO(lesson);
    }

    @Transactional
    public Lesson saveLesson(Lesson lesson, List<Long> selectedStudentIds) {
        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Lesson created: {id={}, date={}} with {} students.",
                savedLesson.getId(), savedLesson.getLessonDate(), selectedStudentIds != null ? selectedStudentIds.size() : 0);
        associateStudentsWithLesson(savedLesson, selectedStudentIds);
        return savedLesson;
    }

    @Transactional
    public void updateLesson(Long lessonId, LessonUpdateForm form) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found for update with id: " + lessonId));

        existingLesson.setLessonDate(form.getLessonDate());

        Map<Long, LessonStudent> existingAssociations = existingLesson.getLessonStudents().stream()
                .collect(Collectors.toMap(ls -> ls.getStudent().getId(), ls -> ls));

        Map<Long, LessonStudentUpdateDTO> updates = form.getStudentUpdates().stream()
                .collect(Collectors.toMap(LessonStudentUpdateDTO::getStudentId, update -> update));

        // Remove students who are no longer in the lesson
        Set<Long> studentsToRemove = existingAssociations.keySet().stream()
                .filter(studentId -> !updates.containsKey(studentId))
                .collect(Collectors.toSet());

        for (Long studentId : studentsToRemove) {
            LessonStudent ls = existingAssociations.get(studentId);
            lessonBalanceService.adjustBalanceForPriceAndStatusChange(ls, ls.getPrice(), LessonStudentStatus.CANCELED); // Treat removal as cancellation
            existingLesson.getLessonStudents().remove(ls);
            lessonStudentService.delete(ls);
            log.info("Removed student {} from lesson {}", studentId, lessonId);
        }

        // Update existing students and add new ones
        boolean isGroupLesson = updates.size() > 1;
        for (LessonStudentUpdateDTO update : form.getStudentUpdates()) {
            LessonStudent existingLs = existingAssociations.get(update.getStudentId());

            if (existingLs != null) {
                // Student already in lesson, update price and status if changed
                BigDecimal newPrice = isGroupLesson ? existingLs.getStudent().getPriceGroup() : existingLs.getStudent().getPriceIndividual();
                lessonBalanceService.adjustBalanceForPriceAndStatusChange(existingLs, newPrice, update.getStatus());
            } else {
                // New student for this lesson
                Student student = studentService.getStudentEntityById(update.getStudentId());
                addNewStudentToLesson(existingLesson, student, update.getStatus(), isGroupLesson);
            }
        }
        lessonRepository.save(existingLesson);
    }

    private void associateStudentsWithLesson(Lesson lesson, List<Long> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }
        List<Student> selectedStudents = studentService.getStudentsByIds(studentIds);
        boolean isGroupLesson = selectedStudents.size() > 1;

        for (Student student : selectedStudents) {
            addNewStudentToLesson(lesson, student, LessonStudentStatus.SCHEDULED, isGroupLesson);
        }
    }

    private void addNewStudentToLesson(Lesson lesson, Student student, LessonStudentStatus status, boolean isGroupLesson) {
        LessonStudent lessonStudent = lessonStudentService.buildLessonStudent(student, lesson);

        BigDecimal price = isGroupLesson ? student.getPriceGroup() : student.getPriceIndividual();
        lessonStudent.setPrice(price);
        lessonStudent.setCurrency(student.getCurrency());
        // Start with SCHEDULED so handlePriceAndStatusChange correctly applies initial charges if needed
        lessonStudent.setStatus(LessonStudentStatus.SCHEDULED);

        lessonStudentService.save(lessonStudent);
        lesson.getLessonStudents().add(lessonStudent);

        // Immediately handle charge if added with a chargeable status
        lessonBalanceService.adjustBalanceForPriceAndStatusChange(lessonStudent, price, status);
        log.info("Added student {} to lesson {} with status {}", student.getId(), lesson.getId(), status);
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
        log.info("Lesson with ID {} deleted.", id);
    }
}