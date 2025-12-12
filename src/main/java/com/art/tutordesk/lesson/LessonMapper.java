package com.art.tutordesk.lesson;

import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = PaymentStatusUtil.class)
public interface LessonMapper {

    @Mappings({
        @Mapping(source = "lesson.lessonStudents", target = "paymentStatus"),
        @Mapping(target = "studentsCount", expression = "java(lesson.getLessonStudents().size())")
    })
    LessonListDTO toLessonListDTO(Lesson lesson);

    @Mapping(source = "lesson.lessonStudents", target = "paymentStatus")
    LessonProfileDTO toLessonProfileDTO(Lesson lesson);
}
