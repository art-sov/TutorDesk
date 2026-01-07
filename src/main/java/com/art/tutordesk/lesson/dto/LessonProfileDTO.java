package com.art.tutordesk.lesson.dto;

import com.art.tutordesk.lesson.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class LessonProfileDTO {

    private Long id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lessonDate;
    private List<LessonStudentDto> studentAssociations;
    private PaymentStatus paymentStatus;
}
