package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Role;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.dto.UserResponseDTO;
import com.example.tasks.exception.InvalidCredentialsException;
import com.example.tasks.mapper.UserMapper;
import com.example.tasks.repository.RoleRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionChecker permissionChecker;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private UserDTO existingUserDto;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .userId(1L)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .password("hashed-password")
                .build();

        existingUserDto = UserDTO.builder()
                .userId(1L)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .password("hashed-password")
                .roleName("USER")
                .build();

        adminRole = Role.builder()
                .roleId(1L)
                .roleName("ADMIN")
                .build();
    }

    @Test
    void getAllUsers_masksPasswordOnEachReturnedUser() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(existingUserDto);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertNull(result.get(0).getPassword());
    }

    // Cazul 1 - User inexistent
    @Test
    void updateUserRole_throwsNotFound_whenUserDoesNotExist() {
        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUserRole(999L, "ADMIN"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(roleRepository, never()).findByRoleName(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    // Cazul 2 - User isi schimba propriul rol
    @Test
    void updateUserRole_throwsBadRequest_whenUserTriesToChangeOwnRole() {
        when(permissionChecker.getCurrentUserEmail()).thenReturn("andrei.krp@gmail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUserRole(1L, "ADMIN"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(roleRepository, never()).findByRoleName(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    // Cazul 3 - Rol invalid
    @Test
    void updateUserRole_throwsBadRequest_whenRoleDoesNotExist() {
        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByRoleName("INVALID_ROLE")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUserRole(1L, "INVALID_ROLE"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    // Cazul 4 - Succes
    @Test
    void updateUserRole_updatesRoleAndMasksPassword_whenValid() {
        UserDTO savedUserDto = UserDTO.builder()
                .userId(1L)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .password("hashed-password")
                .roleName("ADMIN")
                .build();

        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDto(existingUser)).thenReturn(savedUserDto);

        UserDTO result = userService.updateUserRole(1L, "ADMIN");

        assertEquals(adminRole, existingUser.getRole());
        assertEquals("ADMIN", result.getRoleName());
        assertNull(result.getPassword());
    }

    @Test
    void createUser_mapsSavesAndReturnsDto() {
        User unsavedUser = User.builder()
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .password("hashed-password")
                .build();

        when(userMapper.toEntity(existingUserDto)).thenReturn(unsavedUser);
        when(userRepository.save(unsavedUser)).thenReturn(existingUser);
        when(userMapper.toDto(existingUser)).thenReturn(existingUserDto);

        UserDTO result = userService.createUser(existingUserDto);

        assertEquals(existingUserDto, result);
        verify(userRepository, times(1)).save(unsavedUser);
    }

    @Test
    void deleteUserWithTasks_deletesOnlyMatchingTasksAndTheUser() {
        Task matchingTask = Task.builder()
                .taskId(100L)
                .taskName("Write handover")
                .user(existingUser)
                .build();
        Task unrelatedTask = Task.builder()
                .taskId(200L)
                .taskName("Unrelated task")
                .user(User.builder().userId(9L).email("other@example.com").build())
                .build();
        Task nullUserTask = Task.builder()
                .taskId(300L)
                .taskName("Unassigned task")
                .user(null)
                .build();

        when(taskRepository.findAll()).thenReturn(List.of(matchingTask, unrelatedTask, nullUserTask));

        userService.deleteUserWithTasks(1L);

        verify(taskRepository, times(1)).deleteAll(List.of(matchingTask));
        verify(userRepository, times(1)).deleteById(1L);
    }

    // Cazul 1 - Email inexistent
    @Test
    void login_throwsInvalidCredentialsException_whenEmailDoesNotExist() {
        CredentialsDTO credentials = CredentialsDTO.builder()
                .email("necunoscut@example.com")
                .password("orice")
                .build();

        when(userRepository.findByEmail("necunoscut@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login(credentials));
    }

    // Cazul 2 - Parola gresita
    @Test
    void login_throwsInvalidCredentialsException_whenPasswordIsWrong() {
        CredentialsDTO credentials = CredentialsDTO.builder()
                .email("andrei.krp@gmail.com")
                .password("parola-gresita")
                .build();

        when(userRepository.findByEmail("andrei.krp@gmail.com")).thenReturn(Optional.of(existingUser));

        assertThrows(InvalidCredentialsException.class, () -> userService.login(credentials));
    }

    // Cazul 3 - Succes
    @Test
    void login_returnsUserResponseDto_whenCredentialsAreCorrect() {
        CredentialsDTO credentials = CredentialsDTO.builder()
                .email("andrei.krp@gmail.com")
                .password("hashed-password")
                .build();

        when(userRepository.findByEmail("andrei.krp@gmail.com")).thenReturn(Optional.of(existingUser));

        UserResponseDTO result = userService.login(credentials);

        assertEquals(1L, result.getUserId());
        assertEquals("andrei", result.getUsername());
        assertEquals("andrei.krp@gmail.com", result.getEmail());
    }
}