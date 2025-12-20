package com.art.tutordesk.payment;

import com.art.tutordesk.balance.BalanceService;
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
    private final BalanceService balanceService;
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
        Student student = studentService.getStudentById(paymentDto.getStudentId());
        payment.setStudent(student);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created: {id={}, studentId={}, amount={}, currency={}}",
                savedPayment.getId(), student.getId(), savedPayment.getAmount(), savedPayment.getCurrency());

        balanceService.changeBalance(student.getId(), savedPayment.getCurrency(), savedPayment.getAmount());
        balanceService.resyncPaymentStatus(student.getId());

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
        BigDecimal newAmount = paymentDto.getAmount();
        BigDecimal delta = newAmount.subtract(oldAmount);

        Student student = studentService.getStudentById(paymentDto.getStudentId());

        paymentMapper.updatePaymentFromDto(paymentDto, existingPayment);
        existingPayment.setStudent(student);

        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Payment updated: {id={}, studentId={}, oldAmount={}, newAmount={}, currency={}}",
                updatedPayment.getId(), student.getId(), oldAmount, newAmount, updatedPayment.getCurrency());

        balanceService.changeBalance(student.getId(), updatedPayment.getCurrency(), delta);
        balanceService.resyncPaymentStatus(student.getId());

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
        BigDecimal delta = payment.getAmount().negate();

        log.info("Deleting payment: {id={}, studentId={}, amount={}, currency={}}",
                payment.getId(), student.getId(), payment.getAmount(), payment.getCurrency());

        balanceService.changeBalance(student.getId(), payment.getCurrency(), delta);

        paymentRepository.deleteById(id);

        balanceService.resyncPaymentStatus(student.getId());
        log.info("Payment with ID {} deleted successfully.", id);
    }
}

