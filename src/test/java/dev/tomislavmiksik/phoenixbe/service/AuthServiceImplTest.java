package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.dto.auth.AuthResponse;
import dev.tomislavmiksik.phoenixbe.dto.auth.LoginRequest;
import dev.tomislavmiksik.phoenixbe.dto.auth.RegisterRequest;
import dev.tomislavmiksik.phoenixbe.entity.User;
import dev.tomislavmiksik.phoenixbe.repository.UserRepository;
import dev.tomislavmiksik.phoenixbe.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Demonstrates testing best practices including:
 * - Mocking dependencies
 * - Testing business logic in isolation
 * - Testing both happy paths and error scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegister_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("USER", response.getRole());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken("testuser");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegister_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegister_EmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("USER", response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("Should throw exception when login with invalid credentials")
    void testLogin_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Bad credentials", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("Should encode password during registration")
    void testRegister_PasswordEncoding() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("super-secure-encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt-token");

        // When
        authService.register(registerRequest);

        // Then
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Should assign USER role by default during registration")
    void testRegister_DefaultRole() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt-token");

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertEquals("USER", response.getRole());
    }
}
