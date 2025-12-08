package com.art.tutordesk.lesson.controller;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.service.LessonService;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonViewController {

    private final LessonService lessonService;
    private final StudentService studentService;

    @GetMapping("/list")
    public String listLessons(Model model) {
        List<Lesson> lessons = lessonService.getAllLessonsSorted();
        model.addAttribute("lessons", lessons);
        return "lesson/list-lessons";
    }

    @GetMapping("/new")
    public String newLessonForm(Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("allStudents", studentService.getAllStudents());
        return "lesson/add-lesson";
    }

    @PostMapping("/create")
    public String createLesson(@ModelAttribute Lesson lesson,
                               @RequestParam(value = "selectedStudentIds", required = false) List<Long> selectedStudentIds) {
        lessonService.saveLesson(lesson, selectedStudentIds);
        return "redirect:/lessons/list";
    }
}
