package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceTransactionService {

    private final BalanceTransactionRepository balanceTransactionRepository;

    @Transactional
    public void createBalanceTransaction(Student student, TransactionType transactionType, Currency currency,
                                         TransactionSource sourceEntity,BigDecimal amount, Long sourceId) {

        BalanceTransaction transaction = new BalanceTransaction();
        transaction.setStudent(student);
        transaction.setType(transactionType);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setSourceEntity(sourceEntity);
        transaction.setSourceId(sourceId);

        balanceTransactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransactionsByStudentId(Long studentId) {
        balanceTransactionRepository.deleteByStudentId(studentId);
        log.info("Successfully deleted balance transactions for student with ID: {}", studentId);
    }
}
