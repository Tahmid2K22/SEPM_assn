package com.example.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, String> me(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return Map.of(
            "email", auth.getName(),
            "role", role.replaceFirst("^ROLE_", "")
        );
    }
}
