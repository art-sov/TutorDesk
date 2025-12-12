package com.art.tutordesk.student.service;

import com.art.tutordesk.balance.BalanceRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentHardDeleteService {

    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final LessonStudentRepository lessonStudentRepository;
    private final BalanceRepository balanceRepository;

    @Transactional
    public void performHardDelete(Long studentId) {
        // Ensure student exists before trying to delete dependencies
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        // Delete dependent records in the correct order
        paymentRepository.deleteAllByStudentId(studentId);
        lessonStudentRepository.deleteAllByStudentId(studentId);
        balanceRepository.deleteAllByStudentId(studentId);

        // Finally, delete the student
        studentRepository.deleteById(studentId);
    }
}
