package com.art.tutordesk.student;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.payment.Currency;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "students")
@EntityListeners(AuditingEntityListener.class)
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Size(max = 50, message = "Knowledge level cannot exceed 50 characters")
    private String knowledgeLevel; // "A2", "B1", "C1"

    @Size(max = 50, message = "Country cannot exceed 50 characters")
    private String country;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Global goal cannot exceed 500 characters")
    private String globalGoal;
    private Integer age;

    @NotNull(message = "Individual price is mandatory")
    @DecimalMin(value = "0.00", message = "Individual price must be non-negative")
    @Column(name = "price_individual", nullable = false)
    private BigDecimal priceIndividual;

    @NotNull(message = "Group price is mandatory")
    @DecimalMin(value = "0.00", message = "Group price must be non-negative")
    @Column(name = "price_group", nullable = false)
    private BigDecimal priceGroup;

    @NotNull(message = "Currency is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;
    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LessonStudent> lessonStudents = new HashSet<>();
}

