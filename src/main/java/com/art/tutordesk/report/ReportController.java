package com.art.tutordesk.report;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final StudentService studentService;
    private final ReportService reportService;

    @GetMapping("/list")
    public String showReportPage(Model model) {
        // Get data for filters
        List<Student> students = studentService.getAllActiveStudents();
        model.addAttribute("students", students);

        // Get data for metrics from the service
        model.addAttribute("lessonsThisMonth", reportService.getLessonsThisMonthCount());
        model.addAttribute("activeStudentsCount", reportService.getActiveStudentsCount());
        model.addAttribute("totalPayments", reportService.getTotalPaymentsThisMonth());

        return "report/list-reports";
    }
}
