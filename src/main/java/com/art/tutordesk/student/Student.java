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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "students")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 50)
    private String knowledgeLevel; // "A2", "B1", "C1"

    @Column(length = 50)
    private String country;

    @Column(length = 50)
    private String phoneNumber;

    @Column(length = 500)
    private String globalGoal;

    private Integer age;

    @Column(name = "price_individual", nullable = false)
    private BigDecimal priceIndividual;

    @Column(name = "price_group", nullable = false)
    private BigDecimal priceGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LessonStudent> lessonStudents = new HashSet<>();
}

