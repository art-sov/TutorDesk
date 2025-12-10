package com.art.tutordesk.payment;

import com.art.tutordesk.events.PaymentCreatedEvent;
import com.art.tutordesk.events.PaymentDeletedEvent;
import com.art.tutordesk.events.PaymentModifiedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentCreatedEvent(savedPayment));
        return savedPayment;
    }

    @Transactional
    public Payment updatePayment(Payment payment) {
        // Fetch existing payment to get old amount for balance calculation
        Payment existingPayment = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found for update with id: " + payment.getId()));
        BigDecimal oldAmount = existingPayment.getAmount();

        // Save the updated payment information
        Payment updatedPayment = paymentRepository.save(payment);

        // Publish event with old amount for balance adjustment
        eventPublisher.publishEvent(new PaymentModifiedEvent(updatedPayment, oldAmount));
        return updatedPayment;
    }

    @Transactional
    public void deletePayment(Long id) {
        // Fetch payment to publish event before deletion
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found for deletion with id: " + id));

        eventPublisher.publishEvent(new PaymentDeletedEvent(payment));
        paymentRepository.deleteById(id);
    }
}
