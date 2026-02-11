package com.art.tutordesk.student.service;

import com.art.tutordesk.balance.BalanceQueryService;
import com.art.tutordesk.balance.BalanceTransactionService;
import com.art.tutordesk.balance.TransactionSource;
import com.art.tutordesk.balance.TransactionType;
import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentDto;
import com.art.tutordesk.student.StudentMapper;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentHardDeleteService studentHardDeleteService;
    private final StudentMapper studentMapper;
    private final BalanceTransactionService balanceTransactionService;
    private final BalanceQueryService balanceQueryService;

    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        Student student = studentMapper.toStudent(studentDto);
        Student savedStudent = studentRepository.save(student);
        log.info("Student created: {id={}, firstName='{}', lastName='{}'}",
                savedStudent.getId(), savedStudent.getFirstName(), savedStudent.getLastName());

        balanceTransactionService.createBalanceTransaction(
                savedStudent,
                TransactionType.STUDENT_CREATED,
                savedStudent.getCurrency(),
                TransactionSource.STUDENT,
                BigDecimal.ZERO,
                savedStudent.getId()
        );

        return studentMapper.toStudentDto(savedStudent);
    }

    @Transactional
    public StudentDto updateStudent(StudentDto studentDto) {
        if (studentDto.getId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null for update operation.");
        }
        Student existingStudent = getStudentEntityById(studentDto.getId());

        Currency oldCurrency = existingStudent.getCurrency();

        studentMapper.updateStudentFromDto(studentDto, existingStudent);
        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("Student updated: {id={}, firstName='{}', lastName='{}'}",
                updatedStudent.getId(), updatedStudent.getFirstName(), updatedStudent.getLastName());

        if (oldCurrency != updatedStudent.getCurrency()) {
            balanceTransactionService.createBalanceTransaction(
                    updatedStudent,
                    TransactionType.STUDENT_UPDATED,
                    updatedStudent.getCurrency(),
                    TransactionSource.STUDENT,
                    BigDecimal.ZERO,
                    updatedStudent.getId()
            );
            log.info("Initialized zero balance for student {} in new currency: {}", updatedStudent.getId(), updatedStudent.getCurrency());
        }

        return studentMapper.toStudentDto(updatedStudent);
    }

    public List<StudentDto> getAllActiveStudents() {
        return studentRepository.findAllByActiveTrueOrderByIdAsc().stream()
                .map(studentMapper::toStudentDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> getAllStudentsIncludingInactive() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toStudentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateStudent(Long studentId) {
        Student student = getStudentEntityById(studentId);
        student.setActive(false);
        studentRepository.save(student);
        log.info("Student deactivated: {id={}, firstName='{}', lastName='{}'}",
                student.getId(), student.getFirstName(), student.getLastName());
    }

    @Transactional
    public void hardDeleteStudent(Long studentId) {
        // Delete all associated balance transactions before hard deleting the student
        balanceTransactionService.deleteTransactionsByStudentId(studentId);
        studentHardDeleteService.performHardDelete(studentId);
        log.info("Student with ID {} hard deleted, including associated balance transactions.", studentId);
    }

    @Transactional
    public void activateStudent(Long studentId) {
        Student student = getStudentEntityById(studentId);
        student.setActive(true);
        studentRepository.save(student);
        log.info("Student activated: {id={}, firstName='{}', lastName='{}'}",
                student.getId(), student.getFirstName(), student.getLastName());
    }

    public StudentDto getStudentById(Long studentId) {
        Student student = getStudentEntityById(studentId);
        StudentDto studentDto = studentMapper.toStudentDto(student);
        studentDto.setBalances(balanceQueryService.getAllBalancesForStudent(studentId));
        return studentDto;
    }

    // Helper method to get Student entity, internal to service
    public Student getStudentEntityById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found with id: {}", studentId);
                    return new RuntimeException("Student not found with id: " + studentId);
                });
    }

    public List<Student> getStudentsByIds(List<Long> studentIds) {
        return studentRepository.findAllByIdIn(studentIds);
    }
}