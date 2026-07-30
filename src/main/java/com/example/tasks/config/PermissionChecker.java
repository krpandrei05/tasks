package com.example.tasks.config;

import com.example.tasks.domain.Task;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("permissionChecker")
public class PermissionChecker {

    public boolean hasPermission(String resource, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String requiredAuthority = action.toUpperCase() + "_" + resource.toUpperCase();

        return authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals(requiredAuthority));
    }

    public boolean canAccessTask(Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        return task.getUser() != null && task.getUser().getEmail().equals(authentication.getName());
    }
}