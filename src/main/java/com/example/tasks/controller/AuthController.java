package com.example.tasks.controller;

import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody CredentialsDTO credentialsDTO) throws Exception {
        return authService.login(credentialsDTO);
    }
}