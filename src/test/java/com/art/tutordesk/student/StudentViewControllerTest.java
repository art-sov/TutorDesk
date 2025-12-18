package com.art.tutordesk.student;

import com.art.tutordesk.balance.BalanceService;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(StudentViewController.class)
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
        Student student = createStudent();
        student.setPriceIndividual(BigDecimal.TEN);
        student.setPriceGroup(BigDecimal.ONE);
        student.setCurrency(Currency.USD);

        when(studentService.saveStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/students/create")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("priceIndividual", "10")
                        .param("priceGroup", "1")
                        .param("currency", "USD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));
    }

    @Test
    void createStudent_whenInvalid() throws Exception {
        mockMvc.perform(post("/students/create")
                        .param("firstName", "")) // Blank name
                .andExpect(status().isOk())
                .andExpect(view().name("student/add-student"))
                .andExpect(model().attributeHasFieldErrors("student", "firstName"));
    }

    @Test
    void showStudentList_whenShowActive() throws Exception {
        Student student = createStudent();
        student.setActive(true);

        when(studentService.getAllActiveStudents()).thenReturn(Collections.singletonList(student));

        mockMvc.perform(get("/students/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/list-students"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attribute("showInactive", false));
    }

    @Test
    void showStudentList_whenShowInactive() throws Exception {
        Student student = createStudent();
        student.setActive(false);

        when(studentService.getAllStudentsIncludingInactive()).thenReturn(Collections.singletonList(student));

        mockMvc.perform(get("/students/list").param("showInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/list-students"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attribute("showInactive", true));
    }

    @Test
    void deactivateStudent() throws Exception {
        doNothing().when(studentService).deactivateStudent(1L);

        mockMvc.perform(post("/students/deactivate/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));
    }

    @Test
    void hardDeleteStudent() throws Exception {
        doNothing().when(studentService).hardDeleteStudent(1L);

        mockMvc.perform(post("/students/hard-delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));
    }

    @Test
    void activateStudent() throws Exception {
        doNothing().when(studentService).activateStudent(1L);

        mockMvc.perform(post("/students/activate/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/profile/1"));
    }

    @Test
    void showEditStudentForm() throws Exception {
        Student student = createStudent();
        student.setPriceIndividual(BigDecimal.TEN);
        student.setPriceGroup(BigDecimal.ONE);
        student.setCurrency(Currency.USD);

        when(studentService.getStudentById(1L)).thenReturn(student);

        mockMvc.perform(get("/students/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/edit-student"))
                .andExpect(model().attribute("student", student))
                .andExpect(model().attributeExists("currencies"));
    }

    @Test
    void updateStudent_whenValid() throws Exception {
        Student student = createStudent();
        student.setPriceIndividual(BigDecimal.TEN);
        student.setPriceGroup(BigDecimal.ONE);
        student.setCurrency(Currency.USD);

        when(studentService.saveStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/students/update/1")
                        .param("firstName", "John")
                        .param("lastName", "Updated")
                        .param("priceIndividual", "12")
                        .param("priceGroup", "2")
                        .param("currency", "EUR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/profile/1"));
    }

    @Test
    void updateStudent_whenInvalid() throws Exception {
        mockMvc.perform(post("/students/update/1")
                        .param("firstName", "")) // Blank name
                .andExpect(status().isOk())
                .andExpect(view().name("student/edit-student"))
                .andExpect(model().attributeHasFieldErrors("student", "firstName"));
    }

    @Test
    void showStudentProfile() throws Exception {
        Student student = createStudent();
        student.setCurrency(Currency.USD);
        student.setPriceIndividual(BigDecimal.TEN);
        student.setPriceGroup(BigDecimal.ONE);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(balanceService.getAllBalancesForStudent(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/profile/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/student-profile"))
                .andExpect(model().attribute("student", student))
                .andExpect(model().attributeExists("balances"));
    }

    private static Student createStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        return student;
    }
}