package com.art.tutordesk.lesson.controller;

import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.service.LessonService;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

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
        model.addAttribute("allStudents", studentService.getAllActiveStudents());
        return "lesson/add-lesson";
    }

    @PostMapping("/create")
    public String createLesson(@ModelAttribute Lesson lesson,
                               @RequestParam(value = "selectedStudentIds", required = false) List<Long> selectedStudentIds,
                               RedirectAttributes redirectAttributes) {
        lessonService.saveLesson(lesson, selectedStudentIds);
        redirectAttributes.addFlashAttribute("message", "Lesson created successfully!");
        return "redirect:/lessons/list";
    }

    @GetMapping("/profile/{id}")
    public String showLessonProfile(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id);
        model.addAttribute("lesson", lesson);
        return "lesson/lesson-profile";
    }

    @GetMapping("/edit/{id}")
    public String editLessonForm(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id);
        model.addAttribute("lesson", lesson);
        model.addAttribute("allStudents", studentService.getAllActiveStudents());

        // Get IDs of students already associated with this lesson
        List<Long> selectedStudentIds = lesson.getLessonStudents().stream()
                .map(LessonStudent::getStudent)
                .map(Student::getId)
                .collect(Collectors.toList());
        model.addAttribute("selectedStudentIds", selectedStudentIds);
        
        return "lesson/edit-lesson";
    }

    @PostMapping("/update/{id}")
    public String updateLesson(@PathVariable Long id,
                               @ModelAttribute Lesson lesson,
                               @RequestParam(value = "selectedStudentIds", required = false) List<Long> selectedStudentIds,
                               RedirectAttributes redirectAttributes) {
        lesson.setId(id); // Ensure the lesson ID is set for update
        lessonService.updateLesson(lesson, selectedStudentIds); // Call updateLesson instead of saveLesson
        redirectAttributes.addFlashAttribute("message", "Lesson updated successfully!");
        return "redirect:/lessons/profile/{id}";
    }

    @PostMapping("/delete/{id}")
    public String deleteLesson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lessonService.deleteLesson(id);
        redirectAttributes.addFlashAttribute("message", "Lesson deleted successfully!");
        return "redirect:/lessons/list";
    }
}
