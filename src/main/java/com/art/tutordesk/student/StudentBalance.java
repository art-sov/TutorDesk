package com.art.tutordesk.student;

import com.art.tutordesk.payment.Currency;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_balance", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "balance_currency"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private BigDecimal balanceAmount;

    @Enumerated(EnumType.STRING)
    private Currency balanceCurrency;

    private LocalDateTime lastUpdatedAt;
}
