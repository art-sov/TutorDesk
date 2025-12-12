package com.art.tutordesk.student;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentViewController {

    private final StudentService studentService;
    private final BalanceService balanceService;

    @GetMapping("/new")
    public String showAddStudentForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("currencies", Currency.values());
        return "student/add-student";
    }

    @PostMapping("/create")
    public String createStudent(@ModelAttribute Student student) {
        studentService.saveStudent(student);
        return "redirect:/students/list";
    }

    @GetMapping("/list")
    public String showStudentList(@RequestParam(value = "showInactive", defaultValue = "false") boolean showInactive, Model model) {
        List<Student> students;
        if (showInactive) {
            students = studentService.getAllStudentsIncludingInactive();
        } else {
            students = studentService.getAllActiveStudents();
        }
        model.addAttribute("students", students);
        model.addAttribute("showInactive", showInactive);
        return "student/list-students";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateStudent(@PathVariable Long id) {
        studentService.deactivateStudent(id);
        return "redirect:/students/list";
    }

    @PostMapping("/hard-delete/{id}")
    public String hardDeleteStudent(@PathVariable Long id) {
        studentService.hardDeleteStudent(id);
        return "redirect:/students/list";
    }

    @PostMapping("/activate/{id}")
    public String activateStudent(@PathVariable Long id) {
        studentService.activateStudent(id);
        return "redirect:/students/profile/{id}";
    }

    @GetMapping("/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        model.addAttribute("currencies", Currency.values());
        return "student/edit-student";
    }

    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute("student") Student student) {
        student.setId(id);
        studentService.saveStudent(student);
        return "redirect:/students/profile/{id}";
    }

    @GetMapping("/profile/{id}")
    public String showStudentProfile(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        model.addAttribute("balances", balanceService.getAllBalancesForStudent(id));
        return "student/student-profile";
    }
}
