package com.art.tutordesk.student.service;

import com.art.tutordesk.balance.BalanceRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentHardDeleteService {

    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final LessonStudentRepository lessonStudentRepository;
    private final BalanceRepository balanceRepository;

    @Transactional
    public void performHardDelete(Long studentId) {
        log.info("Starting hard delete for student ID: {}", studentId);
        // Ensure student exists before trying to delete dependencies
        studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found with id: {} for hard delete.", studentId);
                    return new RuntimeException("Student not found with id: " + studentId);
                });

        // Delete dependent records in the correct order
        paymentRepository.deleteAllByStudentId(studentId);
        log.debug("Payments deleted for student ID: {}", studentId);

        lessonStudentRepository.deleteAllByStudentId(studentId);
        log.debug("LessonStudents deleted for student ID: {}", studentId);

        balanceRepository.deleteAllByStudentId(studentId);
        log.debug("Balances deleted for student ID: {}", studentId);

        // Finally, delete the student
        studentRepository.deleteById(studentId);
        log.info("Student with ID {} hard delete completed successfully.", studentId);
    }
}
