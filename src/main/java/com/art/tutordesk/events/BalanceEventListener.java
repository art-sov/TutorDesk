package com.art.tutordesk.events;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.payment.Payment;
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
        balanceService.resyncPaymentStatus(lessonStudent.getStudent());
    }

    @EventListener
    public void handleLessonStudentDeletion(LessonStudentDeletedEvent event) {
        LessonStudent lessonStudent = event.getLessonStudent();
        // Always reverse the debit for a deleted lesson, unless it was FREE
        balanceService.reverseLessonDebit(lessonStudent);
        balanceService.resyncPaymentStatus(lessonStudent.getStudent());
    }

    @EventListener
    public void handlePaymentCreation(PaymentCreatedEvent event) {
        Payment payment = event.getPayment();
        balanceService.updateBalanceOnPaymentCreation(payment, payment.getStudent());
        balanceService.resyncPaymentStatus(payment.getStudent());
    }

    @EventListener
    public void handlePaymentModification(PaymentModifiedEvent event) {
        Payment payment = event.getPayment();
        balanceService.updateBalanceOnPaymentModification(payment, payment.getStudent(), event.getOldPaymentAmount());
        balanceService.resyncPaymentStatus(payment.getStudent());
    }

    @EventListener
    public void handlePaymentDeletion(PaymentDeletedEvent event) {
        Payment payment = event.getPayment();
        balanceService.updateBalanceOnPaymentDeletion(payment, payment.getStudent());
        balanceService.resyncPaymentStatus(payment.getStudent());
    }

}
