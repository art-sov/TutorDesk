package com.art.tutordesk.lesson.service;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonMapper;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private BalanceService balanceService;
    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private LessonService lessonService;

    private Lesson lesson1;
    private Student student1;
    private Student student2;
    private Student student3;

    @BeforeEach
    void setUp() {
        student1 = createStudent(1L, "John", "Doe", new BigDecimal("25.00"), new BigDecimal("20.00"), Currency.USD);
        student2 = createStudent(2L, "Jane", "Smith", new BigDecimal("30.00"), new BigDecimal("24.00"), Currency.PLN);
        student3 = createStudent(3L, "Charlie", "Brown", new BigDecimal("20.00"), new BigDecimal("15.00"), Currency.USD);

        lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setLessonDate(LocalDate.now());
        lesson1.setStartTime(LocalTime.now());
        lesson1.setTopic("Math");
        lesson1.setLessonStudents(new HashSet<>());
    }

    // Test cases for getAllLessonsSorted
    @Test
    void getAllLessonsSorted_shouldReturnEmptyList_whenNoLessonsExist() {
        when(lessonRepository.findAllWithStudentsSorted()).thenReturn(Collections.emptyList());

        List<LessonListDTO> result = lessonService.getAllLessonsSorted();

        assertTrue(result.isEmpty());
        verify(lessonRepository, times(1)).findAllWithStudentsSorted();
        verify(lessonMapper, never()).toLessonListDTO(any(Lesson.class));
    }

    @Test
    void getAllLessonsSorted_shouldReturnLessonsWithCalculatedStatus_whenLessonsExist() {
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setLessonDate(LocalDate.now().plusDays(1));
        lesson2.setStartTime(LocalTime.now());
        lesson2.setTopic("Physics");
        lesson2.setLessonStudents(new HashSet<>());

        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);
        when(lessonRepository.findAllWithStudentsSorted()).thenReturn(lessons);

        // Mock mapper behavior
        LessonListDTO dto1 = new LessonListDTO();
        dto1.setId(lesson1.getId());
        dto1.setPaymentStatus(PaymentStatus.PAID);
        LessonListDTO dto2 = new LessonListDTO();
        dto2.setId(lesson2.getId());
        dto2.setPaymentStatus(PaymentStatus.UNPAID);

        when(lessonMapper.toLessonListDTO(lesson1)).thenReturn(dto1);
        when(lessonMapper.toLessonListDTO(lesson2)).thenReturn(dto2);

        List<LessonListDTO> result = lessonService.getAllLessonsSorted();

        assertEquals(2, result.size());
        assertEquals(PaymentStatus.PAID, result.get(0).getPaymentStatus());
        assertEquals(PaymentStatus.UNPAID, result.get(1).getPaymentStatus());

        verify(lessonRepository, times(1)).findAllWithStudentsSorted();
        verify(lessonMapper, times(1)).toLessonListDTO(lesson1);
        verify(lessonMapper, times(1)).toLessonListDTO(lesson2);
    }

    // Test cases for getLessonById
    @Test
    void getLessonById_shouldThrowException_whenLessonNotFound() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> lessonService.getLessonById(1L));
        verify(lessonRepository, times(1)).findById(anyLong());
        verify(lessonMapper, never()).toLessonProfileDTO(any(Lesson.class));
    }

    @Test
    void getLessonById_shouldReturnLessonWithCalculatedStatus_whenLessonFound() {
        // Setup initial lesson1 with a student
        LessonStudent ls1 = createLessonStudent(10L, lesson1, student1, PaymentStatus.PAID, student1.getPriceIndividual(), student1.getCurrency());
        lesson1.getLessonStudents().add(ls1);

        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1));

        // Mock mapper behavior
        LessonProfileDTO dto = new LessonProfileDTO();
        dto.setId(lesson1.getId());
        dto.setPaymentStatus(PaymentStatus.PAID);

        when(lessonMapper.toLessonProfileDTO(lesson1)).thenReturn(dto);

        LessonProfileDTO result = lessonService.getLessonById(1L);

        assertNotNull(result);
        assertEquals(lesson1.getId(), result.getId());
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
        verify(lessonRepository, times(1)).findById(anyLong());
        verify(lessonMapper, times(1)).toLessonProfileDTO(lesson1);
    }

    // Test cases for saveLesson
    @Test
    void saveLesson_shouldSaveLessonWithoutStudents_whenNoStudentsSelected() {
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);

        Lesson result = lessonService.saveLesson(new Lesson(), Collections.emptyList());

        assertNotNull(result);
        assertEquals(lesson1.getId(), result.getId());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(studentService, never()).getStudentsByIds(any());
        verify(lessonStudentService, never()).buildLessonStudent(any(Student.class), any(Lesson.class), any(PaymentStatus.class));
        verify(lessonStudentService, never()).save(any(LessonStudent.class));
        verify(balanceService, never()).changeBalance(anyLong(), any(Currency.class), any(BigDecimal.class));
        verify(balanceService, never()).resyncPaymentStatus(anyLong());
    }

    @Test
    void saveLesson_shouldSaveLessonWithOneStudent_individualPricing() {
        List<Long> selectedStudentIds = Collections.singletonList(student1.getId());

        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);
        when(studentService.getStudentsByIds(selectedStudentIds)).thenReturn(Collections.singletonList(student1));

        LessonStudent ls = new LessonStudent();
        ls.setStudent(student1);
        ls.setLesson(lesson1);
        ls.setCurrency(student1.getCurrency());
        ls.setPrice(student1.getPriceIndividual());
        when(lessonStudentService.buildLessonStudent(eq(student1), eq(lesson1), any(PaymentStatus.class)))
                .thenReturn(ls);
        when(lessonStudentService.save(any(LessonStudent.class))).thenReturn(ls);

        lessonService.saveLesson(new Lesson(), selectedStudentIds);

        ArgumentCaptor<LessonStudent> lessonStudentCaptor = ArgumentCaptor.forClass(LessonStudent.class);

        verify(lessonStudentService, times(1)).save(lessonStudentCaptor.capture());
        verify(balanceService, times(1)).changeBalance(eq(student1.getId()), eq(student1.getCurrency()), eq(student1.getPriceIndividual().negate()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(student1.getId()));
        assertEquals(student1.getPriceIndividual(), lessonStudentCaptor.getValue().getPrice());
        assertEquals(PaymentStatus.UNPAID, lessonStudentCaptor.getValue().getPaymentStatus());
    }

    @Test
    void saveLesson_shouldSaveLessonWithMultipleStudents_groupPricing() {
        List<Long> selectedStudentIds = Arrays.asList(student1.getId(), student2.getId());

        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson1);
        when(studentService.getStudentsByIds(selectedStudentIds)).thenReturn(Arrays.asList(student1, student2));

        // Mock buildLessonStudent for student1
        LessonStudent ls1 = new LessonStudent();
        ls1.setStudent(student1);
        ls1.setLesson(lesson1);
        ls1.setCurrency(student1.getCurrency());
        ls1.setPrice(student1.getPriceGroup());
        when(lessonStudentService.buildLessonStudent(eq(student1), eq(lesson1), any(PaymentStatus.class))).thenReturn(ls1);
        when(lessonStudentService.save(ls1)).thenReturn(ls1);

        // Mock buildLessonStudent for student2
        LessonStudent ls2 = new LessonStudent();
        ls2.setStudent(student2);
        ls2.setLesson(lesson1);
        ls2.setCurrency(student2.getCurrency());
        ls2.setPrice(student2.getPriceGroup());
        when(lessonStudentService.buildLessonStudent(eq(student2), eq(lesson1), any(PaymentStatus.class))).thenReturn(ls2);
        when(lessonStudentService.save(ls2)).thenReturn(ls2);


        lessonService.saveLesson(new Lesson(), selectedStudentIds);

        ArgumentCaptor<LessonStudent> lessonStudentCaptor = ArgumentCaptor.forClass(LessonStudent.class);
        verify(lessonStudentService, times(2)).save(lessonStudentCaptor.capture());

        List<LessonStudent> capturedLessonStudents = lessonStudentCaptor.getAllValues();
        Optional<LessonStudent> first = capturedLessonStudents.stream().filter(lss -> lss.getStudent().equals(student1)).findFirst();
        Optional<LessonStudent> second = capturedLessonStudents.stream().filter(lss -> lss.getStudent().equals(student2)).findFirst();

        assertTrue(first.isPresent());
        assertEquals(student1.getPriceGroup(), first.get().getPrice());

        assertTrue(second.isPresent());
        assertEquals(student2.getPriceGroup(), second.get().getPrice());

        verify(balanceService, times(1)).changeBalance(eq(student1.getId()), eq(student1.getCurrency()), eq(student1.getPriceGroup().negate()));
        verify(balanceService, times(1)).changeBalance(eq(student2.getId()), eq(student2.getCurrency()), eq(student2.getPriceGroup().negate()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(student1.getId()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(student2.getId()));
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
    void updateLesson_shouldReverseDebitAndResync_whenStudentsAreRemoved() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        lessonToUpdate.setLessonDate(LocalDate.now().plusDays(5));
        lessonToUpdate.setTopic("Updated Topic");

        // Setup: existing lesson has one student
        LessonStudent existingLs = createLessonStudent(10L, lesson1, student1, PaymentStatus.PAID, student1.getPriceIndividual(), student1.getCurrency());
        lesson1.getLessonStudents().add(existingLs);

        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson1));

        lessonService.updateLesson(lessonToUpdate, Collections.emptyList());

        verify(balanceService, times(1)).changeBalance(eq(student1.getId()), eq(student1.getCurrency()), eq(student1.getPriceIndividual()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(student1.getId()));
        assertTrue(lesson1.getLessonStudents().isEmpty());
    }

    @Test
    void updateLesson_shouldHandleStudentChangesAndPricing() {
        Lesson lessonToUpdate = new Lesson();
        lessonToUpdate.setId(1L);
        lessonToUpdate.setTopic("Updated Topic");

        // Existing students in the lesson
        Student studentToRemove = student3; // student3 from setUp
        Student studentToKeep = student2; // student2 from setUp

        LessonStudent lsToRemove = createLessonStudent(10L, lesson1, studentToRemove, PaymentStatus.UNPAID, studentToRemove.getPriceIndividual(), studentToRemove.getCurrency());
        LessonStudent lsToKeep = createLessonStudent(11L, lesson1, studentToKeep, PaymentStatus.PAID, studentToKeep.getPriceIndividual(), studentToKeep.getCurrency());
        lesson1.setLessonStudents(new HashSet<>(Arrays.asList(lsToRemove, lsToKeep)));

        // New student to add
        Student studentToAdd = student1; // student1 from setUp
        List<Long> updatedStudentIds = Arrays.asList(studentToKeep.getId(), studentToAdd.getId()); // Keep one, add one

        // Mock the buildLessonStudent to return configurable objects
        LessonStudent newLsToKeep = new LessonStudent();
        newLsToKeep.setStudent(studentToKeep);
        newLsToKeep.setLesson(lesson1);
        newLsToKeep.setCurrency(studentToKeep.getCurrency());
        newLsToKeep.setPrice(studentToKeep.getPriceGroup());
        LessonStudent newLsToAdd = new LessonStudent();
        newLsToAdd.setStudent(studentToAdd);
        newLsToAdd.setLesson(lesson1);
        newLsToAdd.setCurrency(studentToAdd.getCurrency());
        newLsToAdd.setPrice(studentToAdd.getPriceGroup());

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(studentService.getStudentsByIds(updatedStudentIds)).thenReturn(Arrays.asList(studentToKeep, studentToAdd));
        when(lessonStudentService.buildLessonStudent(eq(studentToKeep), eq(lesson1), any(PaymentStatus.class))).thenReturn(newLsToKeep);
        when(lessonStudentService.save(newLsToKeep)).thenReturn(newLsToKeep);
        when(lessonStudentService.buildLessonStudent(eq(studentToAdd), eq(lesson1), any(PaymentStatus.class))).thenReturn(newLsToAdd);
        when(lessonStudentService.save(newLsToAdd)).thenReturn(newLsToAdd);

        lessonService.updateLesson(lessonToUpdate, updatedStudentIds);

        // Verify balance changes
        verify(balanceService, times(1)).changeBalance(eq(studentToRemove.getId()), eq(studentToRemove.getCurrency()), eq(studentToRemove.getPriceIndividual())); // Removed student's debit reversed
        verify(balanceService, times(1)).changeBalance(eq(studentToKeep.getId()), eq(studentToKeep.getCurrency()), eq(studentToKeep.getPriceIndividual())); // Kept student's old debit reversed
        verify(balanceService, times(1)).changeBalance(eq(studentToKeep.getId()), eq(studentToKeep.getCurrency()), eq(studentToKeep.getPriceGroup().negate())); // Kept student's new debit
        verify(balanceService, times(1)).changeBalance(eq(studentToAdd.getId()), eq(studentToAdd.getCurrency()), eq(studentToAdd.getPriceGroup().negate())); // Added student's new debit

        // Verify resync for all affected students
        verify(balanceService, times(1)).resyncPaymentStatus(eq(studentToRemove.getId()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(studentToKeep.getId()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(studentToAdd.getId()));

        // Verify pricing logic
        ArgumentCaptor<LessonStudent> lessonStudentCaptor = ArgumentCaptor.forClass(LessonStudent.class);
        verify(lessonStudentService, times(2)).save(lessonStudentCaptor.capture());
        List<LessonStudent> savedLessonStudents = lessonStudentCaptor.getAllValues();

        // Since there are two students, both should have group pricing
        Optional<LessonStudent> first = savedLessonStudents.stream().filter(ls -> ls.getStudent().equals(studentToAdd)).findFirst();
        Optional<LessonStudent> second = savedLessonStudents.stream().filter(ls -> ls.getStudent().equals(studentToKeep)).findFirst();

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertEquals(studentToAdd.getPriceGroup(), first.get().getPrice());
        assertEquals(studentToKeep.getPriceGroup(), second.get().getPrice());
    }


    // Test cases for deleteLesson
    @Test
    void deleteLesson_shouldReverseDebitsAndResyncForAffectedStudents() {
        // Setup: Lesson has two students
        LessonStudent ls1 = createLessonStudent(1L, lesson1, student1, PaymentStatus.PAID, student1.getPriceIndividual(), student1.getCurrency());
        LessonStudent ls2 = createLessonStudent(2L, lesson1, student2, PaymentStatus.UNPAID, student2.getPriceIndividual(), student2.getCurrency());
        lesson1.setLessonStudents(new HashSet<>(Arrays.asList(ls1, ls2)));

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));

        lessonService.deleteLesson(1L);

        // Verify balance changes
        verify(balanceService, times(1)).changeBalance(eq(student1.getId()), eq(student1.getCurrency()), eq(ls1.getPrice()));
        verify(balanceService, times(1)).changeBalance(eq(student2.getId()), eq(student2.getCurrency()), eq(ls2.getPrice()));

        // Verify the lesson repository delete method was called
        verify(lessonRepository, times(1)).deleteById(1L);

        // Verify resync for all affected students
        verify(balanceService, times(1)).resyncPaymentStatus(eq(student1.getId()));
        verify(balanceService, times(1)).resyncPaymentStatus(eq(student2.getId()));
    }

    private Student createStudent(Long id, String firstName, String lastName, BigDecimal priceIndividual, BigDecimal priceGroup, Currency currency) {
        Student student = new Student();
        student.setId(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setPriceIndividual(priceIndividual);
        student.setPriceGroup(priceGroup);
        student.setCurrency(currency);
        return student;
    }

    private LessonStudent createLessonStudent(Long id, Lesson lesson, Student student, PaymentStatus paymentStatus, BigDecimal price, Currency currency) {
        LessonStudent ls = new LessonStudent();
        ls.setId(id);
        ls.setLesson(lesson);
        ls.setStudent(student);
        ls.setPaymentStatus(paymentStatus);
        ls.setPrice(price);
        ls.setCurrency(currency);
        return ls;
    }
}