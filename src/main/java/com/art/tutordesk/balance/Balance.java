package com.art.tutordesk.balance;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "student_balance", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "balance_currency"}))
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;
}
