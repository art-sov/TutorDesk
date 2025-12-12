package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Optional<Balance> findByStudentIdAndCurrency(Long studentId, Currency currency);

    List<Balance> findByStudentId(Long studentId);

    @Modifying
    @Query("DELETE FROM Balance b WHERE b.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}


