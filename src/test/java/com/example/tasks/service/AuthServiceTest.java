package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Role;
import com.example.tasks.domain.User;
import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.mapper.UserMapper;
import com.example.tasks.repository.RoleRepository;
import com.example.tasks.repository.UserRepository;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PermissionChecker permissionChecker;

    @InjectMocks
    private AuthService authService;

    private static final String RAW_PASSWORD = "parola123";
    private static final String SALT = "fixed-test-salt";

    private User existingUser;
    private Role userRole;

    @BeforeEach
    void setUp() throws Exception {
        String expectedHash = md5(SALT + RAW_PASSWORD);

        userRole = Role.builder()
                .roleId(1L)
                .roleName("USER")
                .build();

        existingUser = User.builder()
                .userId(1L)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .password(expectedHash)
                .salt(SALT)
                .role(userRole)
                .build();
    }

    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();

        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static String base64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // Cazul 1 - Succes
    @Test
    void login_returnsTokenWithOkStatus_whenCredentialsAreCorrect() throws JoseException {
        CredentialsDTO credentials = CredentialsDTO.builder()
                .email(base64("andrei.krp@gmail.com"))
                .password(base64(RAW_PASSWORD))
                .build();

        when(userRepository.findByEmail("andrei.krp@gmail.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken("andrei.krp@gmail.com", 1L)).thenReturn("fake-jwt-token");

        ResponseEntity<String> result = authService.login(credentials);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("fake-jwt-token", result.getBody());
    }

    // Cazul 2 - Email inexistent
    @Test
    void login_returnsForbidden_whenEmailDoesNotExist() throws JoseException {
        CredentialsDTO credentials = CredentialsDTO.builder()
                .email(base64("necunoscut@example.com"))
                .password(base64(RAW_PASSWORD))
                .build();

        when(userRepository.findByEmail("necunoscut@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> result = authService.login(credentials);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        verify(jwtService, never()).generateToken(any(), any());
    }

    // Cazul 3 - Parola gresita
    @Test
    void login_returnsForbidden_whenPasswordIsWrong() throws JoseException {
        CredentialsDTO credentials = CredentialsDTO.builder()
                .email(base64("andrei.krp@gmail.com"))
                .password(base64("parola-gresita"))
                .build();

        when(userRepository.findByEmail("andrei.krp@gmail.com")).thenReturn(Optional.of(existingUser));

        ResponseEntity<String> result = authService.login(credentials);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        verify(jwtService, never()).generateToken(any(), any());
    }

    // Cazul 1 - Email deja inregistrat
    @Test
    void register_returnsConflict_whenEmailAlreadyExists() {
        UserDTO userDTO = UserDTO.builder()
                .email(base64("andrei.krp@gmail.com"))
                .username(base64("andrei"))
                .password(base64(RAW_PASSWORD))
                .build();

        when(userRepository.findByEmail("andrei.krp@gmail.com")).thenReturn(Optional.of(existingUser));

        ResponseEntity<String> result = authService.register(userDTO);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("Email is already registered", result.getBody());
        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any());
    }

    // Cazul 2 - Username deja inregistrat
    @Test
    void register_returnsConflict_whenUsernameAlreadyExists() {
        UserDTO userDTO = UserDTO.builder()
                .email(base64("new@example.com"))
                .username(base64("andrei"))
                .password(base64(RAW_PASSWORD))
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("andrei")).thenReturn(Optional.of(existingUser));

        ResponseEntity<String> result = authService.register(userDTO);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("Username is already registered", result.getBody());
        verify(userRepository, never()).save(any());
    }

    // Cazul 3 - Rolul implicit USER lipseste din DB
    @Test
    void register_throwsIllegalStateException_whenDefaultRoleMissing() {
        UserDTO userDTO = UserDTO.builder()
                .email(base64("new@example.com"))
                .username(base64("newuser"))
                .password(base64(RAW_PASSWORD))
                .build();

        User mappedUser = User.builder().build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDTO)).thenReturn(mappedUser);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> authService.register(userDTO));

        verify(userRepository, never()).save(any());
    }

    // Cazul 4 - Succes
    @Test
    void register_savesUserWithGeneratedSaltAndHashedPassword_whenValid() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .email(base64("new@example.com"))
                .username(base64("newuser"))
                .password(base64(RAW_PASSWORD))
                .build();

        User mappedUser = User.builder().build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDTO)).thenReturn(mappedUser);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));

        ResponseEntity<String> result = authService.register(userDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User registered successfully", result.getBody());
        assertEquals("newuser", userDTO.getUsername());
        assertEquals("new@example.com", userDTO.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getSalt());
        assertEquals(userRole, savedUser.getRole());
        assertEquals(md5(savedUser.getSalt() + RAW_PASSWORD), userDTO.getPassword());
    }

    // Cazul 1 - Succes
    @Test
    void getCurrentUser_returnsUserDtoWithMaskedPassword_whenAuthenticated() {
        UserDTO mappedDto = UserDTO.builder()
                .userId(1L)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .password(existingUser.getPassword())
                .build();

        when(permissionChecker.getCurrentUserEmail()).thenReturn("andrei.krp@gmail.com");
        when(userRepository.findByEmail("andrei.krp@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(mappedDto);

        ResponseEntity<UserDTO> result = authService.getCurrentUser();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("andrei", result.getBody().getUsername());
        assertNull(result.getBody().getPassword());
    }

    // Cazul 2 - User negasit (token valid, dar user-ul nu exista in DB)
    @Test
    void getCurrentUser_returnsNotFound_whenUserDoesNotExist() {
        when(permissionChecker.getCurrentUserEmail()).thenReturn("deleted@example.com");
        when(userRepository.findByEmail("deleted@example.com")).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> result = authService.getCurrentUser();

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(userMapper, never()).toDto(any());
    }
}