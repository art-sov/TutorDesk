package com.art.tutordesk.lesson;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String topic;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LessonStudent> lessonStudents = new HashSet<>();

    // Payment status will be derived from individual student payments for this lesson,
    // or managed through a dedicated LessonStudent join entity later if needed.
    // It's not a direct field on the Lesson entity itself due to its multi-student dependency.
}
