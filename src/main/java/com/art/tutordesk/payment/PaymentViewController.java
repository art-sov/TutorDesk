package com.art.tutordesk.payment;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;
    private final StudentService studentService;

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

    @GetMapping("/new")
    public String addPaymentForm(Model model) {
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDate.now()); // Set default date
        List<Student> students = studentService.getAllActiveStudents();
        model.addAttribute("payment", payment);
        model.addAttribute("students", students);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("currencies", Currency.values());
        return "payment/add-payment";
    }

    @PostMapping("/create")
    public String createPayment(@ModelAttribute Payment payment) {
        // Ensure student object is fully loaded if coming from a form with just student ID
        if (payment.getStudent() != null && payment.getStudent().getId() != null) {
            Student student = studentService.getStudentById(payment.getStudent().getId());
            payment.setStudent(student);
        }
        paymentService.createPayment(payment);
        return "redirect:/payments/list";
    }

    @GetMapping("/edit/{id}")
    public String editPaymentForm(@PathVariable Long id, Model model) {
        Payment payment = paymentService.getPaymentById(id);
        List<Student> students = studentService.getAllActiveStudents();
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
        // Ensure student object is fully loaded if coming from a form with just student ID
        if (payment.getStudent() != null && payment.getStudent().getId() != null) {
            Student student = studentService.getStudentById(payment.getStudent().getId());
            payment.setStudent(student);
        }
        paymentService.updatePayment(payment);
        return "redirect:/payments/profile/{id}";
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "redirect:/payments/list";
    }
}
