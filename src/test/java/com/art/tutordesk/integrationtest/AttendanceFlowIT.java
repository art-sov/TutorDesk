package com.art.tutordesk.integrationtest;

import com.art.tutordesk.balance.BalanceRepository;
import com.art.tutordesk.config.SecurityConfig;
import com.art.tutordesk.lesson.AttendanceStatus;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(SecurityConfig.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class AttendanceFlowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LessonStudentRepository lessonStudentRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private StudentRepository studentRepository;

    /**
     * This test simulates marking one student as ABSENT in a group lesson.
     * It verifies that:
     * 1. The absent student's lesson price becomes zero and their balance is adjusted (credited).
     * 2. The remaining student's lesson price doesn't change and remains as a group price.
     * 3. The remaining student's balance remains unchanged.
     */
    @Test
    void whenOneStudentMarkedAbsent_priceStaysGroupForRemainingStudent() throws Exception {
        // 1. Create two students and get their dynamic IDs
        Student student1 = new Student();
        student1.setFirstName("Student");
        student1.setLastName("A");
        student1.setPriceIndividual(new BigDecimal("60.00"));
        student1.setPriceGroup(new BigDecimal("40.00"));
        student1.setCurrency(Currency.EUR);
        student1 = studentRepository.save(student1);
        final Long student1Id = student1.getId();

        Student student2 = new Student();
        student2.setFirstName("Student");
        student2.setLastName("B");
        student2.setPriceIndividual(new BigDecimal("55.00"));
        student2.setPriceGroup(new BigDecimal("35.00"));
        student2.setCurrency(Currency.EUR);
        student2 = studentRepository.save(student2);
        final Long student2Id = student2.getId();

        // 2. Create one group lesson for both students
        Set<Long> idsBefore = lessonRepository.findAll().stream().map(Lesson::getId).collect(Collectors.toSet());

        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-11-15")
                        .param("selectedStudentIds", student1Id.toString(), student2Id.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Set<Long> idsAfter = lessonRepository.findAll().stream().map(Lesson::getId).collect(Collectors.toSet());
        idsAfter.removeAll(idsBefore);
        assertThat(idsAfter).hasSize(1);
        final Long lessonId = idsAfter.iterator().next();

        var initialBalanceS1 = balanceRepository.findByStudentIdAndCurrency(student1Id, Currency.EUR).orElseThrow();
        assertThat(initialBalanceS1.getAmount()).isEqualByComparingTo("-40.00");

        var initialBalanceS2 = balanceRepository.findByStudentIdAndCurrency(student2Id, Currency.EUR).orElseThrow();
        assertThat(initialBalanceS2.getAmount()).isEqualByComparingTo("-35.00");

        LessonStudent ls1 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student1Id).orElseThrow();
        assertThat(ls1.getPrice()).isEqualByComparingTo("40.00"); // Group price
        assertThat(ls1.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);

        LessonStudent ls2 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student2Id).orElseThrow();
        assertThat(ls2.getPrice()).isEqualByComparingTo("35.00"); // Group price

        // --- Action: Mark Student 1 as ABSENT ---
        mockMvc.perform(post("/lessons/{lessonId}/students/{studentId}/attendance", lessonId, student1Id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ABSENT\"}"))
                .andExpect(status().isOk());

        LessonStudent finalLs1 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student1Id).orElseThrow();
        assertThat(finalLs1.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(finalLs1.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
//        assertThat(finalLs1.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

        var finalBalanceS1 = balanceRepository.findByStudentIdAndCurrency(student1Id, Currency.EUR).orElseThrow();
        assertThat(finalBalanceS1.getAmount()).isEqualByComparingTo("0.00");

        LessonStudent finalLs2 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student2Id).orElseThrow();
        assertThat(finalLs2.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(finalLs2.getPrice()).isEqualByComparingTo("35.00"); // Price remained a group (35 for student 2)
//        assertThat(finalLs2.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);

        var finalBalanceS2 = balanceRepository.findByStudentIdAndCurrency(student2Id, Currency.EUR).orElseThrow();
        assertThat(finalBalanceS2.getAmount()).isEqualByComparingTo("-35.00"); // the balance didn't change
    }

    /**
     * This test simulates marking an absent student as PRESENT again in a group lesson.
     * It verifies that the student's price is restored to the group rate and their balance is debited accordingly.
     * The other student's price and balance should remain unaffected throughout.
     */
    @Test
    void whenAbsentStudentMarkedPresent_pricesAndBalancesAreRestored() throws Exception {
        // 1. Create two students and get their dynamic IDs
        Student student1 = new Student();
        student1.setFirstName("Student");
        student1.setLastName("A");
        student1.setPriceIndividual(new BigDecimal("60.00"));
        student1.setPriceGroup(new BigDecimal("40.00"));
        student1.setCurrency(Currency.EUR);
        student1 = studentRepository.save(student1);
        final Long student1Id = student1.getId();

        Student student2 = new Student();
        student2.setFirstName("Student");
        student2.setLastName("B");
        student2.setPriceIndividual(new BigDecimal("55.00"));
        student2.setPriceGroup(new BigDecimal("35.00"));
        student2.setCurrency(Currency.EUR);
        student2 = studentRepository.save(student2);
        final Long student2Id = student2.getId();

        // 2. Create one group lesson for both students
        Set<Long> idsBefore = lessonRepository.findAll().stream().map(Lesson::getId).collect(Collectors.toSet());

        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-11-15")
                        .param("selectedStudentIds", student1Id.toString(), student2Id.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Set<Long> idsAfter = lessonRepository.findAll().stream().map(Lesson::getId).collect(Collectors.toSet());
        idsAfter.removeAll(idsBefore);
        assertThat(idsAfter).hasSize(1);
        final Long lessonId = idsAfter.iterator().next();

        // --- Initial State Setup: Mark Student 1 as ABSENT ---
        mockMvc.perform(post("/lessons/{lessonId}/students/{studentId}/attendance", lessonId, student1Id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ABSENT\"}"))
                .andExpect(status().isOk());

        // Verify intermediate state
        var intermediateBalanceS1 = balanceRepository.findByStudentIdAndCurrency(student1Id, Currency.EUR).orElseThrow();
        assertThat(intermediateBalanceS1.getAmount()).isEqualByComparingTo("0.00"); // Balance restored

        // --- Action: Mark Student 1 as PRESENT again ---
        mockMvc.perform(post("/lessons/{lessonId}/students/{studentId}/attendance", lessonId, student1Id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PRESENT\"}"))
                .andExpect(status().isOk());

        // --- Final State Verification (Should match original state) ---
        // Student 1 is back, price reverts to group
        LessonStudent finalLs1 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student1Id).orElseThrow();
        assertThat(finalLs1.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(finalLs1.getPrice()).isEqualByComparingTo("40.00"); // Group price

        // Student 1's balance is debited again
        var finalBalanceS1 = balanceRepository.findByStudentIdAndCurrency(student1Id, Currency.EUR).orElseThrow();
        assertThat(finalBalanceS1.getAmount()).isEqualByComparingTo("-40.00");

        // Student 2's info should have remained constant
        LessonStudent finalLs2 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student2Id).orElseThrow();
        assertThat(finalLs2.getPrice()).isEqualByComparingTo("35.00"); // Group price

        // Student 2's balance should be the same as initial
        var finalBalanceS2 = balanceRepository.findByStudentIdAndCurrency(student2Id, Currency.EUR).orElseThrow();
        assertThat(finalBalanceS2.getAmount()).isEqualByComparingTo("-35.00");
    }

    /**
     * This test simulates marking all students in a lesson as ABSENT.
     * It verifies that both students' lesson prices become zero and their balances are adjusted
     * to remove the charge for this lesson (i.e., restored to zero).
     */
    @Test
    void whenAllStudentsMarkedAbsent_pricesBecomeZeroAndBalancesAreRestored() throws Exception {
        // 1. Create two students and get their dynamic IDs
        Student student1 = new Student();
        student1.setFirstName("Student");
        student1.setLastName("A");
        student1.setPriceIndividual(new BigDecimal("60.00"));
        student1.setPriceGroup(new BigDecimal("40.00"));
        student1.setCurrency(Currency.EUR);
        student1 = studentRepository.save(student1);
        final Long student1Id = student1.getId();

        Student student2 = new Student();
        student2.setFirstName("Student");
        student2.setLastName("B");
        student2.setPriceIndividual(new BigDecimal("55.00"));
        student2.setPriceGroup(new BigDecimal("35.00"));
        student2.setCurrency(Currency.EUR);
        student2 = studentRepository.save(student2);
        final Long student2Id = student2.getId();

        // 2. Create one group lesson for both students
        Set<Long> idsBefore = lessonRepository.findAll().stream().map(Lesson::getId).collect(Collectors.toSet());

        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-11-15")
                        .param("selectedStudentIds", student1Id.toString(), student2Id.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Set<Long> idsAfter = lessonRepository.findAll().stream().map(Lesson::getId).collect(Collectors.toSet());
        idsAfter.removeAll(idsBefore);
        assertThat(idsAfter).hasSize(1);
        final Long lessonId = idsAfter.iterator().next();

        // --- Initial State Verification ---
        assertThat(balanceRepository.findByStudentIdAndCurrency(student1Id, Currency.EUR).orElseThrow().getAmount()).isEqualByComparingTo("-40.00");
        assertThat(balanceRepository.findByStudentIdAndCurrency(student2Id, Currency.EUR).orElseThrow().getAmount()).isEqualByComparingTo("-35.00");

        // --- Action: Mark both students as ABSENT ---
        mockMvc.perform(post("/lessons/{lessonId}/students/{studentId}/attendance", lessonId, student1Id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ABSENT\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/lessons/{lessonId}/students/{studentId}/attendance", lessonId, student2Id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ABSENT\"}"))
                .andExpect(status().isOk());

        // --- Final State Verification ---
        // Student 1 is absent
        LessonStudent finalLs1 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student1Id).orElseThrow();
        assertThat(finalLs1.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(finalLs1.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);

        // Student 1 balance restored
        var finalBalanceS1 = balanceRepository.findByStudentIdAndCurrency(student1Id, Currency.EUR).orElseThrow();
        assertThat(finalBalanceS1.getAmount()).isEqualByComparingTo("0.00");

        // Student 2 is absent
        LessonStudent finalLs2 = lessonStudentRepository.findByLessonIdAndStudentId(lessonId, student2Id).orElseThrow();
        assertThat(finalLs2.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(finalLs2.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);

        // Student 2 balance restored
        var finalBalanceS2 = balanceRepository.findByStudentIdAndCurrency(student2Id, Currency.EUR).orElseThrow();
        assertThat(finalBalanceS2.getAmount()).isEqualByComparingTo("0.00");
    }
}
