package com.art.tutordesk.payment;

import com.art.tutordesk.balance.BalanceTransactionService;
import com.art.tutordesk.balance.TransactionSource;
import com.art.tutordesk.balance.TransactionType;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BalanceTransactionService balanceTransactionService;
    private final PaymentMapper paymentMapper;
    private final StudentService studentService;

    public List<PaymentDto> getAllPayments() {
        log.debug("Fetching all payments.");
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(paymentMapper::toPaymentDto)
                .collect(Collectors.toList());
    }

    public PaymentDto getPaymentById(Long id) {
        log.debug("Fetching payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment not found with id: {}", id);
                    return new RuntimeException("Payment not found with id: " + id);
                });
        return paymentMapper.toPaymentDto(payment);
    }

    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {
        log.info("Attempting to create payment from DTO: {}", paymentDto);
        Payment payment = paymentMapper.toPayment(paymentDto);
        Student student = studentService.getStudentEntityById(paymentDto.getStudentId());
        payment.setStudent(student);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created: {id={}, studentId={}, amount={}, currency={}}",
                savedPayment.getId(), student.getId(), savedPayment.getAmount(), savedPayment.getCurrency());

        balanceTransactionService.createBalanceTransaction(student, TransactionType.PAYMENT_RECEIVED,
                savedPayment.getCurrency(), TransactionSource.PAYMENT, savedPayment.getAmount(), savedPayment.getId());

        return paymentMapper.toPaymentDto(savedPayment);
    }

    @Transactional
    public PaymentDto updatePayment(PaymentDto paymentDto) {
        log.info("Attempting to update payment from DTO: {}", paymentDto);
        Payment existingPayment = paymentRepository.findById(paymentDto.getId())
                .orElseThrow(() -> {
                    log.warn("Payment not found for update with id: {}", paymentDto.getId());
                    return new RuntimeException("Payment not found for update with id: " + paymentDto.getId());
                });

        BigDecimal oldAmount = existingPayment.getAmount();

        Student student = studentService.getStudentEntityById(paymentDto.getStudentId());

        paymentMapper.updatePaymentFromDto(paymentDto, existingPayment);
        existingPayment.setStudent(student);

        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Payment updated: {id={}, studentId={}, oldAmount={}, newAmount={}, currency={}}",
                updatedPayment.getId(), student.getId(), oldAmount, paymentDto.getAmount(), updatedPayment.getCurrency());

        BigDecimal amountDifference = paymentDto.getAmount().subtract(oldAmount);
        balanceTransactionService.createBalanceTransaction(student, TransactionType.PAYMENT_UPDATED,
                updatedPayment.getCurrency(), TransactionSource.PAYMENT, amountDifference, updatedPayment.getId());

        return paymentMapper.toPaymentDto(updatedPayment);
    }

    @Transactional
    public void deletePayment(Long id) {
        log.info("Attempting to delete payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment not found for deletion with id: {}", id);
                    return new RuntimeException("Payment not found for deletion with id: " + id);
                });

        Student student = payment.getStudent();

        paymentRepository.deleteById(id);
        // Record a negative transaction for deleted payment
        balanceTransactionService.createBalanceTransaction(student, TransactionType.PAYMENT_DELETED,
                payment.getCurrency(), TransactionSource.PAYMENT, payment.getAmount().negate(), payment.getId());

        log.info("Payment with ID {} deleted successfully.", id);
    }
}

