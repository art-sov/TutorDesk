package com.art.tutordesk.student.service;

import com.art.tutordesk.balance.BalanceRepository;
import com.art.tutordesk.lesson.repository.LessonStudentRepository;
import com.art.tutordesk.payment.PaymentRepository;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentHardDeleteServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private LessonStudentRepository lessonStudentRepository;
    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private StudentHardDeleteService studentHardDeleteService;

    private Student student1;

    @BeforeEach
    void setUp() {
        student1 = createStudent(1L, "John", "Doe", true);
    }

    @Test
    void performHardDelete_shouldDeleteStudentAndAllDependencies_whenStudentExists() {
        Long studentId = student1.getId();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student1));

        studentHardDeleteService.performHardDelete(studentId);

        // Verify that findById was called
        verify(studentRepository, times(1)).findById(studentId);

        // Verify deletion methods are called in the correct order
        InOrder inOrder = inOrder(paymentRepository, lessonStudentRepository, balanceRepository, studentRepository);
        inOrder.verify(paymentRepository, times(1)).deleteAllByStudentId(studentId);
        inOrder.verify(lessonStudentRepository, times(1)).deleteAllByStudentId(studentId);
        inOrder.verify(balanceRepository, times(1)).deleteAllByStudentId(studentId);
        inOrder.verify(studentRepository, times(1)).deleteById(studentId);
    }

    @Test
    void performHardDelete_shouldThrowException_whenStudentNotFound() {
        Long studentId = 99L; // Non-existent ID
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentHardDeleteService.performHardDelete(studentId));

        // Verify that no deletion methods were called
        verify(studentRepository, times(1)).findById(studentId);
        verify(paymentRepository, never()).deleteAllByStudentId(anyLong());
        verify(lessonStudentRepository, never()).deleteAllByStudentId(anyLong());
        verify(balanceRepository, never()).deleteAllByStudentId(anyLong());
        verify(studentRepository, never()).deleteById(anyLong());
    }

    private Student createStudent(Long id, String firstName, String lastName, boolean active) {
        Student student = new Student();
        student.setId(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setActive(active);
        return student;
    }
}