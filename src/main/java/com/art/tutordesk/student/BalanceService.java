package com.art.tutordesk.student;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.LessonStudentRepository;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final LessonStudentRepository lessonStudentRepository;

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

        BigDecimal lessonPrice = lessonStudent.getPrice();
        Balance balance = getOrCreateBalance(student, lessonStudent.getCurrency());
        BigDecimal currentAmount = balance.getAmount();

        // Check if balance is sufficient BEFORE debiting
        if (currentAmount.compareTo(lessonPrice) >= 0) {
            lessonStudent.setPaymentStatus(PaymentStatus.PAID);
            lessonStudentRepository.save(lessonStudent); // Save the updated status
        }

        // Always debit the balance for the lesson
        balance.setAmount(currentAmount.subtract(lessonPrice));
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
    public void settleUnpaidLessons(Student student) {
        List<Balance> studentBalances = balanceRepository.findByStudentId(student.getId());

        // Settle debts for each currency the student has a balance in
        for (Balance balance : studentBalances) {
            if (balance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Skip if balance is not positive
            }

            List<LessonStudent> unpaidLessons = lessonStudentRepository
                    .findAllByStudentAndPaymentStatusAndCurrencyOrderByLessonLessonDateAsc(
                            student, PaymentStatus.UNPAID, balance.getCurrency());

            for (LessonStudent unpaidLesson : unpaidLessons) {
                BigDecimal lessonPrice = unpaidLesson.getPrice();
                if (balance.getAmount().compareTo(lessonPrice) >= 0) {
                    // Pay for the lesson
                    balance.setAmount(balance.getAmount().subtract(lessonPrice));
                    unpaidLesson.setPaymentStatus(PaymentStatus.PAID);
                    lessonStudentRepository.save(unpaidLesson);
                } else {
                    // Not enough money for the next oldest lesson, so stop for this currency
                    break;
                }
            }
            balance.setLastUpdatedAt(LocalDateTime.now());
            balanceRepository.save(balance);
        }
    }

    @Transactional
    public void refundLesson(LessonStudent lessonStudent) {
        if (lessonStudent.getPaymentStatus() == PaymentStatus.FREE) {
            return;
        }
        Balance balance = getOrCreateBalance(lessonStudent.getStudent(), lessonStudent.getCurrency());
        balance.setAmount(balance.getAmount().add(lessonStudent.getPrice()));
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }
}