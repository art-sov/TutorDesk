package com.art.tutordesk.student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentViewController {

    private final StudentService studentService;

    @GetMapping("/new")
    public String showAddStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "student/add-student";
    }

    @PostMapping("/create")
    public String createStudent(@ModelAttribute Student student) {
        studentService.saveStudent(student);
        // Пока перенаправляем на главную страницу, позже будет список студентов
        return "redirect:/";
    }
}
