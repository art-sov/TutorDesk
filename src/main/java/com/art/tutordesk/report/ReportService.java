package com.art.tutordesk.report;

import com.art.tutordesk.lesson.repository.LessonRepository;
import com.art.tutordesk.lesson.LessonStudent;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.payment.Payment;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final LessonStudentRepository lessonStudentRepository;

    public long getLessonsThisMonthCount() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long count = lessonRepository.countByLessonDateGreaterThanEqual(startOfMonth);
        log.debug("Lessons this month count (from {}): {}", startOfMonth, count);
        return count;
    }

    public long getActiveStudentsCount() {
        long count = studentRepository.countByActiveTrue();
        log.debug("Active students count: {}", count);
        return count;
    }

    public Map<Currency, BigDecimal> getTotalPaymentsThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        List<Payment> payments = paymentRepository.findByPaymentDateGreaterThanEqual(startOfMonth);

        Map<Currency, BigDecimal> totalPayments = payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getCurrency,
                        Collectors.mapping(Payment::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        log.debug("Total payments this month (from {}): {}", startOfMonth, totalPayments);
        return totalPayments;
    }

    public List<ReportItemDto> generateReport(LocalDate startDate, LocalDate endDate, List<Long> studentIds, boolean includeLessons, boolean includePayments) {
        log.info("Generating report from {} to {} for students {}. Include lessons: {}, include payments: {}",
                startDate, endDate, studentIds, includeLessons, includePayments);
        List<ReportItemDto> reportItems = new ArrayList<>();

        if (includeLessons) {
            List<LessonStudent> lessons = lessonStudentRepository.findByLessonDateBetweenAndStudentIds(startDate, endDate, studentIds);
            log.debug("Found {} lessons for report criteria.", lessons.size());
            lessons.stream()
                    .map(ls -> ReportItemDto.builder()
                            .studentName(ls.getStudent().getFirstName() + " " + ls.getStudent().getLastName())
                            .itemType(ReportItemDto.ItemType.LESSON)
                            .currency(ls.getCurrency())
                            .amount(ls.getPrice())
                            .date(ls.getLesson().getLessonDate())
                            .build())
                    .forEach(reportItems::add);
        }

        if (includePayments) {
            List<Payment> payments = paymentRepository.findByFilters(startDate, endDate, studentIds);
            log.debug("Found {} payments for report criteria.", payments.size());
            payments.stream()
                    .map(p -> ReportItemDto.builder()
                            .studentName(p.getStudent().getFirstName() + " " + p.getStudent().getLastName())
                            .itemType(ReportItemDto.ItemType.PAYMENT)
                            .currency(p.getCurrency())
                            .amount(p.getAmount())
                            .date(p.getPaymentDate())
                            .build())
                    .forEach(reportItems::add);
        }

        reportItems.sort(Comparator.comparing(ReportItemDto::getDate));
        log.info("Report generated with {} items.", reportItems.size());
        return reportItems;
    }
}
