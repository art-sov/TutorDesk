package com.art.tutordesk.lesson.dto;

import com.art.tutordesk.lesson.LessonStudentStatus;
import lombok.Data;

@Data
public class LessonStudentUpdateDTO {
    private Long studentId;
    private LessonStudentStatus status;
}
