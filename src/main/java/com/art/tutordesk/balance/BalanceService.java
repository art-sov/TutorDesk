package com.art.tutordesk.balance;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final LessonStudentRepository lessonStudentRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    public List<Balance> getAllBalancesForStudent(Long studentId) {
        return balanceRepository.findByStudentId(studentId);
    }

    @Transactional
    public void changeBalance(Long studentId, Currency currency, BigDecimal delta) {
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("No balance change for student {} currency {} as delta is zero.", studentId, currency);
            return; // No change
        }
        Balance balance = getOrCreateBalance(studentId, currency);
        BigDecimal oldAmount = balance.getAmount();
        balance.setAmount(balance.getAmount().add(delta));
        log.info("Balance changed for student {}: currency={}, oldAmount={}, delta={}, newAmount={}",
                studentId, currency, oldAmount, delta, balance.getAmount());
        // No explicit save needed due to dirty checking
    }
    
    private Balance getOrCreateBalance(Long studentId, Currency currency) {
        return balanceRepository.findByStudentIdAndCurrency(studentId, currency)
                .orElseGet(() -> {
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> {
                                log.warn("Student not found with id: {} when getting or creating balance.", studentId);
                                return new IllegalStateException("Student not found with id: " + studentId);
                            });
                    Balance newBalance = new Balance();
                    newBalance.setStudent(student);
                    newBalance.setAmount(BigDecimal.ZERO);
                    newBalance.setCurrency(currency);
                    Balance savedBalance = balanceRepository.save(newBalance);
                    log.info("New balance created for student {} currency {}: amount={}",
                            studentId, currency, savedBalance.getAmount());
                    return savedBalance;
                });
    }

    @Transactional
    public void resyncPaymentStatus(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found with id: {} during payment status resync.", studentId);
                    return new RuntimeException("Student not found with id: " + studentId);
                });
        log.info("Resyncing payment status for student: {}", studentId);

        Set<Currency> currencies = new HashSet<>();
        currencies.addAll(lessonStudentRepository.findCurrenciesByStudentId(studentId));
        currencies.addAll(balanceRepository.findCurrenciesByStudentId(studentId));

        for (Currency currency : currencies) {
            resyncPaymentStatusForCurrency(student, currency);
        }
        log.info("Finished resyncing payment status for student: {}", studentId);
    }

    private void resyncPaymentStatusForCurrency(Student student, Currency currency) {
        BigDecimal credit = paymentRepository.sumPayments(student, currency);

        List<LessonStudent> lessons =
                lessonStudentRepository
                        .findAllByStudentAndCurrencyAndPaymentStatusNotOrderByLessonLessonDateAsc(
                                student, currency, PaymentStatus.FREE);

        for (LessonStudent lesson : lessons) {
            PaymentStatus oldStatus = lesson.getPaymentStatus();
            if (credit.compareTo(lesson.getPrice()) >= 0) {
                lesson.setPaymentStatus(PaymentStatus.PAID);
                credit = credit.subtract(lesson.getPrice());
            } else {
                lesson.setPaymentStatus(PaymentStatus.UNPAID);
            }
            if (oldStatus != lesson.getPaymentStatus()) {
                log.info("LessonStudent {}: payment status changed from {} to {} for student {} lesson {}.",
                        lesson.getId(), oldStatus, lesson.getPaymentStatus(), student.getId(), lesson.getLesson().getId());
            }
        }
        log.debug("Resync for student {} currency {} completed. Remaining credit: {}", student.getId(), currency, credit);
    }
}
