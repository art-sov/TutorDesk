package com.art.tutordesk.payment;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        PaymentDto paymentDto = new PaymentDto();
        populateFormModel(model, paymentDto);
        return "payment/add-payment";
    }

    @PostMapping("/create")
    public String createPayment(@Valid @ModelAttribute("payment") PaymentDto paymentDto, BindingResult bindingResult, Model model) {
        if (paymentDto.getStudentId() == null) {
            bindingResult.rejectValue("studentId", "NotNull", "Please select a student");
        }

        if (bindingResult.hasErrors()) {
            populateFormModel(model, paymentDto);
            return "payment/add-payment";
        }

        paymentService.createPayment(paymentDto);
        return "redirect:/payments/list";
    }

    @GetMapping("/edit/{id}")
    public String editPaymentForm(@PathVariable Long id, Model model) {
        PaymentDto paymentDto = paymentService.getPaymentById(id);
        populateFormModel(model, paymentDto);
        return "payment/edit-payment";
    }

    @PostMapping("/update/{id}")
    public String updatePayment(@PathVariable Long id, @Valid @ModelAttribute("payment") PaymentDto paymentDto, BindingResult bindingResult, Model model) {
        paymentDto.setId(id);
        if (paymentDto.getStudentId() == null) {
            bindingResult.rejectValue("studentId", "NotNull", "Please select a student");
        }

        if (bindingResult.hasErrors()) {
            populateFormModel(model, paymentDto);
            return "payment/edit-payment";
        }

        paymentService.updatePayment(paymentDto);
        return "redirect:/payments/profile/{id}";
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "redirect:/payments/list";
    }

    private void populateFormModel(Model model, PaymentDto paymentDto) {
        List<Student> students = studentService.getAllActiveStudents();
        model.addAttribute("payment", paymentDto);
        model.addAttribute("students", students);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("currencies", Currency.values());
    }
}
