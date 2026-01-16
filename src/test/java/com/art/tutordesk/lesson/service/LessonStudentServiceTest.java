package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonStudentServiceTest {

    @Mock
    private LessonStudentRepository lessonStudentRepository;

    @InjectMocks
    private LessonStudentService lessonStudentService;

    private LessonStudent lessonStudent;
    private Student student;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        student = createStudent();
        lesson = createLesson();
        lessonStudent = createStudentLesson();
    }

    @Test
    void save_shouldSaveLessonStudent() {


        when(lessonStudentRepository.save(any(LessonStudent.class))).thenReturn(lessonStudent);

        LessonStudent result = lessonStudentService.save(lessonStudent);

        assertNotNull(result);
        verify(lessonStudentRepository, times(1)).save(lessonStudent);
    }

    @Test
    void buildLessonStudent_shouldCorrectlyBuildObjectAndSetCurrency() {
        PaymentStatus paymentStatus = PaymentStatus.UNPAID;

        LessonStudent result = lessonStudentService.buildLessonStudent(student, lesson, paymentStatus);

        assertNotNull(result);
        assertEquals(lesson, result.getLesson());
        assertEquals(student, result.getStudent());
        assertEquals(paymentStatus, result.getPaymentStatus());
        assertEquals(student.getCurrency(), result.getCurrency());
        assertNull(result.getPrice()); // Price should not be set by this service
    }

    @Test
    void findByLessonIdAndStudentId_whenFound_shouldReturnLessonStudent() {
        Long lessonId = 1L;
        Long studentId = 1L;
        when(lessonStudentRepository.findByLessonIdAndStudentId(lessonId, studentId)).thenReturn(Optional.of(lessonStudent));

        Optional<LessonStudent> result = lessonStudentService.findByLessonIdAndStudentId(lessonId, studentId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(lessonStudent);
    }

    @Test
    void findByLessonIdAndStudentId_whenNotFound_shouldReturnEmpty() {
        Long lessonId = 2L;
        Long studentId = 2L;
        when(lessonStudentRepository.findByLessonIdAndStudentId(lessonId, studentId)).thenReturn(Optional.empty());

        Optional<LessonStudent> result = lessonStudentService.findByLessonIdAndStudentId(lessonId, studentId);

        assertThat(result).isNotPresent();
    }

    private Student createStudent() {
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setCurrency(Currency.USD);
        student1.setPriceIndividual(new BigDecimal("25.00"));
        student1.setPriceGroup(new BigDecimal("20.00"));
        return student1;
    }

    private Lesson createLesson() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setLessonDate(LocalDate.now());
        return lesson;
    }

    private LessonStudent createStudentLesson() {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setId(1L);
        lessonStudent.setLesson(lesson);
        lessonStudent.setStudent(student);
        return lessonStudent;
    }
}