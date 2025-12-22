package com.art.tutordesk.student.service;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentDto;
import com.art.tutordesk.student.StudentMapper;
import com.art.tutordesk.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentHardDeleteService studentHardDeleteService;
    private final StudentMapper studentMapper;

    @Transactional
    public StudentDto saveStudent(StudentDto studentDto) {
        Student student;
        if (studentDto.getId() == null) {
            student = studentMapper.toStudent(studentDto);
        } else {
            student = getStudentEntityById(studentDto.getId());
            studentMapper.updateStudentFromDto(studentDto, student);
        }
        Student savedStudent = studentRepository.save(student);
        log.info("Student saved: {id={}, firstName='{}', lastName='{}'}",
                savedStudent.getId(), savedStudent.getFirstName(), savedStudent.getLastName());
        return studentMapper.toStudentDto(savedStudent);
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
        studentHardDeleteService.performHardDelete(studentId);
        log.info("Student with ID {} hard deleted.", studentId);
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
        return studentMapper.toStudentDto(student);
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