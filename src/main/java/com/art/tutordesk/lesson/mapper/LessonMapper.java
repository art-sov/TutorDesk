package com.art.tutordesk.lesson.mapper;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonListDTO;
import com.art.tutordesk.lesson.LessonProfileDTO;
import com.art.tutordesk.lesson.PaymentStatusUtil;
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
