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
    @Mapping(source = "student.priceIndividual", target = "priceIndividual")
    @Mapping(source = "student.priceGroup", target = "priceGroup")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "currency", target = "currency")
    LessonStudentDto toLessonStudentDto(LessonStudent lessonStudent);
}
