package com.art.tutordesk.payment;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = PaymentViewController.class)
class PaymentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private StudentService studentService;

    @Test
    void listPayments() throws Exception {
        Student student = createStudent();
        Payment payment = createPayment(student);

        when(paymentService.getAllPayments()).thenReturn(Collections.singletonList(payment));

        mockMvc.perform(get("/payments/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/list-payments"))
                .andExpect(model().attributeExists("payments"));
    }

    @Test
    void viewPaymentProfile() throws Exception {
        Student student = createStudent();
        Payment payment = createPayment(student);

        when(paymentService.getPaymentById(1L)).thenReturn(payment);

        mockMvc.perform(get("/payments/profile/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/payment-profile"))
                .andExpect(model().attributeExists("payment"));
    }

    @Test
    void addPaymentForm() throws Exception {
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/payments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/add-payment"))
                .andExpect(model().attributeExists("payment"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attributeExists("paymentMethods"))
                .andExpect(model().attributeExists("currencies"));
    }

    @Test
    void createPayment_whenValid() throws Exception {
        Student student = createStudent();

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(new Payment());

        mockMvc.perform(post("/payments/create")
                        .param("amount", "100")
                        .param("currency", "USD")
                        .param("paymentDate", "2025-12-17")
                        .param("paymentMethod", "CASH")
                        .param("student.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/list"));
    }

    @Test
    void createPayment_whenInvalid_noStudent() throws Exception {
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/payments/create")
                        .param("amount", "100")
                        .param("currency", "USD")
                        .param("paymentDate", "2025-12-17")
                        .param("paymentMethod", "CASH"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/add-payment"))
                .andExpect(model().attributeHasFieldErrors("payment", "student"));
    }

    @Test
    void createPayment_whenInvalid_bindingResultErrors() throws Exception {
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/payments/create")
                        .param("amount", "-100") // Invalid amount
                        .param("student.id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/add-payment"))
                .andExpect(model().attributeHasFieldErrors("payment", "amount"));
    }


    @Test
    void editPaymentForm() throws Exception {
        Payment payment = new Payment();
        payment.setId(1L);
        Student student = new Student();
        student.setId(1L);
        payment.setStudent(student);

        when(paymentService.getPaymentById(1L)).thenReturn(payment);
        when(studentService.getAllActiveStudents()).thenReturn(Collections.singletonList(student));

        mockMvc.perform(get("/payments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/edit-payment"))
                .andExpect(model().attributeExists("payment"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attributeExists("paymentMethods"))
                .andExpect(model().attributeExists("currencies"));
    }

    @Test
    void updatePayment_whenValid() throws Exception {
        Student student = createStudent();

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(paymentService.updatePayment(any(Payment.class))).thenReturn(new Payment());

        mockMvc.perform(post("/payments/update/1")
                        .param("amount", "150")
                        .param("currency", "EUR")
                        .param("paymentDate", "2025-12-18")
                        .param("paymentMethod", "CARD")
                        .param("student.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/profile/1"));
    }

    @Test
    void updatePayment_whenInvalid_noStudent() throws Exception {
        Payment payment = new Payment();
        payment.setId(1L);

        when(paymentService.getPaymentById(1L)).thenReturn(payment);
        when(studentService.getAllActiveStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/payments/update/1")
                        .param("amount", "150")
                        .param("currency", "EUR")
                        .param("paymentDate", "2025-12-18")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/edit-payment"))
                .andExpect(model().attributeHasFieldErrors("payment", "student"));
    }

    @Test
    void updatePayment_whenInvalid_bindingResultErrors() throws Exception {
        Payment payment = new Payment();
        payment.setId(1L);
        Student student = new Student();
        student.setId(1L);
        payment.setStudent(student);

        when(paymentService.getPaymentById(anyLong())).thenReturn(payment);
        when(studentService.getAllActiveStudents()).thenReturn(List.of(student));


        mockMvc.perform(post("/payments/update/1")
                        .param("amount", "0") // Invalid
                        .param("student.id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/edit-payment"))
                .andExpect(model().attributeHasFieldErrors("payment", "amount"));
    }

    @Test
    void deletePayment() throws Exception {
        doNothing().when(paymentService).deletePayment(1L);

        mockMvc.perform(post("/payments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/list"));
    }

    private Student createStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        return student;
    }

    private Payment createPayment(Student student) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(BigDecimal.TEN);
        payment.setCurrency(Currency.USD);
        payment.setPaymentDate(LocalDate.now());
        payment.setStudent(student);
        payment.setPaymentMethod(PaymentMethod.CASH);
        return payment;
    }
}