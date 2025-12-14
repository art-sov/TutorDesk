package com.art.tutordesk.payment;

import com.art.tutordesk.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPaymentDateGreaterThanEqual(LocalDate startDate);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND (:studentIds IS NULL OR p.student.id IN :studentIds)")
    List<Payment> findByFilters(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                @Param("studentIds") List<Long> studentIds);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);

    @Query("""
            SELECT coalesce(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.student = :student AND p.currency = :currency
            """)
    BigDecimal sumPayments(@Param("student") Student student, @Param("currency") Currency currency);
}
