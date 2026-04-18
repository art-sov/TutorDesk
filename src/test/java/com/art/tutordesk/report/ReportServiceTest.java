package com.art.tutordesk.report;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentStatus;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.PaymentStatusUtil;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private LessonStudentRepository lessonStudentRepository;
    @Mock
    private PaymentStatusUtil paymentStatusUtil;

    @InjectMocks
    private ReportService reportService;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");

        student2 = new Student();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");

        // Default behavior for historical fetches and status calculation to avoid NPE
        lenient().when(lessonStudentRepository.findAllByStudentId(anyLong())).thenReturn(Collections.emptyList());
        lenient().when(paymentRepository.findAllByStudentId(anyLong())).thenReturn(Collections.emptyList());
        lenient().when(paymentStatusUtil.calculatePaymentStatuses(anyList(), anyList())).thenReturn(Collections.emptyMap());
    }

    @Test
    void getLessonsThisMonthCount() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        when(lessonRepository.countByLessonDateGreaterThanEqual(startOfMonth)).thenReturn(15L);

        long result = reportService.getLessonsThisMonthCount();

        assertEquals(15L, result);
        verify(lessonRepository).countByLessonDateGreaterThanEqual(startOfMonth);
    }

    @Test
    void getActiveStudentsCount() {
        when(studentRepository.countByActiveTrue()).thenReturn(10L);

        long result = reportService.getActiveStudentsCount();

        assertEquals(10L, result);
        verify(studentRepository).countByActiveTrue();
    }

    @Test
    void getTotalPaymentsThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        Payment payment1 = new Payment();
        payment1.setAmount(BigDecimal.valueOf(100));
        payment1.setCurrency(Currency.USD);
        Payment payment2 = new Payment();
        payment2.setAmount(BigDecimal.valueOf(50));
        payment2.setCurrency(Currency.USD);
        Payment payment3 = new Payment();
        payment3.setAmount(BigDecimal.valueOf(200));
        payment3.setCurrency(Currency.EUR);
        List<Payment> payments = Arrays.asList(payment1, payment2, payment3);
        when(paymentRepository.findByPaymentDateGreaterThanEqual(startOfMonth)).thenReturn(payments);

        Map<Currency, BigDecimal> result = reportService.getTotalPaymentsThisMonth();

        assertEquals(2, result.size());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(result.get(Currency.USD)));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(result.get(Currency.EUR)));
        verify(paymentRepository).findByPaymentDateGreaterThanEqual(startOfMonth);
    }

    @Test
    void generateReport_IncludeLessonsOnly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<Long> studentIds = Arrays.asList(1L, 2L);

        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setLessonDate(LocalDate.of(2025, 1, 10));
        LessonStudent ls1 = new LessonStudent();
        ls1.setId(101L);
        ls1.setStudent(student1);
        ls1.setLesson(lesson1);
        ls1.setPrice(BigDecimal.valueOf(50));
        ls1.setCurrency(Currency.USD);
        ls1.setStatus(LessonStudentStatus.COMPLETED);

        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setLessonDate(LocalDate.of(2025, 1, 5));
        LessonStudent ls2 = new LessonStudent();
        ls2.setId(102L);
        ls2.setStudent(student2);
        ls2.setLesson(lesson2);
        ls2.setPrice(BigDecimal.valueOf(60));
        ls2.setCurrency(Currency.EUR);
        ls2.setStatus(LessonStudentStatus.COMPLETED);

        when(lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, studentIds)).thenReturn(Arrays.asList(ls1, ls2));
        when(paymentStatusUtil.calculatePaymentStatuses(anyList(), anyList())).thenReturn(Map.of(101L, PaymentStatus.PAID, 102L, PaymentStatus.UNPAID));

        List<ReportItemDto> report = reportService.generateReport(startDate, endDate, studentIds, true, false);

        assertEquals(2, report.size());
        assertEquals("Jane Smith", report.get(0).getStudentName());
        assertEquals(PaymentStatus.UNPAID, report.get(0).getPaymentStatus());
        assertEquals("John Doe", report.get(1).getStudentName());
        assertEquals(PaymentStatus.PAID, report.get(1).getPaymentStatus());

        verify(paymentStatusUtil, times(2)).calculatePaymentStatuses(anyList(), anyList());
    }

    @Test
    void generateReport_IncludePaymentsOnly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<Long> studentIds = Arrays.asList(1L, 2L);

        Payment payment1 = new Payment();
        payment1.setStudent(student1);
        payment1.setAmount(BigDecimal.valueOf(100));
        payment1.setCurrency(Currency.USD);
        payment1.setPaymentDate(LocalDate.of(2025, 1, 15));
        Payment payment2 = new Payment();
        payment2.setStudent(student2);
        payment2.setAmount(BigDecimal.valueOf(120));
        payment2.setCurrency(Currency.EUR);
        payment2.setPaymentDate(LocalDate.of(2025, 1, 3));

        when(paymentRepository.findByFilters(startDate, endDate, studentIds)).thenReturn(Arrays.asList(payment1, payment2));

        List<ReportItemDto> report = reportService.generateReport(startDate, endDate, studentIds, false, true);

        assertEquals(2, report.size());
        assertEquals(LocalDate.of(2025, 1, 3), report.get(0).getDate());
        assertEquals(LocalDate.of(2025, 1, 15), report.get(1).getDate());

        verify(paymentStatusUtil, never()).calculatePaymentStatuses(anyList(), anyList());
    }

    @Test
    void generateReport_IncludeBoth() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<Long> studentIds = List.of(1L);

        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setLessonDate(LocalDate.of(2025, 1, 10));
        LessonStudent ls = new LessonStudent();
        ls.setId(101L);
        ls.setStudent(student1);
        ls.setLesson(lesson);
        ls.setPrice(BigDecimal.valueOf(50));
        ls.setCurrency(Currency.USD);
        ls.setStatus(LessonStudentStatus.COMPLETED);
        when(lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, studentIds)).thenReturn(Collections.singletonList(ls));

        Payment payment = new Payment();
        payment.setStudent(student1);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setCurrency(Currency.USD);
        payment.setPaymentDate(LocalDate.of(2025, 1, 5));
        when(paymentRepository.findByFilters(startDate, endDate, studentIds)).thenReturn(Collections.singletonList(payment));

        List<ReportItemDto> report = reportService.generateReport(startDate, endDate, studentIds, true, true);

        assertEquals(2, report.size());
        assertEquals(ReportItemDto.ItemType.PAYMENT, report.get(0).getItemType());
        assertEquals(ReportItemDto.ItemType.LESSON, report.get(1).getItemType());
        verify(paymentStatusUtil, times(1)).calculatePaymentStatuses(anyList(), anyList());
    }

    @Test
    void generateReport_ExcludesScheduledAndCanceledStatus() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<Long> studentIds = Collections.singletonList(1L);

        LessonStudent scheduledLS = new LessonStudent();
        scheduledLS.setId(101L);
        scheduledLS.setStudent(student1);
        scheduledLS.setStatus(LessonStudentStatus.SCHEDULED);
        scheduledLS.setLesson(new Lesson());
        scheduledLS.getLesson().setLessonDate(LocalDate.of(2025, 1, 10));

        LessonStudent canceledLS = new LessonStudent();
        canceledLS.setId(102L);
        canceledLS.setStudent(student1);
        canceledLS.setStatus(LessonStudentStatus.CANCELED);
        canceledLS.setLesson(new Lesson());
        canceledLS.getLesson().setLessonDate(LocalDate.of(2025, 1, 11));

        when(lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, studentIds))
                .thenReturn(Arrays.asList(scheduledLS, canceledLS));

        List<ReportItemDto> report = reportService.generateReport(startDate, endDate, studentIds, true, false);

        assertEquals(2, report.size());
        assertNull(report.get(0).getPaymentStatus());
        assertNull(report.get(1).getPaymentStatus());
    }
}