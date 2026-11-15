package com.example.webapp.service;

import com.example.webapp.repository.AdminRepository;
import com.example.webapp.repository.StudentRepository;
import com.example.webapp.repository.TeacherRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AppUserDetailsService(AdminRepository adminRepository,
                                 TeacherRepository teacherRepository,
                                 StudentRepository studentRepository) {
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return adminRepository.findByEmail(email)
            .map(a -> user(a.getEmail(), a.getPassword(), "ADMIN"))
            .or(() -> teacherRepository.findByEmail(email)
                .map(t -> user(t.getEmail(), t.getPassword(), "TEACHER")))
            .or(() -> studentRepository.findByEmail(email)
                .map(s -> user(s.getEmail(), s.getPassword(), "STUDENT")))
            .orElseThrow(() -> new UsernameNotFoundException("No account found for email: " + email));
    }

    private UserDetails user(String email, String passwordHash, String role) {
        return User.withUsername(email).password(passwordHash).roles(role).build();
    }
}
