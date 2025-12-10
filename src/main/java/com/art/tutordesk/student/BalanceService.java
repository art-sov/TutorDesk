package com.art.tutordesk.student;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final StudentRepository studentRepository;

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
            return;
        }
        Balance balance = getOrCreateBalance(student, lessonStudent.getCurrency());
        balance.setAmount(balance.getAmount().subtract(lessonStudent.getPrice()));
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }

    @Transactional
    public void updateBalanceOnLessonModification(LessonStudent lessonStudent, Student student, BigDecimal oldLessonCost) {
        if (lessonStudent.getPaymentStatus() == PaymentStatus.FREE) {
            return;
        }
        Balance balance = getOrCreateBalance(student, lessonStudent.getCurrency());
        BigDecimal currentLessonCost = lessonStudent.getPrice();
        balance.setAmount(balance.getAmount().add(oldLessonCost).subtract(currentLessonCost));
        balance.setLastUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);
    }

    @Transactional
    public void updateBalanceOnLessonDeletion(LessonStudent lessonStudent, Student student) {
        if (lessonStudent.getPaymentStatus() == PaymentStatus.FREE) {
            return;
        }
        Balance balance = getOrCreateBalance(student, lessonStudent.getCurrency());
        balance.setAmount(balance.getAmount().add(lessonStudent.getPrice())); // Lesson deleted, add cost back
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
    
    public Optional<Balance> getBalance(Long studentId, Currency currency) {
        return balanceRepository.findByStudentIdAndCurrency(studentId, currency);
    }
    
    public List<Balance> getAllBalancesForStudent(Long studentId) {
        return balanceRepository.findByStudentId(studentId);
    }
}