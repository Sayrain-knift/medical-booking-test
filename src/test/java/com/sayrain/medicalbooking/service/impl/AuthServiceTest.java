package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.AuthResponse;
import com.sayrain.medicalbooking.dto.LoginRequest;
import com.sayrain.medicalbooking.dto.RegisterRequest;
import com.sayrain.medicalbooking.model.User;
import com.sayrain.medicalbooking.repository.UserRepository;
import com.sayrain.medicalbooking.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPhone("123456789");
        // 使用User.UserRole枚举，而不是字符串
        registerRequest.setRole(User.UserRole.PATIENT);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("john");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john");
        mockUser.setPassword("encodedPassword");
        mockUser.setEmail("john@example.com");
        // 使用User.UserRole枚举，而不是字符串
        mockUser.setRole(User.UserRole.PATIENT);
    }

    // 用户注册成功
    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        String result = authService.registerUser(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    // 重复用户名异常
    @Test
    void testRegister_DuplicateUsername() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser(registerRequest));

        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // 登录成功
    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("john")).thenReturn("fake-jwt-token");

        AuthResponse response = authService.loginUser(loginRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("john", response.getUsername());
        assertEquals("PATIENT", response.getRole()); // 这里仍然是字符串，因为AuthResponse返回的是role.name()
        assertEquals("Login successful", response.getMessage());
    }

    // 登录凭证错误
    @Test
    void testLogin_InvalidCredentials() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.loginUser(loginRequest));

        assertEquals("Invalid username or password", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    // 用户不存在
    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.loginUser(loginRequest));

        assertEquals("Invalid username or password", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}