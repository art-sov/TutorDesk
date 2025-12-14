package com.art.tutordesk.test;

import com.art.tutordesk.balance.Balance;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentMethod;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DataJpaTestSetupTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void whenContextLoads_thenStudentsAreLoaded() {
        // Verify active students
        Student student1 = entityManager.find(Student.class, 1L);
        assertThat(student1).isNotNull();
        assertThat(student1.getFirstName()).isEqualTo("Test");
        assertThat(student1.getLastName()).isEqualTo("Student1");
        assertThat(student1.isActive()).isTrue();

        Student studentFree = entityManager.find(Student.class, 3L);
        assertThat(studentFree).isNotNull();
        assertThat(studentFree.getFirstName()).isEqualTo("Test");
        assertThat(studentFree.getLastName()).isEqualTo("StudentFree");
        assertThat(studentFree.getPriceIndividual()).isZero();
        assertThat(studentFree.isActive()).isTrue();

        // Verify inactive student
        Student studentInactive = entityManager.find(Student.class, 5L);
        assertThat(studentInactive).isNotNull();
        assertThat(studentInactive.getFirstName()).isEqualTo("Test");
        assertThat(studentInactive.getLastName()).isEqualTo("StudentInactive");
        assertThat(studentInactive.isActive()).isFalse();
    }

    @Test
    void whenLessonsAreLoaded_thenVerifyLessonDetails() {
        Lesson lesson1 = entityManager.find(Lesson.class, 1L);
        assertThat(lesson1).isNotNull();
        assertThat(lesson1.getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(lesson1.getStartTime()).isEqualTo(LocalTime.of(10, 0, 0));
        assertThat(lesson1.getTopic()).isEqualTo("Group Lesson A");

        Lesson lesson2 = entityManager.find(Lesson.class, 2L);
        assertThat(lesson2).isNotNull();
        assertThat(lesson2.getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 2));
        assertThat(lesson2.getStartTime()).isEqualTo(LocalTime.of(11, 0, 0));
        assertThat(lesson2.getTopic()).isEqualTo("Group Lesson B");
    }

    @Test
    void whenLessonStudentsAreLoaded_thenVerifyDetailsAndRelationships() {
        LessonStudent ls1 = entityManager.find(LessonStudent.class, 1L);
        assertThat(ls1).isNotNull();
        assertThat(ls1.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(ls1.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(ls1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(ls1.getStudent()).isNotNull();
        assertThat(ls1.getStudent().getId()).isEqualTo(1L);
        assertThat(ls1.getLesson()).isNotNull();
        assertThat(ls1.getLesson().getId()).isEqualTo(1L);

        LessonStudent lsFree = entityManager.find(LessonStudent.class, 3L);
        assertThat(lsFree).isNotNull();
        assertThat(lsFree.getPaymentStatus()).isEqualTo(PaymentStatus.FREE);
        assertThat(lsFree.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(lsFree.getCurrency()).isEqualTo(Currency.PLN);
        assertThat(lsFree.getStudent()).isNotNull();
        assertThat(lsFree.getStudent().getId()).isEqualTo(3L);
        assertThat(lsFree.getLesson()).isNotNull();
        assertThat(lsFree.getLesson().getId()).isEqualTo(2L);
    }

    @Test
    void whenPaymentsAreLoaded_thenVerifyDetailsAndRelationships() {
        Payment payment1 = entityManager.find(Payment.class, 1L);
        assertThat(payment1).isNotNull();
        assertThat(payment1.getPaymentDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(payment1.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(payment1.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(payment1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(payment1.getStudent()).isNotNull();
        assertThat(payment1.getStudent().getId()).isEqualTo(1L);
    }

    @Test
    void whenBalancesAreLoaded_thenVerifyDetailsAndRelationships() {
        Balance balance1 = entityManager.find(Balance.class, 1L);
        assertThat(balance1).isNotNull();
        assertThat(balance1.getAmount()).isEqualByComparingTo(new BigDecimal("-15.00"));
        assertThat(balance1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(balance1.getStudent()).isNotNull();
        assertThat(balance1.getStudent().getId()).isEqualTo(1L);

        Balance balanceFreeStudent = entityManager.find(Balance.class, 3L);
        assertThat(balanceFreeStudent).isNotNull();
        assertThat(balanceFreeStudent.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balanceFreeStudent.getCurrency()).isEqualTo(Currency.PLN);
        assertThat(balanceFreeStudent.getStudent()).isNotNull();
        assertThat(balanceFreeStudent.getStudent().getId()).isEqualTo(3L);
    }
}