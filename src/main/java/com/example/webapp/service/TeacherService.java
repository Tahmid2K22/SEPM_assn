package com.example.webapp.service;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.entity.Teacher;
import com.example.webapp.repository.TeacherRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository,
                          ModelMapper modelMapper,
                          PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    public Optional<TeacherDTO> getTeacherById(Long id) {
        return teacherRepository.findById(id).map(this::toDTO);
    }

    public TeacherDTO saveTeacher(TeacherDTO teacherDTO) {
        Teacher teacher = modelMapper.map(teacherDTO, Teacher.class);
        teacher.setId(null);
        teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        teacher.setSubject(teacherDTO.getSubject());
        return toDTO(teacherRepository.save(teacher));
    }

    public Optional<TeacherDTO> updateTeacher(Long id, TeacherDTO teacherDTO) {
        return teacherRepository.findById(id).map(existing -> {
            existing.setName(teacherDTO.getName());
            existing.setEmail(teacherDTO.getEmail());
            existing.setSubject(teacherDTO.getSubject());
            if (isPresent(teacherDTO.getPassword())) {
                existing.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
            }
            return toDTO(teacherRepository.save(existing));
        });
    }

    public boolean deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            return false;
        }
        teacherRepository.deleteById(id);
        return true;
    }

    private TeacherDTO toDTO(Teacher teacher) {
        TeacherDTO dto = modelMapper.map(teacher, TeacherDTO.class);
        dto.setPassword(null);
        return dto;
    }

    private boolean isPresent(String password) {
        return password != null && !password.isBlank();
    }
}
