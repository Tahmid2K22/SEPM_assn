package com.example.webapp.service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          ModelMapper modelMapper,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    public Optional<StudentDTO> getStudentById(Long id) {
        return studentRepository.findById(id).map(this::toDTO);
    }

    public StudentDTO saveStudent(StudentDTO studentDTO) {
        Student student = modelMapper.map(studentDTO, Student.class);
        student.setId(null);
        student.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
        student.setCourse(studentDTO.getCourse());
        return toDTO(studentRepository.save(student));
    }

    public Optional<StudentDTO> updateStudent(Long id, StudentDTO studentDTO) {
        return studentRepository.findById(id).map(existing -> {
            existing.setName(studentDTO.getName());
            existing.setEmail(studentDTO.getEmail());
            existing.setCourse(studentDTO.getCourse());
            if (isPresent(studentDTO.getPassword())) {
                existing.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
            }
            return toDTO(studentRepository.save(existing));
        });
    }

    public boolean deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }

    private StudentDTO toDTO(Student student) {
        StudentDTO dto = modelMapper.map(student, StudentDTO.class);
        dto.setPassword(null);
        return dto;
    }

    private boolean isPresent(String password) {
        return password != null && !password.isBlank();
    }
}
