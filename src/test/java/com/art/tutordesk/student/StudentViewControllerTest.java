package com.art.tutordesk.student;

import com.art.tutordesk.balance.BalanceService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Import(SecurityConfig.class)
@WebMvcTest(StudentViewController.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class StudentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private StudentService studentService;
    @MockitoBean
    private BalanceService balanceService;

    @Test
    void showAddStudentForm() throws Exception {
        mockMvc.perform(get("/students/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/add-student"))
                .andExpect(model().attributeExists("student"))
                .andExpect(model().attributeExists("currencies"));
    }

    @Test
    void createStudent_whenValid() throws Exception {
        StudentDto studentDto = createStudentDto();

        when(studentService.saveStudent(any(StudentDto.class))).thenReturn(studentDto);

        mockMvc.perform(post("/students/create")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("priceIndividual", "10")
                        .param("priceGroup", "1")
                        .param("currency", "USD")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));
    }

    @Test
    void createStudent_whenInvalid() throws Exception {
        mockMvc.perform(post("/students/create")
                        .param("firstName", "")
                        .with(csrf())) // Blank name
                .andExpect(status().isOk())
                .andExpect(view().name("student/add-student"))
                .andExpect(model().attributeHasFieldErrors("student", "firstName"));
    }

    @Test
    void showStudentList_whenShowActive() throws Exception {
        StudentDto studentDto = createStudentDto();
        studentDto.setActive(true);

        when(studentService.getAllActiveStudents()).thenReturn(Collections.singletonList(studentDto));

        mockMvc.perform(get("/students/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/list-students"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attribute("showInactive", false));
    }

    @Test
    void showStudentList_whenShowInactive() throws Exception {
        StudentDto studentDto = createStudentDto();
        studentDto.setActive(false);

        when(studentService.getAllStudentsIncludingInactive()).thenReturn(Collections.singletonList(studentDto));

        mockMvc.perform(get("/students/list").param("showInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/list-students"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attribute("showInactive", true));
    }

    @Test
    void deactivateStudent() throws Exception {
        doNothing().when(studentService).deactivateStudent(1L);

        mockMvc.perform(post("/students/deactivate/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));
    }

    @Test
    void hardDeleteStudent() throws Exception {
        doNothing().when(studentService).hardDeleteStudent(1L);

        mockMvc.perform(post("/students/hard-delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));
    }

    @Test
    void activateStudent() throws Exception {
        doNothing().when(studentService).activateStudent(1L);

        mockMvc.perform(post("/students/activate/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/profile/1"));
    }

    @Test
    void showEditStudentForm() throws Exception {
        StudentDto studentDto = createStudentDto();
        studentDto.setPriceIndividual(BigDecimal.TEN);
        studentDto.setPriceGroup(BigDecimal.ONE);
        studentDto.setCurrency(Currency.USD);

        when(studentService.getStudentById(1L)).thenReturn(studentDto);

        mockMvc.perform(get("/students/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/edit-student"))
                .andExpect(model().attribute("student", studentDto))
                .andExpect(model().attributeExists("currencies"));
    }

    @Test
    void updateStudent_whenValid() throws Exception {
        StudentDto studentDto = createStudentDto();
        studentDto.setPriceIndividual(new BigDecimal("12"));
        studentDto.setPriceGroup(new BigDecimal("2"));
        studentDto.setCurrency(Currency.EUR);

        when(studentService.saveStudent(any(StudentDto.class))).thenReturn(studentDto);

        mockMvc.perform(post("/students/update/1")
                        .param("firstName", "John")
                        .param("lastName", "Updated")
                        .param("priceIndividual", "12")
                        .param("priceGroup", "2")
                        .param("currency", "EUR")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/profile/1"));
    }

    @Test
    void updateStudent_whenInvalid() throws Exception {
        mockMvc.perform(post("/students/update/1")
                        .param("firstName", "") // Blank name
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("student/edit-student"))
                .andExpect(model().attributeHasFieldErrors("student", "firstName"));
    }

    @Test
    void showStudentProfile() throws Exception {
        StudentDto studentDto = createStudentDto();
        studentDto.setCurrency(Currency.USD);
        studentDto.setPriceIndividual(BigDecimal.TEN);
        studentDto.setPriceGroup(BigDecimal.ONE);

        when(studentService.getStudentById(1L)).thenReturn(studentDto);
        when(balanceService.getAllBalancesForStudent(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/profile/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/student-profile"))
                .andExpect(model().attribute("student", studentDto))
                .andExpect(model().attributeExists("balances"));
    }

    private static StudentDto createStudentDto() {
        StudentDto student = new StudentDto();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setPriceIndividual(BigDecimal.valueOf(50.00));
        student.setPriceGroup(BigDecimal.valueOf(30.00));
        student.setCurrency(Currency.USD);
        student.setActive(true);
        return student;
    }
}