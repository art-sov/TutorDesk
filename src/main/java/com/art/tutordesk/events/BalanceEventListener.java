package com.art.tutordesk.events;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentStatus;
import com.art.tutordesk.student.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceEventListener {

    private final BalanceService balanceService;

    @EventListener
    public void handleLessonStudentCreation(LessonStudentCreatedEvent event) {
        LessonStudent lessonStudent = event.getLessonStudent();
        balanceService.updateBalanceOnLessonCreation(lessonStudent, lessonStudent.getStudent());
    }

    @EventListener
    public void handleLessonStudentDeletion(LessonStudentDeletedEvent event) {
        LessonStudent lessonStudent = event.getLessonStudent();
        // If a PAID lesson is deleted, refund the student.
        // This might make them eligible to pay for other lessons.
        if (lessonStudent.getPaymentStatus() == PaymentStatus.PAID) {
            balanceService.refundLesson(lessonStudent);
            balanceService.settleUnpaidLessons(lessonStudent.getStudent());
        }
    }

    @EventListener
    public void handlePaymentCreation(PaymentCreatedEvent event) {
        Payment payment = event.getPayment();
        balanceService.updateBalanceOnPaymentCreation(payment, payment.getStudent());
        balanceService.settleUnpaidLessons(payment.getStudent());
    }

    @EventListener
    public void handlePaymentModification(PaymentModifiedEvent event) {
        Payment payment = event.getPayment();
        balanceService.updateBalanceOnPaymentModification(payment, payment.getStudent(), event.getOldPaymentAmount());
        balanceService.settleUnpaidLessons(payment.getStudent());
    }

    @EventListener
    public void handlePaymentDeletion(PaymentDeletedEvent event) {
        Payment payment = event.getPayment();
        balanceService.updateBalanceOnPaymentDeletion(payment, payment.getStudent());
    }

    @EventListener
    public void handleStudentHardDeletion(StudentHardDeletedEvent event) {
        balanceService.resetBalancesForStudent(event.getStudentId());
    }
}
