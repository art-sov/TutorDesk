package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentStatus;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.PaymentStatusUtil;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.dto.LessonStudentDto;
import com.art.tutordesk.lesson.dto.LessonStudentUpdateDTO;
import com.art.tutordesk.lesson.dto.LessonUpdateForm;
import com.art.tutordesk.lesson.mapper.LessonMapper;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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
    private LessonMapper lessonMapper;
    @Mock
    private LessonBalanceService lessonBalanceService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private LessonStudentRepository lessonStudentRepository;
    @Mock
    private PaymentStatusUtil paymentStatusUtil;

    @InjectMocks
    private LessonService lessonService;

    private Lesson lesson;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setLessonDate(LocalDate.now());
        lesson.setLessonStudents(new HashSet<>());

        student1 = new Student();
        student1.setId(10L);
        student1.setFirstName("John");
        student1.setPriceIndividual(new BigDecimal("25.00"));
        student1.setPriceGroup(new BigDecimal("20.00"));
        student1.setCurrency(Currency.USD);

        student2 = new Student();
        student2.setId(11L);
        student2.setFirstName("Jane");
        student2.setPriceIndividual(new BigDecimal("30.00"));
        student2.setPriceGroup(new BigDecimal("24.00"));
        student2.setCurrency(Currency.USD);

        // Lenient stubs for new dependencies to avoid breaking existing tests
        lenient().when(lessonStudentRepository.findAllByStudentId(anyLong())).thenReturn(Collections.emptyList());
        lenient().when(paymentRepository.findAllByStudentId(anyLong())).thenReturn(Collections.emptyList());
        lenient().when(paymentStatusUtil.calculatePaymentStatuses(anyList(), anyList())).thenReturn(Collections.emptyMap());
        lenient().when(paymentStatusUtil.calculateOverallLessonPaymentStatus(anyList())).thenReturn(PaymentStatus.UNPAID);
    }

    @Test
    void testGetLessonsByDateRange() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);
        when(lessonRepository.findByLessonDateBetween(start, end)).thenReturn(List.of(lesson));
        when(lessonMapper.toLessonListDTO(any())).thenReturn(new LessonListDTO());

        List<LessonListDTO> result = lessonService.getLessonsByDateRange(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(lessonRepository).findByLessonDateBetween(start, end);
    }

    @Test
    void testGetLessonsByDateRange_Empty() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);
        when(lessonRepository.findByLessonDateBetween(start, end)).thenReturn(List.of());

        List<LessonListDTO> result = lessonService.getLessonsByDateRange(start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(lessonRepository).findByLessonDateBetween(start, end);
        verify(lessonMapper, never()).toLessonListDTO(any());
    }

    @Test
    void testGetLessonById_Found() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toLessonProfileDTO(lesson)).thenReturn(new LessonProfileDTO());

        LessonProfileDTO result = lessonService.getLessonById(1L);

        assertNotNull(result);
        verify(lessonRepository).findById(1L);
    }

    @Test
    void testGetLessonById_WithStudentsAndStatuses() {
        LessonStudent ls1 = createLessonStudent(100L, student1, lesson, new BigDecimal("25.00"), LessonStudentStatus.COMPLETED);
        lesson.getLessonStudents().add(ls1);
        
        LessonProfileDTO dto = new LessonProfileDTO();
        LessonStudentDto lsDto = new LessonStudentDto();
        lsDto.setId(100L);
        lsDto.setStatus(LessonStudentStatus.COMPLETED);
        dto.setStudentAssociations(List.of(lsDto));

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toLessonProfileDTO(lesson)).thenReturn(dto);
        when(paymentStatusUtil.calculatePaymentStatuses(anyList(), anyList())).thenReturn(Map.of(100L, PaymentStatus.PAID));
        when(paymentStatusUtil.calculateOverallLessonPaymentStatus(anyList())).thenReturn(PaymentStatus.PAID);

        LessonProfileDTO result = lessonService.getLessonById(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
        assertEquals(PaymentStatus.PAID, result.getStudentAssociations().getFirst().getPaymentStatus());
    }

    @Test
    void testGetLessonById_ScheduledStudentHasNoPaymentStatus() {
        LessonStudent ls1 = createLessonStudent(100L, student1, lesson, new BigDecimal("25.00"), LessonStudentStatus.SCHEDULED);
        lesson.getLessonStudents().add(ls1);
        
        LessonProfileDTO dto = new LessonProfileDTO();
        LessonStudentDto lsDto = new LessonStudentDto();
        lsDto.setId(100L);
        lsDto.setStatus(LessonStudentStatus.SCHEDULED);
        dto.setStudentAssociations(List.of(lsDto));

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toLessonProfileDTO(lesson)).thenReturn(dto);

        LessonProfileDTO result = lessonService.getLessonById(1L);

        assertNotNull(result);
        assertNull(result.getPaymentStatus());
        assertNull(result.getStudentAssociations().getFirst().getPaymentStatus());
    }

    @Test
    void testGetLessonById_NotFound() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> lessonService.getLessonById(1L));
    }

    @Test
    void testSaveLesson() {
        List<Long> studentIds = List.of(10L);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);
        when(studentService.getStudentsByIds(studentIds)).thenReturn(List.of(student1));

        LessonStudent ls = new LessonStudent();
        ls.setStudent(student1);
        ls.setLesson(lesson);
        when(lessonStudentService.buildLessonStudent(student1, lesson)).thenReturn(ls);

        Lesson result = lessonService.saveLesson(lesson, studentIds);

        assertNotNull(result);
        verify(lessonRepository).save(lesson);
        verify(lessonStudentService).save(any(LessonStudent.class));
        assertEquals(new BigDecimal("25.00"), ls.getPrice());
    }

    @Test
    void testUpdateLesson_RemoveStudent() {
        LessonStudent ls1 = createLessonStudent(100L, student1, lesson, new BigDecimal("20.00"), LessonStudentStatus.COMPLETED);
        LessonStudent ls2 = createLessonStudent(101L, student2, lesson, new BigDecimal("24.00"), LessonStudentStatus.COMPLETED);
        lesson.getLessonStudents().addAll(Set.of(ls1, ls2));

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        LessonUpdateForm form = new LessonUpdateForm();
        form.setLessonDate(lesson.getLessonDate());
        LessonStudentUpdateDTO update1 = new LessonStudentUpdateDTO();
        update1.setStudentId(10L);
        update1.setStatus(LessonStudentStatus.COMPLETED);
        form.setStudentUpdates(List.of(update1));

        lessonService.updateLesson(1L, form);

        verify(lessonBalanceService).adjustBalanceForPriceAndStatusChange(ls2, ls2.getPrice(), LessonStudentStatus.CANCELED);
        verify(lessonStudentService).delete(ls2);
        verify(lessonBalanceService).adjustBalanceForPriceAndStatusChange(ls1, student1.getPriceIndividual(), LessonStudentStatus.COMPLETED);

        assertEquals(1, lesson.getLessonStudents().size());
    }

    @Test
    void testUpdateLesson_AddStudent() {
        LessonStudent ls1 = createLessonStudent(100L, student1, lesson, new BigDecimal("25.00"), LessonStudentStatus.SCHEDULED);
        lesson.getLessonStudents().add(ls1);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(studentService.getStudentEntityById(11L)).thenReturn(student2);

        LessonStudent ls2New = new LessonStudent();
        ls2New.setStudent(student2);
        ls2New.setLesson(lesson);
        when(lessonStudentService.buildLessonStudent(student2, lesson)).thenReturn(ls2New);

        LessonUpdateForm form = new LessonUpdateForm();
        form.setLessonDate(lesson.getLessonDate());
        LessonStudentUpdateDTO update1 = new LessonStudentUpdateDTO();
        update1.setStudentId(10L);
        update1.setStatus(LessonStudentStatus.SCHEDULED);
        LessonStudentUpdateDTO update2 = new LessonStudentUpdateDTO();
        update2.setStatus(LessonStudentStatus.SCHEDULED);
        update2.setStudentId(11L);
        form.setStudentUpdates(List.of(update1, update2));

        lessonService.updateLesson(1L, form);

        verify(lessonBalanceService).adjustBalanceForPriceAndStatusChange(ls1, student1.getPriceGroup(), LessonStudentStatus.SCHEDULED);
        verify(lessonStudentService).save(ls2New);
        assertEquals(new BigDecimal("24.00"), ls2New.getPrice());
        verify(lessonBalanceService).adjustBalanceForPriceAndStatusChange(ls2New, student2.getPriceGroup(), LessonStudentStatus.SCHEDULED);

        assertEquals(2, lesson.getLessonStudents().size());
    }

    @Test
    void testDeleteLesson() {
        lessonService.deleteLesson(1L);
        verify(lessonRepository).deleteById(1L);
    }

    private LessonStudent createLessonStudent(Long id, Student s, Lesson l, BigDecimal p, LessonStudentStatus st) {
        LessonStudent ls = new LessonStudent();
        ls.setId(id);
        ls.setStudent(s);
        ls.setLesson(l);
        ls.setPrice(p);
        ls.setStatus(st);
        ls.setCurrency(s.getCurrency());
        return ls;
    }
}
