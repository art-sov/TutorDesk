package com.art.tutordesk.report;

import com.art.tutordesk.lesson.LessonRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.StudentRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;

    public long getLessonsThisMonthCount() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return lessonRepository.countByLessonDateGreaterThanEqual(startOfMonth);
    }

    public long getActiveStudentsCount() {
        return studentRepository.countByActiveTrue();
    }

    public Map<Currency, BigDecimal> getTotalPaymentsThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        List<Payment> payments = paymentRepository.findByPaymentDateGreaterThanEqual(startOfMonth);

        return payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getCurrency,
                        Collectors.mapping(Payment::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }
}
