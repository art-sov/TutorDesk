package com.art.tutordesk.integrationtest;

import com.art.tutordesk.balance.Balance;
import com.art.tutordesk.balance.BalanceRepository;
import com.art.tutordesk.lesson.Lesson;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.lesson.service.LessonService;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/sql/cleanup.sql")
public class StudentLessonFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private LessonStudentRepository lessonStudentRepository;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void test_createStudentAndAddLesson() throws Exception {
        // 1. Create a new student via an HTTP POST request
        mockMvc.perform(post("/students/create")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("priceIndividual", "50.00")
                        .param("priceGroup", "30.00")
                        .param("currency", "USD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));

        // 2. Verify the student is in the database
        List<Student> students = studentRepository.findAll();
        assertThat(students).hasSize(1);
        Student student = students.get(0);
        assertThat(student.getFirstName()).isEqualTo("John");
        assertThat(student.getPriceIndividual()).isEqualByComparingTo("50.00");

        // 3. Create a new lesson for this student
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-12-20")
                        .param("startTime", "10:00")
                        .param("selectedStudentIds", student.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/list"));

        // 4. Verify LessonStudent is created and has correct status
        List<LessonStudent> lessonStudents = lessonStudentRepository.findAll();
        assertThat(lessonStudents).hasSize(1);
        LessonStudent lessonStudent = lessonStudents.get(0);
        assertThat(lessonStudent.getStudent().getId()).isEqualTo(student.getId());
        assertThat(lessonStudent.getLesson().getLessonDate()).isEqualTo("2025-12-20");
        assertThat(lessonStudent.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(lessonStudent.getPrice()).isEqualByComparingTo("50.00"); // Individual lesson price

        // 5. Verify the student's balance is created and debited
        Optional<Balance> balanceOpt = balanceRepository.findByStudentIdAndCurrency(student.getId(), Currency.USD);
        assertThat(balanceOpt).isPresent();
        Balance balance = balanceOpt.get();
        // The balance should be the negative of the lesson price
        assertThat(balance.getAmount()).isEqualByComparingTo("-50.00");
    }

    @Test
    void test_paymentCoversMultipleLessonsChronologically() throws Exception {
        // --- Pre-requisite: A student with three unpaid lessons exists ---
        // 1. Create Student
        Student student = new Student();
        student.setFirstName("Jane");
        student.setLastName("Doe");
        student.setPriceIndividual(new BigDecimal("60.00"));
        student.setPriceGroup(new BigDecimal("40.00"));
        student.setCurrency(Currency.EUR);
        Student savedStudent = studentRepository.save(student);

        // 2. Create three lessons for the student on different dates
        Lesson lesson1 = new Lesson();
        lesson1.setLessonDate(LocalDate.of(2025, 12, 21));
        lesson1.setStartTime(LocalTime.of(14, 0));
        lessonService.saveLesson(lesson1, List.of(savedStudent.getId()));

        Lesson lesson2 = new Lesson();
        lesson2.setLessonDate(LocalDate.of(2025, 12, 22));
        lesson2.setStartTime(LocalTime.of(14, 0));
        lessonService.saveLesson(lesson2, List.of(savedStudent.getId()));

        Lesson lesson3 = new Lesson();
        lesson3.setLessonDate(LocalDate.of(2025, 12, 23));
        lesson3.setStartTime(LocalTime.of(14, 0));
        lessonService.saveLesson(lesson3, List.of(savedStudent.getId()));

        // Verify initial state
        Balance initialBalance = balanceRepository.findByStudentIdAndCurrency(savedStudent.getId(), Currency.EUR).orElseThrow();
        assertThat(initialBalance.getAmount()).isEqualByComparingTo("-180.00"); // 3 lessons * -60

        List<LessonStudent> initialLessons = lessonStudentRepository.findAll();
        assertThat(initialLessons).allMatch(ls -> ls.getPaymentStatus() == PaymentStatus.UNPAID);


        // --- Action: Create a payment that covers the first two lessons but not the third ---
        mockMvc.perform(post("/payments/create")
                        .param("student.id", savedStudent.getId().toString())
                        .param("amount", "130.00") // Covers lesson 1 & 2 (120), with 10 leftover
                        .param("currency", "EUR")
                        .param("paymentDate", "2025-12-23")
                        .param("paymentMethod", "CASH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/list"));

        // --- Assertions ---
        // 1. Verify student's balance is correct
        Balance finalBalance = balanceRepository.findByStudentIdAndCurrency(savedStudent.getId(), Currency.EUR).orElseThrow();
        assertThat(finalBalance.getAmount()).isEqualByComparingTo("-50.00"); // -180 + 130 = -50

        // 2. Verify payment statuses of lessons
        List<LessonStudent> finalLessons = lessonStudentRepository.findAll().stream()
                .sorted(Comparator.comparing(ls -> ls.getLesson().getLessonDate()))
                .collect(Collectors.toList());

        assertThat(finalLessons).hasSize(3);
        assertThat(finalLessons.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PAID);     // Lesson 1 (cost 60, covered by payment)
        assertThat(finalLessons.get(1).getPaymentStatus()).isEqualTo(PaymentStatus.PAID);     // Lesson 2 (cost 60, covered by payment)
        assertThat(finalLessons.get(2).getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);   // Lesson 3 (cost 60, not enough funds)
    }

    @Test
    void test_groupLessonToIndividualLessonBalanceChange() throws Exception {
        // 1. Create two students
        Student studentA = new Student();
        studentA.setFirstName("Student");
        studentA.setLastName("A");
        studentA.setPriceIndividual(new BigDecimal("60.00"));
        studentA.setPriceGroup(new BigDecimal("40.00"));
        studentA.setCurrency(Currency.EUR);
        studentA = studentRepository.save(studentA);

        Student studentB = new Student();
        studentB.setFirstName("Student");
        studentB.setLastName("B");
        studentB.setPriceIndividual(new BigDecimal("55.00")); // Different price for variety
        studentB.setPriceGroup(new BigDecimal("35.00"));
        studentB.setCurrency(Currency.EUR);
        studentB = studentRepository.save(studentB);

        // 2. Create one group lesson for both students
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-11-15")
                        .param("startTime", "12:00")
                        .param("selectedStudentIds", studentA.getId().toString(), studentB.getId().toString()))
                .andExpect(status().is3xxRedirection());

        // 3. Verify initial state: both are charged the group price
        List<LessonStudent> createdLessonStudents = lessonStudentRepository.findAll();
        assertThat(createdLessonStudents).hasSize(2);
        Lesson savedLesson = createdLessonStudents.get(0).getLesson();

        Balance balanceA_initial = balanceRepository.findByStudentIdAndCurrency(studentA.getId(), Currency.EUR).orElseThrow();
        assertThat(balanceA_initial.getAmount()).isEqualByComparingTo("-40.00");

        Balance balanceB_initial = balanceRepository.findByStudentIdAndCurrency(studentB.getId(), Currency.EUR).orElseThrow();
        assertThat(balanceB_initial.getAmount()).isEqualByComparingTo("-35.00");

        // 4. Update the lesson to remove student B, making it an individual lesson for student A
        mockMvc.perform(post("/lessons/update/{id}", savedLesson.getId())
                        .param("lessonDate", "2025-11-15")
                        .param("startTime", "12:00")
                        .param("selectedStudentIds", studentA.getId().toString())) // Only student A remains
                .andExpect(status().is3xxRedirection());

        // 5. Final Assertions
        // Verify Student B (removed) has their balance returned to zero
        Balance balanceB_final = balanceRepository.findByStudentIdAndCurrency(studentB.getId(), Currency.EUR).orElseThrow();
        assertThat(balanceB_final.getAmount()).isEqualByComparingTo("0.00");

        // Verify Student A (remaining) is now charged the individual price
        Balance balanceA_final = balanceRepository.findByStudentIdAndCurrency(studentA.getId(), Currency.EUR).orElseThrow();
        assertThat(balanceA_final.getAmount()).isEqualByComparingTo("-60.00");

        // Verify the price on the LessonStudent record for Student A was also updated
        List<LessonStudent> finalLessonStudents = lessonStudentRepository.findAll();
        assertThat(finalLessonStudents).hasSize(1);
        LessonStudent finalLessonStudentA = finalLessonStudents.get(0);
        assertThat(finalLessonStudentA.getStudent().getId()).isEqualTo(studentA.getId());
        assertThat(finalLessonStudentA.getPrice()).isEqualByComparingTo("60.00");
    }

    @Test
    void test_deletePaymentAndLessonAndVerifyBalance() throws Exception {
        // --- Pre-requisite: A student with a paid lesson exists ---
        // 1. Create Student
        Student student = new Student();
        student.setFirstName("Charlie");
        student.setLastName("Brown");
        student.setPriceIndividual(new BigDecimal("75.00"));
        student.setPriceGroup(new BigDecimal("50.00"));
        student.setCurrency(Currency.USD);
        student = studentRepository.save(student);

        // 2. Create a lesson for the student
        Lesson lesson = new Lesson();
        lesson.setLessonDate(LocalDate.of(2025, 10, 10));
        lesson.setStartTime(LocalTime.of(15, 0));
        Lesson savedLesson = lessonService.saveLesson(lesson, List.of(student.getId()));
        LessonStudent lessonStudent = lessonStudentRepository.findAll().get(0);

        // 3. Make a payment for the lesson
        mockMvc.perform(post("/payments/create")
                        .param("student.id", student.getId().toString())
                        .param("amount", "75.00")
                        .param("currency", "USD")
                        .param("paymentDate", "2025-10-10")
                        .param("paymentMethod", "PAYPAL"))
                .andExpect(status().is3xxRedirection());

        // --- Initial Assertions: Verify lesson is PAID and balance is zero ---
        LessonStudent paidLessonStudent = lessonStudentRepository.findById(lessonStudent.getId()).orElseThrow();
        assertThat(paidLessonStudent.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        Balance balanceAfterPayment = balanceRepository.findByStudentIdAndCurrency(student.getId(), Currency.USD).orElseThrow();
        assertThat(balanceAfterPayment.getAmount()).isEqualByComparingTo("0.00");
        long paymentId = paymentRepository.findAll().get(0).getId();

        // --- Action 1: Delete the Payment ---
        mockMvc.perform(post("/payments/delete/{id}", paymentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/list"));

        // --- Assertions after Payment Deletion ---
        // Verify balance is debited again
        Balance balanceAfterPaymentDeletion = balanceRepository.findByStudentIdAndCurrency(student.getId(), Currency.USD).orElseThrow();
        assertThat(balanceAfterPaymentDeletion.getAmount()).isEqualByComparingTo("-75.00");
        // Verify lesson status is UNPAID again
        LessonStudent unpaidLessonStudent = lessonStudentRepository.findById(lessonStudent.getId()).orElseThrow();
        assertThat(unpaidLessonStudent.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(paymentRepository.findAll()).isEmpty();

        // --- Action 2: Delete the Lesson ---
        mockMvc.perform(post("/lessons/delete/{id}", savedLesson.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/list"));

        // --- Final Assertions ---
        // Verify balance is zero again
        Balance finalBalance = balanceRepository.findByStudentIdAndCurrency(student.getId(), Currency.USD).orElseThrow();
        assertThat(finalBalance.getAmount()).isEqualByComparingTo("0.00");
        // Verify the LessonStudent record is gone
        assertThat(lessonStudentRepository.findAll()).isEmpty();
    }

    @Test
    void test_fullStudentLifecycle() throws Exception {
        // 1. Create a student
        mockMvc.perform(post("/students/create")
                        .param("firstName", "Temp")
                        .param("lastName", "Student")
                        .param("priceIndividual", "100.00")
                        .param("priceGroup", "80.00")
                        .param("currency", "USD"))
                .andExpect(status().is3xxRedirection());
        
        Student student = studentRepository.findAll().get(0);
        Long studentId = student.getId();

        // 2. Add a lesson for the student
        mockMvc.perform(post("/lessons/create")
                        .param("lessonDate", "2025-11-20")
                        .param("startTime", "10:00")
                        .param("selectedStudentIds", studentId.toString()))
                .andExpect(status().is3xxRedirection());
        
        // 3. Make a payment for the student
        mockMvc.perform(post("/payments/create")
                        .param("student.id", studentId.toString())
                        .param("amount", "100.00")
                        .param("currency", "USD")
                        .param("paymentDate", "2025-11-20")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().is3xxRedirection());

        // 4. Verify the lesson is paid
        LessonStudent lessonStudent = lessonStudentRepository.findAll().get(0);
        assertThat(lessonStudent.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        Balance balance = balanceRepository.findByStudentIdAndCurrency(studentId, Currency.USD).orElseThrow();
        assertThat(balance.getAmount()).isEqualByComparingTo("0.00");

        // 5. Deactivate the student
        mockMvc.perform(post("/students/deactivate/{id}", studentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));

        Student deactivatedStudent = studentRepository.findById(studentId).orElseThrow();
        assertThat(deactivatedStudent.isActive()).isFalse();

        // 6. Hard-delete the student
        mockMvc.perform(post("/students/hard-delete/{id}", studentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/list"));

        // 7. Verify all associated data is gone
        assertThat(studentRepository.findById(studentId)).isEmpty();
        assertThat(balanceRepository.findByStudentId(studentId)).isEmpty();
        assertThat(paymentRepository.findAll()).noneMatch(p -> p.getStudent().getId().equals(studentId));
        assertThat(lessonStudentRepository.findAll()).noneMatch(ls -> ls.getStudent().getId().equals(studentId));
    }
}
