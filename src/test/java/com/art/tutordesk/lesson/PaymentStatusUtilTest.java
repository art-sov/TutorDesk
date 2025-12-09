package com.art.tutordesk.lesson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentStatusUtilTest {

    private PaymentStatusUtil paymentStatusUtil;

    @BeforeEach
    void setUp() {
        paymentStatusUtil = new PaymentStatusUtil();
    }

    private LessonStudent createLessonStudent(PaymentStatus status) {
        LessonStudent lessonStudent = new LessonStudent();
        lessonStudent.setPaymentStatus(status);
        return lessonStudent;
    }

    @Test
    void testCalculateWithEmptySet_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithAllStudentsFree_ShouldReturnFree() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.FREE));
        students.add(createLessonStudent(PaymentStatus.FREE));
        assertEquals(PaymentStatus.FREE, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithAllStudentsPaid_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID));
        students.add(createLessonStudent(PaymentStatus.PAID));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithAllStudentsUnpaid_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID));
        students.add(createLessonStudent(PaymentStatus.UNPAID));
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithPaidAndFreeStudents_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID));
        students.add(createLessonStudent(PaymentStatus.FREE));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithUnpaidAndFreeStudents_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID));
        students.add(createLessonStudent(PaymentStatus.FREE));
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithPaidAndUnpaidStudents_ShouldReturnPartiallyPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID));
        students.add(createLessonStudent(PaymentStatus.UNPAID));
        assertEquals(PaymentStatus.PARTIALLY_PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithPaidUnpaidAndFreeStudents_ShouldReturnPartiallyPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID));
        students.add(createLessonStudent(PaymentStatus.UNPAID));
        students.add(createLessonStudent(PaymentStatus.FREE));
        assertEquals(PaymentStatus.PARTIALLY_PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithSinglePaidStudent_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithSingleUnpaidStudent_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID));
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithSingleFreeStudent_ShouldReturnFree() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.FREE));
        assertEquals(PaymentStatus.FREE, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }
}