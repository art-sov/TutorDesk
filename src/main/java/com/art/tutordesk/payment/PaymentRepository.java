package com.art.tutordesk.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPaymentDateGreaterThanEqual(LocalDate startDate);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
           "AND (:studentIds IS NULL OR p.student.id IN :studentIds)")
    List<Payment> findByFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("studentIds") List<Long> studentIds);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
