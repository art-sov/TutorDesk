package com.art.tutordesk.events;

import com.art.tutordesk.lesson.LessonStudent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LessonStudentCreatedEvent {
    private final LessonStudent lessonStudent;
}
