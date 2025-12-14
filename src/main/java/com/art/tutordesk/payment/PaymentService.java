package com.art.tutordesk.payment;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.student.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BalanceService balanceService;

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
        Student student = savedPayment.getStudent();
        
        // Use the new generic changeBalance method
        balanceService.changeBalance(student.getId(), savedPayment.getCurrency(), savedPayment.getAmount());
        balanceService.resyncPaymentStatus(student.getId());
        
        return savedPayment;
    }

    @Transactional
    public Payment updatePayment(Payment payment) {
        Payment existingPayment = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found for update with id: " + payment.getId()));
        
        BigDecimal oldAmount = existingPayment.getAmount();
        BigDecimal newAmount = payment.getAmount();
        BigDecimal delta = newAmount.subtract(oldAmount);
        
        Student student = existingPayment.getStudent();

        // Save the updated payment information
        paymentRepository.save(payment);

        // Use the new generic changeBalance method
        balanceService.changeBalance(student.getId(), payment.getCurrency(), delta);
        balanceService.resyncPaymentStatus(student.getId());
        
        return payment;
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found for deletion with id: " + id));
        
        Student student = payment.getStudent();
        BigDecimal delta = payment.getAmount().negate(); // The delta is the negative of the payment amount

        // Update balance *before* deleting the payment
        balanceService.changeBalance(student.getId(), payment.getCurrency(), delta);
        
        // Delete the actual payment
        paymentRepository.deleteById(id);

        // Resync statuses after all DB changes are done within this transaction
        balanceService.resyncPaymentStatus(student.getId());
    }
}

