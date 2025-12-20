package com.art.tutordesk.student.service;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentHardDeleteService studentHardDeleteService;

    @Transactional
    public Student saveStudent(Student student) {
        Student savedStudent = studentRepository.save(student);
        log.info("Student saved: first name: {}, last name: {}", student.getFirstName(), student.getLastName());
        return savedStudent;
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
        log.info("Student deactivated: first name: {}, last name: {}", student.getFirstName(), student.getLastName());
    }

    @Transactional
    public void hardDeleteStudent(Long studentId) {
        studentHardDeleteService.performHardDelete(studentId);
        log.info("Student with ID {} hard deleted.", studentId);
    }

    @Transactional
    public void activateStudent(Long studentId) {
        Student student = getStudentById(studentId);
        student.setActive(true);
        studentRepository.save(student);
        log.info("Student activated: first name: {}, last name: {}", student.getFirstName(), student.getLastName());
    }

    public Student getStudentById(Long studentId) {
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