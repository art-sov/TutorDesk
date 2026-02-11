package com.art.tutordesk.balance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.art.tutordesk.payment.Currency;
import java.util.List;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {
    List<BalanceTransaction> findByStudentIdAndCurrencyOrderByTransactionDateTimeDesc(Long studentId, Currency currency);

    void deleteByStudentId(Long studentId);
}
