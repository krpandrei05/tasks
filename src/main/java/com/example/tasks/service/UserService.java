package com.example.tasks.service;

import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.dto.UserResponseDTO;
import com.example.tasks.exception.InvalidCredentialsException;
import com.example.tasks.mapper.UserMapper;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskRepository taskRepository;

    public List<UserDTO> getAllUsers() {
        log.info("Users retrieved!");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public UserDTO createUser(@Valid UserDTO userDTO) {
        log.info("User created!");
        User user = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void deleteUserWithTasks(Long userId) {
        log.info("Deleting user with id: {} and all their tasks", userId);
        List<Task> userTasks = taskRepository.findAll().stream()
                .filter(task -> task.getUser() != null && task.getUser().getUserId().equals(userId))
                .toList();
        taskRepository.deleteAll(userTasks);
        userRepository.deleteById(userId);
    }

    public UserResponseDTO login(CredentialsDTO credentialsDTO) {
        log.info("Login attempt for email: {}", credentialsDTO.getEmail());
        User user = userRepository.findByEmail(credentialsDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.getPassword().equals(credentialsDTO.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}