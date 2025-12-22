package com.art.tutordesk.lesson.mapper;

import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.dto.LessonStudentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonStudentMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.firstName", target = "studentFirstName")
    @Mapping(source = "student.lastName", target = "studentLastName")
    LessonStudentDto toLessonStudentDto(LessonStudent lessonStudent);
}
