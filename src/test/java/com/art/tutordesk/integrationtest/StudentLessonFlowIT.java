package com.art.tutordesk.integrationtest;

import com.art.tutordesk.BaseIntegrationTest;
import com.art.tutordesk.balance.BalanceQueryService;
import com.art.tutordesk.config.SecurityConfig;
import com.art.tutordesk.lesson.LessonStudentStatus;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class StudentLessonFlowIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonStudentRepository lessonStudentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BalanceQueryService balanceQueryService;

    private Student studentA;
    private Student studentB;

    @BeforeEach
    void setUp() {
        studentA = new Student();
        studentA.setFirstName("Student");
        studentA.setLastName("A");
        studentA.setPriceIndividual(new BigDecimal("25.00"));
        studentA.setPriceGroup(new BigDecimal("20.00"));
        studentA.setCurrency(Currency.USD);
        studentA.setActive(true);
        studentA = studentRepository.save(studentA);

        studentB = new Student();
        studentB.setFirstName("Student");
        studentB.setLastName("B");
        studentB.setPriceIndividual(new BigDecimal("30.00"));
        studentB.setPriceGroup(new BigDecimal("24.00"));
        studentB.setCurrency(Currency.PLN);
        studentB.setActive(true);
        studentB = studentRepository.save(studentB);
    }

    @Test
    void tc1_1_createLessonWithOneStudent() throws Exception {
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/list"));

        assertThat(lessonRepository.findAll()).hasSize(1);
        var lessonStudents = lessonStudentRepository.findAll();
        assertThat(lessonStudents).hasSize(1);
        var ls = lessonStudents.getFirst();
        assertThat(ls.getStudent().getId()).isEqualTo(studentA.getId());
        assertThat(ls.getStatus()).isEqualTo(LessonStudentStatus.SCHEDULED);
        assertThat(ls.getPrice()).isEqualByComparingTo("25.00");

        Map<Currency, BigDecimal> balances = balanceQueryService.getAllBalancesForStudent(studentA.getId());
        assertThat(balances.getOrDefault(Currency.USD, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void tc1_2_createLessonWithMultipleStudents() throws Exception {
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString(), studentB.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        var lessonStudents = lessonStudentRepository.findAll();
        assertThat(lessonStudents).hasSize(2);

        var lsA = lessonStudents.stream().filter(ls -> ls.getStudent().getId().equals(studentA.getId())).findFirst().orElseThrow();
        var lsB = lessonStudents.stream().filter(ls -> ls.getStudent().getId().equals(studentB.getId())).findFirst().orElseThrow();

        assertThat(lsA.getPrice()).isEqualByComparingTo("20.00");
        assertThat(lsB.getPrice()).isEqualByComparingTo("24.00");

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).getOrDefault(Currency.USD, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balanceQueryService.getAllBalancesForStudent(studentB.getId()).getOrDefault(Currency.PLN, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void tc3_1_chargeStudentByUpdatingStatus() throws Exception {
        // Setup: Create lesson SCHEDULED
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().getFirst().getId();

        // Step: Change SCHEDULED -> COMPLETED
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).get(Currency.USD)).isEqualByComparingTo("-25.00");
    }

    @Test
    void tc3_2_reverseChargeByUpdatingStatus() throws Exception {
        // Setup: Create lesson COMPLETED
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().getFirst().getId();
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).get(Currency.USD)).isEqualByComparingTo("-25.00");

        // Step: Change COMPLETED -> CANCELED
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "CANCELED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).getOrDefault(Currency.USD, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void tc3_4_balanceCorrectionOnPriceChange() throws Exception {
        // Setup: Lesson with Student A (COMPLETED) and Student B (SCHEDULED) -> Group price applies
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString(), studentB.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().getFirst().getId();
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .param("studentUpdates[1].studentId", studentB.getId().toString())
                        .param("studentUpdates[1].status", "SCHEDULED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).get(Currency.USD)).isEqualByComparingTo("-20.00");

        // Step: Remove Student B -> Student A becomes Individual price (25.00)
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        // Student B is NOT in studentUpdates, so it will be removed
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).get(Currency.USD)).isEqualByComparingTo("-25.00");
    }

    @Test
    void tc3_5_removeStudentFromChargedLesson() throws Exception {
        // Setup: Group lesson, both COMPLETED
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString(), studentB.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().getFirst().getId();
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .param("studentUpdates[1].studentId", studentB.getId().toString())
                        .param("studentUpdates[1].status", "COMPLETED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).get(Currency.USD)).isEqualByComparingTo("-20.00");
        assertThat(balanceQueryService.getAllBalancesForStudent(studentB.getId()).get(Currency.PLN)).isEqualByComparingTo("-24.00");

        // Step: Remove Student B
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Verifications
        assertThat(balanceQueryService.getAllBalancesForStudent(studentB.getId()).getOrDefault(Currency.PLN, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).get(Currency.USD)).isEqualByComparingTo("-25.00");
    }

    @Test
    void tc4_1_verifyDeleteDisabledInProfile() throws Exception {
        // Setup: Lesson COMPLETED
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().getFirst().getId();
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-12-20")
                        .param("studentUpdates[0].studentId", studentA.getId().toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Verify that profile view says it cannot be deleted
        mockMvc.perform(get("/lessons/profile/" + lessonId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canDeleteLesson", false));
    }

    @Test
    void tc4_2_deleteLessonAllScheduled() throws Exception {
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("selectedStudentIds", studentA.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().getFirst().getId();

        mockMvc.perform(post("/lessons/delete/" + lessonId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/list"));

        assertThat(lessonRepository.findById(lessonId)).isEmpty();
        assertThat(balanceQueryService.getAllBalancesForStudent(studentA.getId()).getOrDefault(Currency.USD, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void test_fullStudentLifecycle() throws Exception {
        // 1. Create student
        mockMvc.perform(post("/students/create")
                        .param("firstName", "Lifecycle")
                        .param("lastName", "Student")
                        .param("priceIndividual", "100.00")
                        .param("priceGroup", "80.00")
                        .param("currency", "USD")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getLastName().equals("Student"))
                .findFirst()
                .orElseThrow();

        Long sId = student.getId();

        // 2. Add lesson
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-11-20")
                        .param("selectedStudentIds", sId.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long lessonId = lessonRepository.findAll().stream()
                .filter(l -> l.getLessonDate().equals(LocalDate.of(2025, 11, 20)))
                .findFirst()
                .orElseThrow()
                .getId();

        // 3. Mark COMPLETED
        mockMvc.perform(post("/lessons/update/" + lessonId)
                        .param("lessonDate", "2025-11-20")
                        .param("studentUpdates[0].studentId", sId.toString())
                        .param("studentUpdates[0].status", "COMPLETED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(sId).get(Currency.USD)).isEqualByComparingTo("-100.00");

        // 4. Make payment
        mockMvc.perform(post("/payments/create")
                        .param("studentId", sId.toString())
                        .param("amount", "100.00")
                        .param("currency", "USD")
                        .param("paymentDate", "2025-11-20")
                        .param("paymentMethod", "CARD")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(balanceQueryService.getAllBalancesForStudent(sId).getOrDefault(Currency.USD, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);

        // 5. Deactivate
        mockMvc.perform(post("/students/deactivate/" + sId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTrue(studentRepository.findById(sId).isPresent());
        assertThat(studentRepository.findById(sId).get().isActive()).isFalse();

        // 6. Hard-delete
        mockMvc.perform(post("/students/hard-delete/" + sId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(studentRepository.findById(sId)).isEmpty();
        assertThat(paymentRepository.findAll().stream().anyMatch(p -> p.getStudent().getId().equals(sId))).isFalse();
        assertThat(lessonStudentRepository.findAll().stream().anyMatch(ls -> ls.getStudent().getId().equals(sId))).isFalse();
    }
}
