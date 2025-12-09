package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonRepository;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.PaymentStatusUtil;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private StudentService studentService;
    @Mock
    private LessonStudentService lessonStudentService;
    @Mock
    private PaymentStatusUtil paymentStatusUtil;

    @InjectMocks
    private LessonService lessonService;

    private Lesson lesson1;
    private Student student1;
    // lessonStudent1 is now managed within specific test cases if needed

    @BeforeEach
    void setUp() {
        student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");

        lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setLessonDate(LocalDate.now());
        lesson1.setStartTime(LocalTime.now());
        lesson1.setTopic("Math");
        // Initialize lessonStudents as an empty set in setUp to avoid interference
        lesson1.setLessonStudents(new HashSet<>());
    }

    // Test cases for getAllLessonsSorted
    @Test
    void getAllLessonsSorted_shouldReturnEmptyList_whenNoLessonsExist() {
        when(lessonRepository.findAllWithStudentsSorted()).thenReturn(Collections.emptyList());

        List<Lesson> result = lessonService.getAllLessonsSorted();

        assertTrue(result.isEmpty());
        verify(lessonRepository, times(1)).findAllWithStudentsSorted();
        verify(paymentStatusUtil, never()).calculateAndSetLessonPaymentStatus(anySet());
    }

    @Test
    void getAllLessonsSorted_shouldReturnLessonsWithCalculatedStatus_whenLessonsExist() {
        // Setup initial lesson1 (e.g., one student PAID)
        LessonStudent ls1 = new LessonStudent();
        ls1.setStudent(student1);
        ls1.setLesson(lesson1);
        ls1.setPaymentStatus(PaymentStatus.PAID);
        lesson1.getLessonStudents().add(ls1);


        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setLessonDate(LocalDate.now().plusDays(1));
        lesson2.setStartTime(LocalTime.now());
        lesson2.setTopic("Physics");
        lesson2.setLessonStudents(new HashSet<>()); // Ensure lesson2 has its own students

        LessonStudent ls2 = new LessonStudent();
        ls2.setStudent(student1); // Reusing student1 for simplicity, but could be a new one
        ls2.setLesson(lesson2);
        ls2.setPaymentStatus(PaymentStatus.UNPAID);
        lesson2.getLessonStudents().add(ls2);


        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);
        when(lessonRepository.findAllWithStudentsSorted()).thenReturn(lessons);
        
        // Mocking paymentStatusUtil behavior
        when(paymentStatusUtil.calculateAndSetLessonPaymentStatus(lesson1.getLessonStudents()))
            .thenReturn(PaymentStatus.PAID);
        when(paymentStatusUtil.calculateAndSetLessonPaymentStatus(lesson2.getLessonStudents()))
            .thenReturn(PaymentStatus.UNPAID);

        List<Lesson> result = lessonService.getAllLessonsSorted();

        assertEquals(2, result.size());
        assertEquals(PaymentStatus.PAID, result.get(0).getPaymentStatus()); // Corrected getter
        assertEquals(PaymentStatus.UNPAID, result.get(1).getPaymentStatus()); // Corrected getter
        
        verify(lessonRepository, times(1)).findAllWithStudentsSorted();
        verify(paymentStatusUtil, times(1)).calculateAndSetLessonPaymentStatus(lesson1.getLessonStudents());
        verify(paymentStatusUtil, times(1)).calculateAndSetLessonPaymentStatus(lesson2.getLessonStudents());
    }

    // Test cases for getLessonById
    @Test
    void getLessonById_shouldThrowException_whenLessonNotFound() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> lessonService.getLessonById(1L));
        verify(lessonRepository, times(1)).findById(anyLong());
        verify(paymentStatusUtil, never()).calculateAndSetLessonPaymentStatus(anySet());
    }

    @Test
    void getLessonById_shouldReturnLessonWithCalculatedStatus_whenLessonFound() {
        // Setup initial lesson1 with a student
        LessonStudent ls1 = new LessonStudent();
        ls1.setStudent(student1);
        ls1.setLesson(lesson1);
        ls1.setPaymentStatus(PaymentStatus.PAID);
        lesson1.getLessonStudents().add(ls1);

        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1));
        when(paymentStatusUtil.calculateAndSetLessonPaymentStatus(lesson1.getLessonStudents()))
            .thenReturn(PaymentStatus.PAID);

        Lesson result = lessonService.getLessonById(1L);

        assertNotNull(result);
        assertEquals(lesson1.getId(), result.getId());
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus()); // Corrected getter
        verify(lessonRepository, times(1)).findById(anyLong());
        verify(paymentStatusUtil, times(1)).calculateAndSetLessonPaymentStatus(lesson1.getLessonStudents());
    }

    // Test cases for saveLesson
    @Test
    void saveLesson_shouldSaveLessonWithoutStudents_whenNoStudentsSelected() {
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1)); // For refresh

        Lesson result = lessonService.saveLesson(new Lesson(), Collections.emptyList());

        assertNotNull(result);
        assertEquals(lesson1.getId(), result.getId());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(studentService, never()).getStudentById(anyLong());
        verify(lessonStudentService, never()).createLessonStudent(any(Student.class), any(Lesson.class), any(PaymentStatus.class));
    }

    @Test
    void saveLesson_shouldSaveLessonWithStudents_whenStudentsSelected() {
        Student student2 = new Student();
        student2.setId(2L);
        List<Long> selectedStudentIds = Arrays.asList(student1.getId(), student2.getId());

        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);
        when(studentService.getStudentById(student1.getId())).thenReturn(student1);
        when(studentService.getStudentById(student2.getId())).thenReturn(student2);
        // Mocking createLessonStudent and returning some LessonStudent
        when(lessonStudentService.createLessonStudent(any(Student.class), any(Lesson.class), any(PaymentStatus.class)))
            .thenReturn(new LessonStudent());
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1)); // For refresh

        Lesson result = lessonService.saveLesson(new Lesson(), selectedStudentIds);

        assertNotNull(result);
        assertEquals(lesson1.getId(), result.getId());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(studentService, times(1)).getStudentById(student1.getId());
        verify(studentService, times(1)).getStudentById(student2.getId());
        verify(lessonStudentService, times(2)).createLessonStudent(any(Student.class), any(Lesson.class), eq(PaymentStatus.UNPAID));
    }

    // Test cases for updateLesson
    @Test
    void updateLesson_shouldThrowException_whenLessonNotFound() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> lessonService.updateLesson(lessonToUpdate, Collections.emptyList()));
        verify(lessonRepository, times(1)).findById(anyLong());
    }

    @Test
    void updateLesson_shouldUpdateLessonWithoutStudents_whenNoStudentsSelected() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        lessonToUpdate.setLessonDate(LocalDate.now().plusDays(5));
        lessonToUpdate.setTopic("Updated Topic");

        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);

        Lesson result = lessonService.updateLesson(lessonToUpdate, Collections.emptyList());

        assertNotNull(result);
        assertEquals(lesson1.getId(), result.getId());
        assertEquals(lessonToUpdate.getTopic(), lesson1.getTopic()); // Verify update
        assertTrue(lesson1.getLessonStudents().isEmpty()); // Verify clear
        verify(lessonRepository, times(1)).findById(anyLong());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(lessonStudentService, never()).createLessonStudent(any(Student.class), any(Lesson.class), any(PaymentStatus.class));
    }

    @Test
    void updateLesson_shouldUpdateLessonWithStudents_whenStudentsSelectedAndExistingPaymentsPreserved() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        lessonToUpdate.setLessonDate(LocalDate.now().plusDays(5));
        lessonToUpdate.setStartTime(LocalTime.now()); 
        lessonToUpdate.setTopic("Updated Topic");

        Student student2 = new Student(); // This student will have an existing PAID status
        student2.setId(2L);
        
        // Create an existing LessonStudent for student2 within initialLesson1
        LessonStudent existingLessonStudentForStudent2 = new LessonStudent();
        existingLessonStudentForStudent2.setStudent(student2);
        existingLessonStudentForStudent2.setPaymentStatus(PaymentStatus.PAID);
        
        // Ensure initialLesson1 contains only existingLessonStudentForStudent2
        Lesson initialLesson1 = new Lesson();
        initialLesson1.setId(1L);
        initialLesson1.setLessonDate(LocalDate.now());
        initialLesson1.setStartTime(LocalTime.now());
        initialLesson1.setTopic("Initial Topic");
        initialLesson1.setLessonStudents(new HashSet<>(Collections.singletonList(existingLessonStudentForStudent2)));

        List<Long> selectedStudentIds = Arrays.asList(student1.getId(), student2.getId()); // student1 is new, student2 is existing

        when(lessonRepository.findById(eq(1L))).thenReturn(Optional.of(initialLesson1)); // Return the lesson with existing students
        when(lessonRepository.save(any(Lesson.class))).thenReturn(initialLesson1); // Save returns the same lesson
        when(studentService.getStudentById(eq(student1.getId()))).thenReturn(student1);
        when(studentService.getStudentById(eq(student2.getId()))).thenReturn(student2);
        
        // Mock the createLessonStudent service call
        when(lessonStudentService.createLessonStudent(any(Student.class), any(Lesson.class), any(PaymentStatus.class)))
            .thenReturn(new LessonStudent());

        lessonService.updateLesson(lessonToUpdate, selectedStudentIds);

        assertNotNull(initialLesson1); // Check initialLesson1, as updatedLesson is the same object
        assertEquals(lessonToUpdate.getId(), initialLesson1.getId());
        assertEquals(lessonToUpdate.getTopic(), initialLesson1.getTopic()); // Verify update
        
        verify(lessonRepository, times(1)).findById(eq(1L)); // Verify findById with specific ID
        verify(lessonRepository, times(1)).save(eq(initialLesson1)); // Verify save with the initialLesson1 object
        verify(studentService, times(1)).getStudentById(eq(student1.getId()));
        verify(studentService, times(1)).getStudentById(eq(student2.getId()));
        
        // Verify createLessonStudent is called for student1 with UNPAID (new association)
        verify(lessonStudentService, times(1)).createLessonStudent(eq(student1), eq(initialLesson1), eq(PaymentStatus.UNPAID));
        // Verify createLessonStudent is called for student2 with PAID (existing association, status preserved)
        verify(lessonStudentService, times(1)).createLessonStudent(eq(student2), eq(initialLesson1), eq(PaymentStatus.PAID));
    }

    // Test cases for deleteLesson
    @Test
    void deleteLesson_shouldCallRepositoryDeleteById() {
        lessonService.deleteLesson(1L);
        verify(lessonRepository, times(1)).deleteById(1L);
    }
}