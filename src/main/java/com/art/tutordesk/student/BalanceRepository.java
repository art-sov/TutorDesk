package com.art.tutordesk.student;

import com.art.tutordesk.payment.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Optional<Balance> findByStudentIdAndCurrency(Long studentId, Currency currency);

    List<Balance> findByStudentId(Long studentId);
}
