package com.example.tasks.service;

import com.example.tasks.domain.User;
import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.mapper.UserMapper;
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
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final UserMapper userMapper;

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

    public ResponseEntity<String> register(UserDTO userDTO) {
        String email = new String(Base64.getDecoder().decode(userDTO.getEmail()), StandardCharsets.UTF_8);

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already is registered");
        }

        String username = new String(Base64.getDecoder().decode(userDTO.getUsername()), StandardCharsets.UTF_8);
        String password = new String(Base64.getDecoder().decode(userDTO.getPassword()), StandardCharsets.UTF_8);

        String salt = generateSalt();
        String hashedPassword = hashMd5(salt + password);

        userDTO.setUsername(username);
        userDTO.setEmail(email);
        userDTO.setPassword(hashedPassword);

        User user = userMapper.toEntity(userDTO);
        user.setSalt(salt);

        userRepository.save(user);
        log.info("User registered: {}", email);

        return ResponseEntity.status(HttpStatus.OK).body("User registered successfully");
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}