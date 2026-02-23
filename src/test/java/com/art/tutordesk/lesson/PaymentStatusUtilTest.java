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

    private LessonStudent createLessonStudent(PaymentStatus status, AttendanceStatus attendanceStatus) {
        LessonStudent lessonStudent = new LessonStudent();
//        lessonStudent.setPaymentStatus(status);
        lessonStudent.setAttendanceStatus(attendanceStatus);
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
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.FREE, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithAllStudentsPaid_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithAllStudentsUnpaid_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithPaidAndFreeStudents_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithUnpaidAndFreeStudents_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithPaidAndUnpaidStudents_ShouldReturnPartiallyPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PARTIALLY_PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithPaidUnpaidAndFreeStudents_ShouldReturnPartiallyPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PARTIALLY_PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithSinglePaidStudent_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithSingleUnpaidStudent_ShouldReturnUnpaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.UNPAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithSingleFreeStudent_ShouldReturnFree() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.FREE, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithAllStudentsAbsent_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.ABSENT));
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.ABSENT));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithMixedAttendanceAndPaymentStatuses_ShouldIgnoreAbsentStudents() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.ABSENT));
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PARTIALLY_PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

    @Test
    void testCalculateWithMixedAttendanceAndPaymentStatuses_AllPresentStudentsPaidOrFree_ShouldReturnPaid() {
        Set<LessonStudent> students = new HashSet<>();
        students.add(createLessonStudent(PaymentStatus.UNPAID, AttendanceStatus.ABSENT));
        students.add(createLessonStudent(PaymentStatus.PAID, AttendanceStatus.PRESENT));
        students.add(createLessonStudent(PaymentStatus.FREE, AttendanceStatus.PRESENT));
        assertEquals(PaymentStatus.PAID, paymentStatusUtil.calculateAndSetLessonPaymentStatus(students));
    }

}