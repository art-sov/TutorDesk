package com.art.tutordesk.lesson.controller;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonViewController {

    private final LessonService lessonService;

    @GetMapping("/list")
    public String listLessons(Model model) {
        List<Lesson> lessons = lessonService.getAllLessonsSorted();
        model.addAttribute("lessons", lessons);
        return "lesson/list-lessons";
    }
}
