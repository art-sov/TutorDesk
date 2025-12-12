package com.art.tutordesk.lesson;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LessonListDTO {
    private Long id;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private String topic;
    private int studentsCount;
    private PaymentStatus paymentStatus;
}
