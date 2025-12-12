package com.art.tutordesk.student.service;

import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentHardDeleteService studentHardDeleteService;

    @InjectMocks
    private StudentService studentService;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        student1 = createStudent(1L, "John", "Doe", true);
        student2 = createStudent(2L, "Jane", "Smith", false);
    }

    @Test
    void saveStudent_shouldSaveNewStudent() {
        Student newStudent = createStudent(null, "New", "Student", true);
        when(studentRepository.save(any(Student.class))).thenReturn(student1);

        Student result = studentService.saveStudent(newStudent);

        assertNotNull(result);
        assertEquals(student1.getId(), result.getId());
        verify(studentRepository, times(1)).save(newStudent);
    }

    @Test
    void saveStudent_shouldUpdateExistingStudent() {
        Student existingStudent = createStudent(1L, "Updated", "Name", true);
        when(studentRepository.save(any(Student.class))).thenReturn(existingStudent);

        Student result = studentService.saveStudent(existingStudent);

        assertNotNull(result);
        assertEquals(existingStudent.getFirstName(), result.getFirstName());
        verify(studentRepository, times(1)).save(existingStudent);
    }

    @Test
    void getAllActiveStudents_shouldReturnEmptyList() {
        when(studentRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(Collections.emptyList());

        List<Student> result = studentService.getAllActiveStudents();

        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAllByActiveTrueOrderByIdAsc();
    }

    @Test
    void getAllActiveStudents_shouldReturnActiveStudents() {
        List<Student> activeStudents = Collections.singletonList(student1);
        when(studentRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(activeStudents);

        List<Student> result = studentService.getAllActiveStudents();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(student1.getFirstName(), result.get(0).getFirstName());
        verify(studentRepository, times(1)).findAllByActiveTrueOrderByIdAsc();
    }

    @Test
    void getAllStudentsIncludingInactive_shouldReturnEmptyList() {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        List<Student> result = studentService.getAllStudentsIncludingInactive();

        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getAllStudentsIncludingInactive_shouldReturnAllStudents() {
        List<Student> allStudents = Arrays.asList(student1, student2);
        when(studentRepository.findAll()).thenReturn(allStudents);

        List<Student> result = studentService.getAllStudentsIncludingInactive();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void deactivateStudent_shouldDeactivateExistingStudent() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student1));
        when(studentRepository.save(any(Student.class))).thenReturn(student1);

        studentService.deactivateStudent(student1.getId());

        assertFalse(student1.isActive());
        verify(studentRepository, times(1)).findById(student1.getId());
        verify(studentRepository, times(1)).save(student1);
    }

    @Test
    void deactivateStudent_shouldThrowException_whenStudentNotFound() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.deactivateStudent(anyLong()));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void hardDeleteStudent_shouldCallHardDeleteService() {
        studentService.hardDeleteStudent(student1.getId());

        verify(studentHardDeleteService, times(1)).performHardDelete(student1.getId());
    }

    @Test
    void activateStudent_shouldActivateInactiveStudent() {
        student2.setActive(false); // Ensure student is inactive for the test
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student2));
        when(studentRepository.save(any(Student.class))).thenReturn(student2);

        studentService.activateStudent(student2.getId());

        assertTrue(student2.isActive());
        verify(studentRepository, times(1)).findById(student2.getId());
        verify(studentRepository, times(1)).save(student2);
    }

    @Test
    void activateStudent_shouldThrowException_whenStudentNotFound() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.activateStudent(anyLong()));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void getStudentById_shouldReturnStudent_whenFound() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student1));

        Student result = studentService.getStudentById(student1.getId());

        assertNotNull(result);
        assertEquals(student1.getId(), result.getId());
        verify(studentRepository, times(1)).findById(student1.getId());
    }

    @Test
    void getStudentById_shouldThrowException_whenNotFound() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.getStudentById(anyLong()));
        verify(studentRepository, times(1)).findById(anyLong());
    }

    @Test
    void getStudentsByIds_shouldReturnEmptyList_whenNoIdsProvided() {
        when(studentRepository.findAllByIdIn(anyList())).thenReturn(Collections.emptyList());

        List<Student> result = studentService.getStudentsByIds(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAllByIdIn(Collections.emptyList());
    }

    @Test
    void getStudentsByIds_shouldReturnStudents_whenIdsProvided() {
        List<Long> ids = Arrays.asList(student1.getId(), student2.getId());
        List<Student> students = Arrays.asList(student1, student2);
        when(studentRepository.findAllByIdIn(ids)).thenReturn(students);

        List<Student> result = studentService.getStudentsByIds(ids);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(studentRepository, times(1)).findAllByIdIn(ids);
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