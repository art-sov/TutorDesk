package com.art.tutordesk.lesson.dto;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
public class LessonProfileDTO {
    private Long id;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private String topic;
    @ToString.Exclude
    private Set<LessonStudent> lessonStudents;
    private PaymentStatus paymentStatus;
}
