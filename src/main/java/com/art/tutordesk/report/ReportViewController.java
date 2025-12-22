package com.art.tutordesk.report;

import com.art.tutordesk.student.StudentDto;
import com.art.tutordesk.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportViewController {

    private final StudentService studentService;
    private final ReportService reportService;

    @GetMapping("/list")
    public String showReportPage(Model model) {
        // Get data for filters
        List<StudentDto> students = studentService.getAllActiveStudents();
        model.addAttribute("students", students);

        // Get data for metrics from the service
        model.addAttribute("lessonsThisMonth", reportService.getLessonsThisMonthCount());
        model.addAttribute("activeStudentsCount", reportService.getActiveStudentsCount());
        model.addAttribute("totalPayments", reportService.getTotalPaymentsThisMonth());

        return "report/list-reports";
    }

    @PostMapping("/generate")
    public String generateReport(@RequestParam("startDate") LocalDate startDate,
                                 @RequestParam("endDate") LocalDate endDate,
                                 @RequestParam(value = "selectedStudentIds", required = false) List<Long> selectedStudentIds,
                                 @RequestParam(value = "includeLessons", required = false) boolean includeLessons,
                                 @RequestParam(value = "includePayments", required = false) boolean includePayments,
                                 Model model) {

        List<ReportItemDto> reportItems = reportService.generateReport(startDate, endDate, selectedStudentIds, includeLessons, includePayments);
        model.addAttribute("reportItems", reportItems);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "report/view-report";
    }
}
