package com.art.tutordesk.lesson;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "lessonStudents")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Lesson date is mandatory")
    private LocalDate lessonDate;

    @NotNull(message = "Start time is mandatory")
    private LocalTime startTime;

    @Size(max = 200, message = "Topic cannot exceed 200 characters")
    private String topic;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "lesson", orphanRemoval = true)
    private Set<LessonStudent> lessonStudents = new HashSet<>();
}
