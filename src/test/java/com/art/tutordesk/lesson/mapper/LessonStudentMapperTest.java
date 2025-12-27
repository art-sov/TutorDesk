package com.art.tutordesk.lesson.mapper;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.dto.LessonStudentDto;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = LessonStudentMapperImpl.class)
public class LessonStudentMapperTest {

    @Autowired
    private LessonStudentMapper lessonStudentMapper;

    private LessonStudent lessonStudent;

    @BeforeEach
    void setUp() {
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");

        lessonStudent = new LessonStudent();
        lessonStudent.setId(1L);
        lessonStudent.setStudent(student);
        lessonStudent.setPrice(BigDecimal.valueOf(40));
        lessonStudent.setCurrency(Currency.USD);
        lessonStudent.setPaymentStatus(PaymentStatus.UNPAID);
    }

    @Test
    void toLessonStudentDto_shouldMapCorrectly() {
        LessonStudentDto dto = lessonStudentMapper.toLessonStudentDto(lessonStudent);

        assertEquals(dto.getStudentId(), lessonStudent.getStudent().getId());
        assertEquals(dto.getStudentFirstName(), lessonStudent.getStudent().getFirstName());
        assertEquals(dto.getStudentLastName(), lessonStudent.getStudent().getLastName());
        assertEquals(dto.getId(), lessonStudent.getId());
        assertEquals(dto.getCurrency(), lessonStudent.getCurrency());
        assertEquals(dto.getPrice(), lessonStudent.getPrice());
        assertEquals(dto.getPaymentStatus(), lessonStudent.getPaymentStatus());
    }

    @Test
    void toLessonStudent_shouldReturnNull_whenLessonStudentIsNull() {
        LessonStudentDto dto = lessonStudentMapper.toLessonStudentDto(null);

        assertNull(dto);
    }
}
