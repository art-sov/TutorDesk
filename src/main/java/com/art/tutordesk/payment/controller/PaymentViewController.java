package com.art.tutordesk.payment.controller;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentMethod;
import com.art.tutordesk.payment.service.PaymentService;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;
    private final StudentService studentService; // Inject StudentService

    @GetMapping("/list")
    public String listPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "payment/list-payments";
    }

    @GetMapping("/profile/{id}")
    public String viewPaymentProfile(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.getPaymentById(id));
        return "payment/payment-profile";
    }

    @GetMapping("/edit/{id}")
    public String editPaymentForm(@PathVariable Long id, Model model) {
        Payment payment = paymentService.getPaymentById(id);
        List<Student> students = studentService.getAllStudents();
        model.addAttribute("payment", payment);
        model.addAttribute("students", students);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("currencies", Currency.values());
        return "payment/edit-payment";
    }

    @PostMapping("/update/{id}")
    public String updatePayment(@PathVariable Long id, @ModelAttribute Payment payment) {
        // Ensure the ID from the path is set on the payment object
        payment.setId(id);
        paymentService.savePayment(payment);
        return "redirect:/payments/profile/{id}";
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "redirect:/payments/list";
    }
}
