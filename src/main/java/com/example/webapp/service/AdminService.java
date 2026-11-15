package com.example.webapp.service;

import com.example.webapp.dto.AdminDTO;
import com.example.webapp.entity.Admin;
import com.example.webapp.repository.AdminRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository,
                        ModelMapper modelMapper,
                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    public Optional<AdminDTO> getAdminById(Long id) {
        return adminRepository.findById(id).map(this::toDTO);
    }

    public AdminDTO saveAdmin(AdminDTO adminDTO) {
        Admin admin = modelMapper.map(adminDTO, Admin.class);
        admin.setId(null);
        admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        return toDTO(adminRepository.save(admin));
    }

    public Optional<AdminDTO> updateAdmin(Long id, AdminDTO adminDTO) {
        return adminRepository.findById(id).map(existing -> {
            existing.setName(adminDTO.getName());
            existing.setEmail(adminDTO.getEmail());
            if (isPresent(adminDTO.getPassword())) {
                existing.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
            }
            return toDTO(adminRepository.save(existing));
        });
    }

    public boolean deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            return false;
        }
        adminRepository.deleteById(id);
        return true;
    }

    private AdminDTO toDTO(Admin admin) {
        AdminDTO dto = modelMapper.map(admin, AdminDTO.class);
        dto.setPassword(null);
        return dto;
    }

    private boolean isPresent(String password) {
        return password != null && !password.isBlank();
    }
}
