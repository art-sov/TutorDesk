package com.art.tutordesk.test;

import com.art.tutordesk.balance.BalanceTransaction;
import com.art.tutordesk.balance.TransactionSource;
import com.art.tutordesk.balance.TransactionType;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentMethod;
import com.art.tutordesk.student.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/data-test.sql")
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

        Lesson lesson2 = entityManager.find(Lesson.class, 2L);
        assertThat(lesson2).isNotNull();
        assertThat(lesson2.getLessonDate()).isEqualTo(LocalDate.of(2025, 1, 2));
    }

    @Test
    void whenLessonStudentsAreLoaded_thenVerifyDetailsAndRelationships() {
        LessonStudent ls1 = entityManager.find(LessonStudent.class, 1L);
        assertThat(ls1).isNotNull();
        assertThat(ls1.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(ls1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(ls1.getStudent()).isNotNull();
        assertThat(ls1.getStudent().getId()).isEqualTo(1L);
        assertThat(ls1.getLesson()).isNotNull();
        assertThat(ls1.getLesson().getId()).isEqualTo(1L);

        LessonStudent lsFree = entityManager.find(LessonStudent.class, 3L);
        assertThat(lsFree).isNotNull();
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
        assertThat(payment1.getPaymentDate()).isEqualTo(LocalDate.of(2024, 12, 20));
        assertThat(payment1.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(payment1.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(payment1.getCurrency()).isEqualTo(Currency.USD);
        assertThat(payment1.getStudent()).isNotNull();
        assertThat(payment1.getStudent().getId()).isEqualTo(1L);

        Payment payment5 = entityManager.find(Payment.class, 5L);
        assertThat(payment5).isNotNull();
        assertThat(payment5.getPaymentDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(payment5.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(payment5.getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(payment5.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(payment5.getStudent()).isNotNull();
        assertThat(payment5.getStudent().getId()).isEqualTo(5L);
    }

    @Test
    void whenBalanceTransactionAreLoaded_thenVerifyDetailsAndRelationships() {
        BalanceTransaction balanceTransaction = entityManager.find(BalanceTransaction.class, 1L);
        assertThat(balanceTransaction).isNotNull();
        assertThat(balanceTransaction.getTransactionDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(balanceTransaction.getType()).isEqualTo(TransactionType.STUDENT_CREATED);
        assertThat(balanceTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(balanceTransaction.getCurrency()).isEqualTo(Currency.USD);
        assertThat(balanceTransaction.getSourceEntity()).isEqualTo(TransactionSource.STUDENT);
        assertThat(balanceTransaction.getSourceId()).isEqualTo(1L);
        assertThat(balanceTransaction.getStudent()).isNotNull();
    }
}
