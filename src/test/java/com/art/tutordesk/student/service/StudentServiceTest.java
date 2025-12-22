package com.art.tutordesk.student.service;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.Student;
import com.art.tutordesk.student.StudentDto;
import com.art.tutordesk.student.StudentMapper;
import com.art.tutordesk.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    private Student student1;
    private Student student2;
    private StudentDto studentDto1;
    private StudentDto studentDto2;

    @BeforeEach
    void setUp() {
        student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setActive(true);

        studentDto1 = createStudentDto(1L, "John", "Doe", true);

        student2 = new Student();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setActive(false);

        studentDto2 = createStudentDto(2L, "Jane", "Smith", false);
    }

    @Test
    void saveStudent_shouldSaveNewStudent() {
        StudentDto newStudentDto = createStudentDto(null, "New", "Student", true);
        Student newStudentEntity = new Student();
        newStudentEntity.setFirstName(newStudentDto.getFirstName());
        newStudentEntity.setLastName(newStudentDto.getLastName());
        newStudentEntity.setActive(true);
        newStudentEntity.setId(10L);

        when(studentMapper.toStudent(newStudentDto)).thenReturn(newStudentEntity);
        when(studentRepository.save(any(Student.class))).thenReturn(newStudentEntity);
        when(studentMapper.toStudentDto(newStudentEntity)).thenReturn(newStudentDto);

        StudentDto result = studentService.saveStudent(newStudentDto);

        assertNotNull(result);
        assertEquals(newStudentDto.getFirstName(), result.getFirstName());
        verify(studentMapper, times(1)).toStudent(newStudentDto);
        verify(studentRepository, times(1)).save(newStudentEntity);
        verify(studentMapper, times(1)).toStudentDto(newStudentEntity);
    }

    @Test
    void saveStudent_shouldUpdateExistingStudent() {
        StudentDto existingStudentDto = createStudentDto(1L, "Updated", "Name", true);
        Student existingStudentEntity = new Student();
        existingStudentEntity.setId(1L);
        existingStudentEntity.setFirstName("John");
        existingStudentEntity.setLastName("Doe");
        existingStudentEntity.setActive(true);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudentEntity));
        doAnswer(invocation -> {
            Student target = invocation.getArgument(1);
            target.setFirstName(existingStudentDto.getFirstName());
            target.setLastName(existingStudentDto.getLastName());
            target.setActive(existingStudentDto.isActive());
            return null;
        }).when(studentMapper).updateStudentFromDto(eq(existingStudentDto), any(Student.class));
        when(studentRepository.save(existingStudentEntity)).thenReturn(existingStudentEntity);
        when(studentMapper.toStudentDto(existingStudentEntity)).thenReturn(existingStudentDto);

        StudentDto result = studentService.saveStudent(existingStudentDto);

        assertNotNull(result);
        assertEquals(existingStudentDto.getFirstName(), result.getFirstName());
        verify(studentRepository, times(1)).findById(1L);
        verify(studentMapper, times(1)).updateStudentFromDto(eq(existingStudentDto), any(Student.class));
        verify(studentRepository, times(1)).save(existingStudentEntity);
        verify(studentMapper, times(1)).toStudentDto(existingStudentEntity);
    }

    @Test
    void getAllActiveStudents_shouldReturnEmptyList() {
        when(studentRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(Collections.emptyList());

        List<StudentDto> result = studentService.getAllActiveStudents();

        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAllByActiveTrueOrderByIdAsc();
        verify(studentMapper, never()).toStudentDto(any(Student.class));
    }

    @Test
    void getAllActiveStudents_shouldReturnActiveStudents() {
        List<Student> activeStudents = Collections.singletonList(student1);
        when(studentRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(activeStudents);
        when(studentMapper.toStudentDto(student1)).thenReturn(studentDto1);

        List<StudentDto> result = studentService.getAllActiveStudents();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(studentDto1.getFirstName(), result.getFirst().getFirstName());
        verify(studentRepository, times(1)).findAllByActiveTrueOrderByIdAsc();
        verify(studentMapper, times(1)).toStudentDto(student1);
    }

    @Test
    void getAllStudentsIncludingInactive_shouldReturnEmptyList() {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        List<StudentDto> result = studentService.getAllStudentsIncludingInactive();

        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAll();
        verify(studentMapper, never()).toStudentDto(any(Student.class));
    }

    @Test
    void getAllStudentsIncludingInactive_shouldReturnAllStudents() {
        List<Student> allStudents = Arrays.asList(student1, student2);
        when(studentRepository.findAll()).thenReturn(allStudents);
        when(studentMapper.toStudentDto(student1)).thenReturn(studentDto1);
        when(studentMapper.toStudentDto(student2)).thenReturn(studentDto2);

        List<StudentDto> result = studentService.getAllStudentsIncludingInactive();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(studentRepository, times(1)).findAll();
        verify(studentMapper, times(1)).toStudentDto(student1);
        verify(studentMapper, times(1)).toStudentDto(student2);
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
        student2.setActive(false);
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
        when(studentMapper.toStudentDto(student1)).thenReturn(studentDto1);

        StudentDto result = studentService.getStudentById(student1.getId());

        assertNotNull(result);
        assertEquals(studentDto1.getId(), result.getId());
        verify(studentRepository, times(1)).findById(student1.getId());
        verify(studentMapper, times(1)).toStudentDto(student1);
    }

    @Test
    void getStudentById_shouldThrowException_whenNotFound() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.getStudentById(anyLong()));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(studentMapper, never()).toStudentDto(any(Student.class));
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

    private StudentDto createStudentDto(Long id, String firstName, String lastName, boolean active) {
        StudentDto student = new StudentDto();
        student.setId(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setActive(active);
        student.setPriceIndividual(BigDecimal.valueOf(50.00));
        student.setPriceGroup(BigDecimal.valueOf(30.00));
        student.setCurrency(Currency.USD);
        return student;
    }
}