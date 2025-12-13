package com.art.tutordesk.lesson;

import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = PaymentStatusUtil.class)
public interface LessonMapper {

    @Mappings({
        @Mapping(source = "lessonStudents", target = "paymentStatus"),
        @Mapping(target = "studentNames", expression = "java(mapLessonStudentsToNames(lesson.getLessonStudents()))")
    })
    LessonListDTO toLessonListDTO(Lesson lesson);

    @Mapping(source = "lessonStudents", target = "paymentStatus")
    LessonProfileDTO toLessonProfileDTO(Lesson lesson);

    default List<String> mapLessonStudentsToNames(Set<LessonStudent> lessonStudents) {
        if (lessonStudents == null) {
            return Collections.emptyList();
        }
        return lessonStudents.stream()
                .map(ls -> ls.getStudent().getFirstName() + " " + ls.getStudent().getLastName())
                .collect(Collectors.toList());
    }
}
