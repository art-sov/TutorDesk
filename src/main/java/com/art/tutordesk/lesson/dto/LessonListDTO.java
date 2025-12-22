package com.art.tutordesk.lesson.dto;

import com.art.tutordesk.lesson.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class LessonListDTO {
    private Long id;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private String topic;
    private List<String> studentNames;
    private PaymentStatus paymentStatus;
}
