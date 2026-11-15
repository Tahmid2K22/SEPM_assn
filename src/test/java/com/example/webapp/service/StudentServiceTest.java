package com.example.webapp.service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final String STORED_HASH = "$2a$10$stored-hash-value";

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, new ModelMapper(), passwordEncoder);
    }

    private Student storedStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Tahmid Chowdhury");
        student.setEmail("tahmid@stud.kuet.ac.bd");
        student.setPassword(STORED_HASH);
        student.setCourse("Software Engineering");
        return student;
    }

    @Test
    void getAllStudentsMapsEntitiesToDtosWithoutLeakingThePasswordHash() {
        when(studentRepository.findAll()).thenReturn(List.of(storedStudent()));

        List<StudentDTO> result = studentService.getAllStudents();

        assertEquals(1, result.size());
        assertEquals("Tahmid Chowdhury", result.get(0).getName());
        assertEquals("tahmid@stud.kuet.ac.bd", result.get(0).getEmail());
        assertNull(result.get(0).getPassword(), "BCrypt hash must never leave the service layer");
    }

    @Test
    void saveStudentHashesThePasswordAndForcesAnInsert() {
        StudentDTO input = new StudentDTO();
        input.setName("Dave");
        input.setEmail("dave@uni.edu");
        input.setPassword("dave123");
        input.setCourse("Databases");

        when(passwordEncoder.encode("dave123")).thenReturn("$2a$10$newly-encoded");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentDTO saved = studentService.saveStudent(input);

        assertEquals("Dave", saved.getName());
        assertNull(saved.getPassword(), "Created DTO must not carry the hash");
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertNull(captor.getValue().getId(), "id from the request must be ignored on create");
        assertEquals("$2a$10$newly-encoded", captor.getValue().getPassword(), "raw password must be BCrypt-encoded before save");
    }

    @Test
    void getStudentByIdReturnsTheDtoForAnExistingIdAndEmptyForAMissingOne() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(storedStudent()));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        StudentDTO found = studentService.getStudentById(1L).orElseThrow();

        assertEquals("Tahmid Chowdhury", found.getName());
        assertEquals("Software Engineering", found.getCourse());
        assertNull(found.getPassword(), "hash must not be exposed on read either");

        assertTrue(studentService.getStudentById(99L).isEmpty(), "missing id must map to 404 via empty Optional");
    }

    @Test
    void updateStudentEncodesTheNewPasswordWhenOneIsProvided() {
        Student existing = storedStudent();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("new-secret")).thenReturn("$2a$10$rotated-hash");

        StudentDTO patch = new StudentDTO();
        patch.setName("Carol Updated");
        patch.setEmail("student@uni.edu");
        patch.setPassword("new-secret");
        patch.setCourse("Databases");

        StudentDTO updated = studentService.updateStudent(1L, patch).orElseThrow();

        assertEquals("Carol Updated", updated.getName());
        assertEquals("Databases", updated.getCourse());
        assertNull(updated.getPassword());
        verify(passwordEncoder).encode("new-secret");
        assertEquals("$2a$10$rotated-hash", existing.getPassword(), "old hash must be replaced by the encoded new password");
    }

    @Test
    void deleteStudentReportsSuccessOnlyWhenTheRowExists() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertTrue(studentService.deleteStudent(1L), "existing row -> delete succeeds (204 in controller)");
        verify(studentRepository).deleteById(1L);

        assertFalse(studentService.deleteStudent(99L), "missing row -> delete reports failure (404 in controller)");
        verify(studentRepository, never()).deleteById(99L);
    }

    @Test
    void updateStudentKeepsTheStoredHashWhenThePasswordFieldIsBlank() {
        Student existing = storedStudent();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentDTO patch = new StudentDTO();
        patch.setName("Carol Updated");
        patch.setEmail("student@uni.edu");
        patch.setPassword("");
        patch.setCourse("Databases 2");

        StudentDTO updated = studentService.updateStudent(1L, patch).orElseThrow();

        assertEquals("Carol Updated", updated.getName());
        assertEquals("Databases 2", updated.getCourse());
        verify(passwordEncoder, never()).encode(anyString());
        assertEquals(STORED_HASH, existing.getPassword(), "blank password must keep the original hash");
    }
}
