package com.art.tutordesk.report;

import com.art.tutordesk.config.SecurityConfig;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Import(SecurityConfig.class)
@WebMvcTest(ReportViewController.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class ReportViewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private StudentService studentService;
    @MockitoBean
    private ReportService reportService;

    @Test
    void showReportPage() throws Exception {
        Map<Currency, BigDecimal> paymentsMap = Collections.singletonMap(Currency.USD, BigDecimal.valueOf(1000));

        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());
        when(reportService.getLessonsThisMonthCount()).thenReturn(10L);
        when(reportService.getActiveStudentsCount()).thenReturn(5L);
        when(reportService.getTotalPaymentsThisMonth()).thenReturn(paymentsMap);

        mockMvc.perform(get("/reports/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/list-reports"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attribute("lessonsThisMonth", 10L))
                .andExpect(model().attribute("activeStudentsCount", 5L))
                .andExpect(model().attribute("totalPayments", paymentsMap));
    }

    @Test
    void generateReport() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(reportService.generateReport(any(LocalDate.class), any(LocalDate.class), anyList(), anyBoolean(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/reports/generate")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("selectedStudentIds", "1", "2")
                        .param("includeLessons", "true")
                        .param("includePayments", "false")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("report/view-report"))
                .andExpect(model().attributeExists("reportItems"))
                .andExpect(model().attribute("startDate", startDate))
                .andExpect(model().attribute("endDate", endDate));
    }
}
