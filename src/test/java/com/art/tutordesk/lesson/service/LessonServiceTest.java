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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void saveLesson_shouldSaveLessonWithStudents_andPublishEvents() {
        Student student2 = new Student();
        student2.setId(2L);
        List<Long> selectedStudentIds = Arrays.asList(student1.getId(), student2.getId());

        // Mock the returned LessonStudent objects
        LessonStudent ls1 = new LessonStudent(null, lesson1, student1, PaymentStatus.UNPAID, null, null);
        LessonStudent ls2 = new LessonStudent(null, lesson1, student2, PaymentStatus.UNPAID, null, null);

        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);
        when(studentService.getStudentById(student1.getId())).thenReturn(student1);
        when(studentService.getStudentById(student2.getId())).thenReturn(student2);
        when(lessonStudentService.createLessonStudent(eq(student1), any(Lesson.class), any(PaymentStatus.class))).thenReturn(ls1);
        when(lessonStudentService.createLessonStudent(eq(student2), any(Lesson.class), any(PaymentStatus.class))).thenReturn(ls2);
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1)); // For refresh

        lessonService.saveLesson(new Lesson(), selectedStudentIds);

        // Verify that events were published for each created LessonStudent
        ArgumentCaptor<LessonStudentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(LessonStudentCreatedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<LessonStudentCreatedEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());
        assertTrue(capturedEvents.stream().anyMatch(event -> event.getLessonStudent().getStudent().equals(student1)));
        assertTrue(capturedEvents.stream().anyMatch(event -> event.getLessonStudent().getStudent().equals(student2)));
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
    void updateLesson_shouldPublishDeleteEvent_whenStudentsAreRemoved() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        lessonToUpdate.setLessonDate(LocalDate.now().plusDays(5));
        lessonToUpdate.setTopic("Updated Topic");

        // Setup: existing lesson has one student
        LessonStudent existingLs = new LessonStudent(1L, lesson1, student1, PaymentStatus.PAID, null, null);
        lesson1.getLessonStudents().add(existingLs);

        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);

        // Action: update with no students
        lessonService.updateLesson(lessonToUpdate, Collections.emptyList());

        // Verify a delete event was published
        ArgumentCaptor<LessonStudentDeletedEvent> eventCaptor = ArgumentCaptor.forClass(LessonStudentDeletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(student1, eventCaptor.getValue().getLessonStudent().getStudent());

        // Verify no create events were published
        verify(eventPublisher, never()).publishEvent(any(LessonStudentCreatedEvent.class));
        assertTrue(lesson1.getLessonStudents().isEmpty());
    }

    @Test
    void updateLesson_shouldPublishCreateAndDeleteEvents() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        lessonToUpdate.setTopic("Updated Topic");

        // Existing students in the lesson
        Student studentToRemove = new Student();
        studentToRemove.setId(2L);
        Student studentToKeep = new Student();
        studentToKeep.setId(3L);

        LessonStudent lsToRemove = new LessonStudent(2L, lesson1, studentToRemove, PaymentStatus.UNPAID, null, null);
        LessonStudent lsToKeep = new LessonStudent(3L, lesson1, studentToKeep, PaymentStatus.PAID, null, null);
        lesson1.setLessonStudents(new HashSet<>(Arrays.asList(lsToRemove, lsToKeep)));

        // New student to add
        Student studentToAdd = student1; // student1 from setUp
        List<Long> updatedStudentIds = Arrays.asList(studentToKeep.getId(), studentToAdd.getId()); // Keep one, add one

        LessonStudent createdLs = new LessonStudent(4L, lesson1, studentToAdd, PaymentStatus.UNPAID, null, null);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);
        when(studentService.getStudentById(studentToAdd.getId())).thenReturn(studentToAdd);
        when(studentService.getStudentById(studentToKeep.getId())).thenReturn(studentToKeep);
        when(lessonStudentService.createLessonStudent(eq(studentToAdd), any(Lesson.class), any(PaymentStatus.class))).thenReturn(createdLs);
        when(lessonStudentService.createLessonStudent(eq(studentToKeep), any(Lesson.class), any(PaymentStatus.class))).thenReturn(lsToKeep);


        lessonService.updateLesson(lessonToUpdate, updatedStudentIds);

        // Verify delete event for the removed student
        ArgumentCaptor<LessonStudentDeletedEvent> deleteCaptor = ArgumentCaptor.forClass(LessonStudentDeletedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(deleteCaptor.capture()); // Captures all delete events
        assertTrue(deleteCaptor.getAllValues().stream().anyMatch(e -> e.getLessonStudent().getStudent().equals(studentToRemove)));
        assertTrue(deleteCaptor.getAllValues().stream().anyMatch(e -> e.getLessonStudent().getStudent().equals(studentToKeep)));


        // Verify create event for the added student
        ArgumentCaptor<LessonStudentCreatedEvent> createCaptor = ArgumentCaptor.forClass(LessonStudentCreatedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(createCaptor.capture()); // Captures all create events
        assertTrue(createCaptor.getAllValues().stream().anyMatch(e -> e.getLessonStudent().getStudent().equals(studentToAdd)));
        assertTrue(createCaptor.getAllValues().stream().anyMatch(e -> e.getLessonStudent().getStudent().equals(studentToKeep)));
    }

    // Test cases for deleteLesson
    @Test
    void deleteLesson_shouldPublishDeleteEventsAndCallRepository() {
        // Setup: Lesson has two students
        Student student2 = new Student();
        student2.setId(2L);
        LessonStudent ls1 = new LessonStudent(1L, lesson1, student1, PaymentStatus.PAID, null, null);
        LessonStudent ls2 = new LessonStudent(2L, lesson1, student2, PaymentStatus.UNPAID, null, null);
        lesson1.setLessonStudents(new HashSet<>(Arrays.asList(ls1, ls2)));

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));

        lessonService.deleteLesson(1L);

        // Verify that delete events were published for both students
        verify(eventPublisher, times(2)).publishEvent(any(LessonStudentDeletedEvent.class));
        // Verify the repository delete method was called
        verify(lessonRepository, times(1)).deleteById(1L);
    }
}