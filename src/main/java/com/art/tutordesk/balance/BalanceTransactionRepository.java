package com.art.tutordesk.balance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.tutordesk.payment.Currency;
import java.util.List;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {

    @Modifying
    void deleteByStudentId(Long studentId);

    List<BalanceTransaction> findByStudentIdAndCurrency(Long studentId, Currency currency);

    List<BalanceTransaction> findByStudentId(Long studentId);
}
