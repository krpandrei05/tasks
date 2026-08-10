package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Role;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.mapper.UserMapper;
import com.example.tasks.repository.RoleRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskRepository taskRepository;
    private final RoleRepository roleRepository;
    private final PermissionChecker permissionChecker;

    public List<UserDTO> getAllUsers() {
        log.info("Users retrieved!");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .peek(dto -> dto.setPassword(null))
                .toList();
    }

    @Transactional
    public UserDTO updateUserRole(Long userId, String roleName) {
        log.info("Updating role of user with id: {} to {}", userId, roleName);

        String currentEmail = permissionChecker.getCurrentUserEmail();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        if (user.getEmail() != null && user.getEmail().equals(currentEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName));

        user.setRole(role);
        User savedUser = userRepository.save(user);

        UserDTO userDTO = userMapper.toDto(savedUser);
        userDTO.setPassword(null);
        return userDTO;
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
}