package com.art.tutordesk.lesson.mapper;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {LessonMapperImpl.class, LessonStudentMapperImpl.class})
public class LessonMapperTest {

    @Autowired
    private LessonMapper lessonMapper;

    private Lesson lesson;
    private LessonStudent lessonStudent1;
    private LessonStudent lessonStudent2;
    private LessonStudent lessonStudent3;

    @BeforeEach
    void setUp() {
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setPriceIndividual(new BigDecimal("50.00"));
        student1.setCurrency(Currency.USD);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setPriceIndividual(new BigDecimal("60.00"));
        student2.setCurrency(Currency.EUR);

        Student student3 = new Student();
        student3.setId(3L);
        student3.setFirstName("Peter");
        student3.setLastName("Jones");
        student3.setPriceIndividual(BigDecimal.ZERO); // Free student
        student3.setCurrency(Currency.PLN);

        lesson = new Lesson();
        lesson.setId(10L);
        lesson.setLessonDate(LocalDate.of(2025, 1, 15));
        lesson.setStartTime(LocalTime.of(10, 0));
        lesson.setTopic("Mathematics");

        lessonStudent1 = new LessonStudent();
        lessonStudent1.setId(100L);
        lessonStudent1.setLesson(lesson);
        lessonStudent1.setStudent(student1);
        lessonStudent1.setPaymentStatus(PaymentStatus.PAID);
        lessonStudent1.setPrice(student1.getPriceIndividual());
        lessonStudent1.setCurrency(student1.getCurrency());

        lessonStudent2 = new LessonStudent();
        lessonStudent2.setId(101L);
        lessonStudent2.setLesson(lesson);
        lessonStudent2.setStudent(student2);
        lessonStudent2.setPaymentStatus(PaymentStatus.UNPAID);
        lessonStudent2.setPrice(student2.getPriceIndividual());
        lessonStudent2.setCurrency(student2.getCurrency());

        lessonStudent3 = new LessonStudent();
        lessonStudent3.setId(102L);
        lessonStudent3.setLesson(lesson);
        lessonStudent3.setStudent(student3);
        lessonStudent3.setPaymentStatus(PaymentStatus.FREE);
        lessonStudent3.setPrice(student3.getPriceIndividual());
        lessonStudent3.setCurrency(student3.getCurrency());
    }

    @Test
    void toLessonListDTO_withMultipleStudents_shouldMapCorrectly() {
        Set<LessonStudent> lessonStudents = new HashSet<>();
        lessonStudents.add(lessonStudent1); // PAID
        lessonStudents.add(lessonStudent2); // UNPAID
        lessonStudents.add(lessonStudent3); // FREE
        lesson.setLessonStudents(lessonStudents);

        LessonListDTO dto = lessonMapper.toLessonListDTO(lesson);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(lesson.getId());
        assertThat(dto.getLessonDate()).isEqualTo(lesson.getLessonDate());
        assertThat(dto.getStartTime()).isEqualTo(lesson.getStartTime());
        assertThat(dto.getTopic()).isEqualTo(lesson.getTopic());
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentNames()).containsExactlyInAnyOrder("John Doe", "Jane Smith", "Peter Jones");
    }

    @Test
    void toLessonListDTO_withNoStudents_shouldMapCorrectly() {
        lesson.setLessonStudents(Collections.emptySet());

        LessonListDTO dto = lessonMapper.toLessonListDTO(lesson);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(lesson.getId());
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentNames()).isEmpty();
    }

    @Test
    void toLessonListDTO_withAllFreeStudents_shouldMapCorrectly() {
        Set<LessonStudent> lessonStudents = new HashSet<>();
        lessonStudents.add(lessonStudent3); // FREE
        lessonStudents.add(createLessonStudent(lesson, createStudent(BigDecimal.ZERO), PaymentStatus.FREE));
        lesson.setLessonStudents(lessonStudents);

        LessonListDTO dto = lessonMapper.toLessonListDTO(lesson);

        assertThat(dto).isNotNull();
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentNames()).containsExactlyInAnyOrder("Peter Jones", "Mark White");
    }

    @Test
    void toLessonListDTO_withAllPaidStudents_shouldMapCorrectly() {
        Set<LessonStudent> lessonStudents = new HashSet<>();
        lessonStudents.add(lessonStudent1); // PAID
        lessonStudents.add(createLessonStudent(lesson, createStudent(new BigDecimal("70.00")), PaymentStatus.PAID));
        lesson.setLessonStudents(lessonStudents);

        LessonListDTO dto = lessonMapper.toLessonListDTO(lesson);

        assertThat(dto).isNotNull();
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentNames()).containsExactlyInAnyOrder("John Doe", "Mark White");
    }

    @Test
    void toLessonListDTO_withAllUnpaidStudents_shouldMapCorrectly() {
        Set<LessonStudent> lessonStudents = new HashSet<>();
        lessonStudents.add(lessonStudent2); // UNPAID
        lessonStudents.add(createLessonStudent(lesson, createStudent(new BigDecimal("70.00")), PaymentStatus.UNPAID));
        lesson.setLessonStudents(lessonStudents);

        LessonListDTO dto = lessonMapper.toLessonListDTO(lesson);

        assertThat(dto).isNotNull();
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentNames()).containsExactlyInAnyOrder("Jane Smith", "Mark White");
    }

    @Test
    void toLessonProfileDTO_withMultipleStudents_shouldMapCorrectly() {
        Set<LessonStudent> lessonStudents = new HashSet<>();
        lessonStudents.add(lessonStudent1); // PAID
        lessonStudents.add(lessonStudent2); // UNPAID
        lessonStudents.add(lessonStudent3); // FREE
        lesson.setLessonStudents(lessonStudents);

        LessonProfileDTO dto = lessonMapper.toLessonProfileDTO(lesson);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(lesson.getId());
        assertThat(dto.getLessonDate()).isEqualTo(lesson.getLessonDate());
        assertThat(dto.getStartTime()).isEqualTo(lesson.getStartTime());
        assertThat(dto.getTopic()).isEqualTo(lesson.getTopic());
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentAssociations()).hasSize(3); // Should contain all LessonStudents
    }

    @Test
    void toLessonProfileDTO_withNoStudents_shouldMapCorrectly() {
        lesson.setLessonStudents(Collections.emptySet());

        LessonProfileDTO dto = lessonMapper.toLessonProfileDTO(lesson);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(lesson.getId());
        assertNull(dto.getPaymentStatus());
        assertThat(dto.getStudentAssociations()).isEmpty();
    }

    @Test
    void mapLessonStudentsToNames_withMultipleStudents_shouldReturnCorrectNames() {
        Set<LessonStudent> lessonStudents = new HashSet<>();
        lessonStudents.add(lessonStudent1);
        lessonStudents.add(lessonStudent2);

        List<String> studentNames = lessonMapper.mapLessonStudentsToNames(lessonStudents);

        assertThat(studentNames).containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void mapLessonStudentsToNames_withEmptySet_shouldReturnEmptyList() {
        List<String> studentNames = lessonMapper.mapLessonStudentsToNames(Collections.emptySet());

        assertThat(studentNames).isEmpty();
    }

    @Test
    void mapLessonStudentsToNames_withNullSet_shouldReturnEmptyList() {
        List<String> studentNames = lessonMapper.mapLessonStudentsToNames(null);

        assertThat(studentNames).isEmpty();
    }

    private Student createStudent(BigDecimal price) {
        Student student = new Student();
        student.setId(4L);
        student.setFirstName("Mark");
        student.setLastName("White");
        student.setPriceIndividual(price);
        student.setCurrency(Currency.USD);
        return student;
    }

    private LessonStudent createLessonStudent(Lesson lesson, Student student, PaymentStatus paymentStatus) {
        LessonStudent ls = new LessonStudent();
        ls.setLesson(lesson);
        ls.setStudent(student);
        ls.setPaymentStatus(paymentStatus);
        ls.setPrice(student.getPriceIndividual());
        ls.setCurrency(student.getCurrency());
        return ls;
    }
}