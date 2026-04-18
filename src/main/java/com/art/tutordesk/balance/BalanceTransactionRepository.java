package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {

    void deleteByStudentId(Long studentId);

    List<BalanceTransaction> findByStudentIdAndCurrency(Long studentId, Currency currency);

    List<BalanceTransaction> findByStudentId(Long studentId);
}
