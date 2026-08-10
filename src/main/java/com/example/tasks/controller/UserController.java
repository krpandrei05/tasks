package com.example.tasks.controller;

import com.example.tasks.dto.UpdateRoleDTO;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("@permissionChecker.hasPermission('USER', 'READ')")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PreAuthorize("@permissionChecker.hasPermission('USER', 'UPDATE')")
    @PutMapping("/{id}/role")
    public UserDTO updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleDTO updateRoleDTO) {
        return userService.updateUserRole(id, updateRoleDTO.getRoleName());
    }

    @DeleteMapping("/{id}/with-tasks")
    public void deleteUserWithTasks(@PathVariable Long id) {
        userService.deleteUserWithTasks(id);
    }
}