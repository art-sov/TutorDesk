package com.art.tutordesk.lesson.mapper;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.dto.LessonStudentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = LessonStudentMapper.class)
public abstract class LessonMapper {

    @Autowired
    protected LessonStudentMapper lessonStudentMapper;

    @Mapping(target = "studentNames", expression = "java(mapLessonStudentsToNames(lesson.getLessonStudents()))")
    @Mapping(target = "paymentStatus", ignore = true)
    public abstract LessonListDTO toLessonListDTO(Lesson lesson);

    @Mapping(target = "studentAssociations", source = "lessonStudents")
    @Mapping(target = "paymentStatus", ignore = true)
    public abstract LessonProfileDTO toLessonProfileDTO(Lesson lesson);

    public List<LessonStudentDto> mapLessonStudentsToLessonStudentDtoList(Set<LessonStudent> lessonStudents) {
        if (lessonStudents == null) {
            return Collections.emptyList();
        }
        return lessonStudents.stream()
                .map(lessonStudentMapper::toLessonStudentDto)
                .collect(Collectors.toList());
    }

    public List<String> mapLessonStudentsToNames(Set<LessonStudent> lessonStudents) {
        if (lessonStudents == null) {
            return Collections.emptyList();
        }
        return lessonStudents.stream()
                .map(ls -> ls.getStudent().getFirstName() + " " + ls.getStudent().getLastName())
                .collect(Collectors.toList());
    }
}