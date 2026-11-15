package com.example.webapp.repository;

import com.example.webapp.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    public Optional<Teacher> findByEmail(String email);
}
