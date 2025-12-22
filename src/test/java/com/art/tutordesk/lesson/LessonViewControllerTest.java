package com.art.tutordesk.lesson;

import com.art.tutordesk.config.SecurityConfig;
import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.dto.LessonStudentDto;
import com.art.tutordesk.lesson.service.LessonService;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.StudentDto;
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Import(SecurityConfig.class)
@WebMvcTest(LessonViewController.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class LessonViewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private LessonService lessonService;
    @MockitoBean
    private StudentService studentService;

    @Test
    void listLessons() throws Exception {
        when(lessonService.getLessonsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/lessons/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/list-lessons"))
                .andExpect(model().attributeExists("lessons", "startDate", "endDate"));
    }

    @Test
    void listLessons_withDateParams() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(lessonService.getLessonsByDateRange(startDate, endDate))
                .thenReturn(Collections.singletonList(new LessonListDTO()));

        mockMvc.perform(get("/lessons/list")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/list-lessons"))
                .andExpect(model().attribute("startDate", startDate))
                .andExpect(model().attribute("endDate", endDate));
    }

    @Test
    void newLessonForm() throws Exception {
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/lessons/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/add-lesson"))
                .andExpect(model().attributeExists("lesson", "allStudents"));
    }

    @Test
    void createLesson_whenValid() throws Exception {
        when(lessonService.saveLesson(any(Lesson.class), anyList())).thenReturn(new Lesson());

        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-25")
                        .param("startTime", "10:00")
                        .param("selectedStudentIds", "1", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/list"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void createLesson_whenInvalid_noStudents() throws Exception {
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-25")
                        .param("startTime", "10:00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/add-lesson"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("lesson"));
    }

    @Test
    void createLesson_whenInvalid_bindingResultErrors() throws Exception {
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/lessons/create")
                        .param("selectedStudentIds", "1").with(csrf())) // Missing date and time
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/add-lesson"))
                .andExpect(model().attributeHasFieldErrors("lesson", "lessonDate", "startTime"));
    }

    @Test
    void showLessonProfile() throws Exception {
        LessonProfileDTO lesson = new LessonProfileDTO();
        lesson.setId(1L);
        lesson.setLessonDate(LocalDate.now());
        lesson.setStartTime(LocalTime.now());
        lesson.setStudentAssociations(Collections.emptyList());

        when(lessonService.getLessonById(1L)).thenReturn(lesson);

        mockMvc.perform(get("/lessons/profile/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/lesson-profile"))
                .andExpect(model().attribute("lesson", lesson));
    }

    @Test
    void editLessonForm() throws Exception {
        LessonStudentDto lessonStudentDto = new LessonStudentDto();
        lessonStudentDto.setStudentId(1L);

        LessonProfileDTO lesson = new LessonProfileDTO();
        lesson.setId(1L);
        lesson.setStudentAssociations(List.of(lessonStudentDto));

        when(lessonService.getLessonById(1L)).thenReturn(lesson);
        when(studentService.getAllActiveStudents()).thenReturn(Collections.singletonList(createStudentDto()));

        mockMvc.perform(get("/lessons/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/edit-lesson"))
                .andExpect(model().attributeExists("lesson", "allStudents", "selectedStudentIds"));
    }

    @Test
    void updateLesson_whenValid() throws Exception {
        when(lessonService.updateLesson(any(Lesson.class), anyList())).thenReturn(new Lesson());

        mockMvc.perform(post("/lessons/update/1")
                        .param("lessonDate", "2025-12-26")
                        .param("startTime", "11:00")
                        .param("selectedStudentIds", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/profile/1"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void updateLesson_whenInvalid_noStudents() throws Exception {
        LessonProfileDTO lesson = new LessonProfileDTO();
        lesson.setId(1L);
        lesson.setStudentAssociations(Collections.emptyList());

        when(lessonService.getLessonById(1L)).thenReturn(lesson);
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/lessons/update/1")
                        .param("lessonDate", "2025-12-26")
                        .param("startTime", "11:00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/edit-lesson"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("lesson"));
    }

    @Test
    void updateLesson_whenInvalid_bindingResultErrors() throws Exception {
        LessonProfileDTO lesson = new LessonProfileDTO();
        lesson.setId(1L);
        lesson.setStudentAssociations(Collections.emptyList());

        when(lessonService.getLessonById(1L)).thenReturn(lesson);
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/lessons/update/1")
                        .param("selectedStudentIds", "1").with(csrf())) // missing date/time
                .andExpect(status().isOk())
                .andExpect(view().name("lesson/edit-lesson"))
                .andExpect(model().attributeHasFieldErrors("lesson", "lessonDate", "startTime"));
    }

    @Test
    void deleteLesson() throws Exception {
        doNothing().when(lessonService).deleteLesson(1L);

        mockMvc.perform(post("/lessons/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/list"))
                .andExpect(flash().attributeExists("message"));
    }

    private static StudentDto createStudentDto() {
        StudentDto student = new StudentDto();
        student.setId(1L);
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setPriceIndividual(BigDecimal.TEN);
        student.setPriceGroup(BigDecimal.ONE);
        student.setCurrency(Currency.USD);
        student.setActive(true);
        return student;
    }
}