package com.art.tutordesk.balance;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final LessonStudentRepository lessonStudentRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Balance getOrCreateBalance(Student student, Currency currency) {
        return balanceRepository.findByStudentIdAndCurrency(student.getId(), currency)
                .orElseGet(() -> {
                    Balance newBalance = new Balance();
                    newBalance.setStudent(student);
                    newBalance.setAmount(BigDecimal.ZERO);
                    newBalance.setCurrency(currency);
                    newBalance.setLastUpdatedAt(LocalDateTime.now());
                    return balanceRepository.save(newBalance);
                });
    }

    @Transactional
    public void updateBalanceOnLessonCreation(LessonStudent lessonStudent, Student student) {
        if (lessonStudent.getPaymentStatus() == PaymentStatus.FREE) {
            return; // Do nothing for FREE lessons
        }
        // Always debit the balance for the lesson
        BigDecimal lessonPrice = lessonStudent.getPrice();
        Balance balance = getOrCreateBalance(student, lessonStudent.getCurrency());
        balance.setAmount(balance.getAmount().subtract(lessonPrice));
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }

    @Transactional
    public void updateBalanceOnPaymentCreation(Payment payment, Student student) {
        Balance balance = getOrCreateBalance(student, payment.getCurrency());
        balance.setAmount(balance.getAmount().add(payment.getAmount())); // Payment created: add its amount
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }

    @Transactional
    public void updateBalanceOnPaymentModification(Payment payment, Student student, BigDecimal oldPaymentAmount) {
        Balance balance = getOrCreateBalance(student, payment.getCurrency());
        BigDecimal currentPaymentAmount = payment.getAmount();
        balance.setAmount(balance.getAmount().subtract(oldPaymentAmount).add(currentPaymentAmount)); // Remove old amount, add new amount
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }

    @Transactional
    public void updateBalanceOnPaymentDeletion(Payment payment, Student student) {
        Balance balance = getOrCreateBalance(student, payment.getCurrency());
        balance.setAmount(balance.getAmount().subtract(payment.getAmount())); // Payment deleted, subtract amount
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }

    @Transactional
    public void resetBalancesForStudent(Long studentId) {
        List<Balance> balances = balanceRepository.findByStudentId(studentId);
        if (!balances.isEmpty()) {
            balanceRepository.deleteAll(balances);
        }
    }

    public List<Balance> getAllBalancesForStudent(Long studentId) {
        return balanceRepository.findByStudentId(studentId);
    }

    @Transactional
    public void resyncPaymentStatus(Student student) {
        List<Currency> currencies = balanceRepository.findByStudentId(student.getId()).stream()
                .map(Balance::getCurrency)
                .distinct()
                .collect(Collectors.toList());

        for (Currency currency : currencies) {
            // 1. Get all payments and sum them up.
            List<Payment> payments = paymentRepository.findAllByStudentAndCurrencyOrderByPaymentDateAsc(student, currency);
            BigDecimal totalPayments = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 2. Get all lessons (that are not FREE) and iterate through them chronologically.
            List<LessonStudent> lessons = lessonStudentRepository.findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(student, currency, PaymentStatus.FREE);

            BigDecimal runningBalance = totalPayments;
            boolean changed = false;

            for (LessonStudent lesson : lessons) {
                // Subtract the price of the current lesson to find the balance at this point in time
                runningBalance = runningBalance.subtract(lesson.getPrice());
                
                // If the running balance was non-negative before this lesson, it's considered paid.
                PaymentStatus newStatus = (runningBalance.compareTo(BigDecimal.ZERO) >= 0) ? PaymentStatus.PAID : PaymentStatus.UNPAID;

                if (lesson.getPaymentStatus() != newStatus) {
                    lesson.setPaymentStatus(newStatus);
                    changed = true;
                }
            }

            if (changed) {
                lessonStudentRepository.saveAll(lessons);
            }
        }
    }

    @Transactional
    public void reverseLessonDebit(LessonStudent lessonStudent) {
        if (lessonStudent.getPaymentStatus() == PaymentStatus.FREE) {
            return;
        }
        Balance balance = getOrCreateBalance(lessonStudent.getStudent(), lessonStudent.getCurrency());
        // Always add the price back to the balance, effectively reversing the debit
        balance.setAmount(balance.getAmount().add(lessonStudent.getPrice()));
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }
}
