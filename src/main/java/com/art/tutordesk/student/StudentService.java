package com.art.tutordesk.student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final BalanceService balanceService;

    @Transactional
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllActiveStudents() {
        return studentRepository.findAllByActiveTrueOrderByIdAsc();
    }

    public List<Student> getAllStudentsIncludingInactive() {
        return studentRepository.findAll();
    }

    @Transactional
    public void deactivateStudent(Long studentId) {
        Student student = getStudentById(studentId);
        student.setActive(false);
        studentRepository.save(student);
    }

    @Transactional
    public void hardDeleteStudent(Long studentId) {
        balanceService.resetBalancesForStudent(studentId);
        studentRepository.deleteById(studentId);
    }

    @Transactional
    public void activateStudent(Long studentId) {
        Student student = getStudentById(studentId);
        student.setActive(true);
        studentRepository.save(student);
    }

    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
    }
}
