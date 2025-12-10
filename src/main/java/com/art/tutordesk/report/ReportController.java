package com.art.tutordesk.report;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.art.tutordesk.payment.Currency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final StudentService studentService;

    @GetMapping("/list")
    public String showReportPage(Model model) {
        // Get data for filters
        List<Student> students = studentService.getAllActiveStudents();
        model.addAttribute("students", students);

        // Add mock data
        model.addAttribute("lessonsThisMonth", 25); // Mock value
        model.addAttribute("activeStudentsCount", students.size());

        // Mock payment sums
        Map<Currency, BigDecimal> totalPayments = new HashMap<>();
        totalPayments.put(Currency.USD, new BigDecimal("1250.00"));
        totalPayments.put(Currency.EUR, new BigDecimal("800.50"));
        totalPayments.put(Currency.UAH, new BigDecimal("15000.00"));
        model.addAttribute("totalPayments", totalPayments);

        return "report/list-reports";
    }
}
