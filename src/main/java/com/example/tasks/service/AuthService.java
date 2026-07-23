package com.example.tasks.service;

import com.example.tasks.domain.User;
import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public ResponseEntity<String> login(CredentialsDTO credentialsDTO) throws JoseException {
        log.info("Login attempt (auth flow) for email: {}", credentialsDTO.getEmail());

        String email = new String(Base64.getDecoder().decode(credentialsDTO.getEmail()), StandardCharsets.UTF_8);
        String password = new String(Base64.getDecoder().decode(credentialsDTO.getPassword()), StandardCharsets.UTF_8);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String hashedPassword = hashMd5(user.getSalt() + password);

        if (user.getPassword().equals(hashedPassword)) {
            return ResponseEntity.ok(jwtService.generateToken(email));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private String hashMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}