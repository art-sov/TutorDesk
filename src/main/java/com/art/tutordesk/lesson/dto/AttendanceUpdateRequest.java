package com.art.tutordesk.lesson.dto;

import com.art.tutordesk.lesson.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceUpdateRequest {
    private AttendanceStatus status;
}
