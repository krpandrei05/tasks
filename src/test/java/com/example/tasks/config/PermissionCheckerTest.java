package com.example.tasks.config;

import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionCheckerTest {
    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    private PermissionChecker permissionChecker;

    @BeforeEach
    void setUp() {
        permissionChecker = new PermissionChecker();

        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    // Cazul 1 - Authentication == null
    @Test
    void hasPermission_returnsFalse_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        boolean result = permissionChecker.hasPermission("TASK", "READ");

        assertFalse(result);
    }

    // Cazul 2 - Authentication == false
    @Test
    void hasPermission_returnsFalse_whenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = permissionChecker.hasPermission("TASK", "READ");

        assertFalse(result);
    }

    // Cazul 3 - Are permisiune
    @Test
    void hasPermission_returnsTrue_whenUserHasMatchingAuthority() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("READ_TASK"))).when(authentication).getAuthorities();

        boolean result = permissionChecker.hasPermission("task", "read");

        assertTrue(result);
    }

    // Cazul 4 - Nu are permisiune
    @Test
    void hasPermission_returnsFalse_whenUserLacksMatchingAuthority() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("CREATE_TASK"))).when(authentication).getAuthorities();

        boolean result = permissionChecker.hasPermission("task", "read");

        assertFalse(result);
    }

    // Cazul 1 - Neautentificat
    @Test
    void canAccessTask_returnsFalse_whenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        Task task = Task.builder().build();

        boolean result = permissionChecker.canAccessTask(task);

        assertFalse(result);
    }

    // Cazul 2 - ADMINE
    @Test
    void canAccessTask_returnsTrue_whenUserIsAdmin() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        Task task = Task.builder()
                .user(User.builder().email("altcineva@example.com").build())
                .build();

        boolean result = permissionChecker.canAccessTask(task);

        assertTrue(result);
    }

    // Cazul 3 - Non-ADMIN, dar owner
    @Test
    void canAccessTask_returnsTrue_whenNonAdminOwnsTask() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("andrei.krp@gmail.com");

        Task task = Task.builder()
                .user(User.builder().email("andrei.krp@gmail.com").build())
                .build();

        boolean result = permissionChecker.canAccessTask(task);

        assertTrue(result);
    }

    // Cazul 4 - Non-ADMIN, dar nu-i owner
    @Test
    void canAccessTask_returnsFalse_whenNonAdminDoesNotOwnTask() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("andrei.krp@gmail.com");

        Task task = Task.builder()
                .user(User.builder().email("altcineva@example.com").build())
                .build();

        boolean result = permissionChecker.canAccessTask(task);

        assertFalse(result);
    }

    // Cazul 5 - Non-ADMIN, task fara User asignat
    @Test
    void canAccessTask_returnsFalse_whenTaskHasNoUser() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        Task task = Task.builder().user(null).build();

        boolean result = permissionChecker.canAccessTask(task);

        assertFalse(result);
    }

    // Cazul 6 - Non-ADMIN, user asignat fara email
    @Test
    void canAccessTask_returnsFalse_whenTaskUserHasNoEmail() {
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        Task task = Task.builder()
                .user(User.builder().email(null).build())
                .build();

        boolean result = permissionChecker.canAccessTask(task);

        assertFalse(result);
    }

    // Cazul 1 - Authentication == null
    @Test
    void isCurrentUserAdmin_returnsFalse_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        boolean result = permissionChecker.isCurrentUserAdmin();

        assertFalse(result);
    }

    // Cazul 2 - Are ROLE_ADMIN
    @Test
    void isCurrentUserAdmin_returnsTrue_whenUserHasAdminRole() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        boolean result = permissionChecker.isCurrentUserAdmin();

        assertTrue(result);
    }

    // Cazul 3 - Nu are ROLE_ADMIN
    @Test
    void isCurrentUserAdmin_returnsFalse_whenUserDoesNotHaveAdminRole() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        boolean result = permissionChecker.isCurrentUserAdmin();

        assertFalse(result);
    }

    // Cazul 1 - Authentication == null
    @Test
    void getCurrentUserEmail_returnsNull_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = permissionChecker.getCurrentUserEmail();

        assertNull(result);
    }

    // Cazul 2 - Authentication exista
    @Test
    void getCurrentUserEmail_returnsAuthenticationName_whenAuthenticated() {
        when(authentication.getName()).thenReturn("andrei.krp@gmail.com");

        String result = permissionChecker.getCurrentUserEmail();

        assertEquals("andrei.krp@gmail.com", result);
    }
}