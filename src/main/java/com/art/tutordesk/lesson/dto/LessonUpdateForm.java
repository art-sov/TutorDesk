package com.art.tutordesk.lesson.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class LessonUpdateForm {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lessonDate;

    private List<LessonStudentUpdateDTO> studentUpdates = new ArrayList<>();
}
