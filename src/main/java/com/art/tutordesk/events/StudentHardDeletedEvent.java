package com.art.tutordesk.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudentHardDeletedEvent {
    private final Long studentId;
}
