package com.art.tutordesk.lesson.service;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.lesson.PaymentStatus;
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
import java.time.LocalTime;

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

    private Student student;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        student = createStudent(1L, "John", "Doe", Currency.USD, new BigDecimal("25.00"), new BigDecimal("20.00"));
        lesson = createLesson(1L, LocalDate.now(), LocalTime.now(), "Math");
    }

    @Test
    void save_shouldSaveLessonStudent() {
        LessonStudent lessonStudent = new LessonStudent();
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

    private Student createStudent(Long id, String firstName, String lastName, Currency currency, BigDecimal priceIndividual, BigDecimal priceGroup) {
        Student student1 = new Student();
        student1.setId(id);
        student1.setFirstName(firstName);
        student1.setLastName(lastName);
        student1.setCurrency(currency);
        student1.setPriceIndividual(priceIndividual);
        student1.setPriceGroup(priceGroup);
        return student1;
    }

    private Lesson createLesson(Long id, LocalDate date, LocalTime time, String topic) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setLessonDate(date);
        lesson.setStartTime(time);
        lesson.setTopic(topic);
        return lesson;
    }
}