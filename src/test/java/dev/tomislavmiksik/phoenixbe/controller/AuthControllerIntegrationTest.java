package dev.tomislavmiksik.phoenixbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomislavmiksik.phoenixbe.dto.auth.AuthResponse;
import dev.tomislavmiksik.phoenixbe.dto.auth.LoginRequest;
import dev.tomislavmiksik.phoenixbe.dto.auth.RegisterRequest;
import dev.tomislavmiksik.phoenixbe.security.ApiKeyAuthenticationFilter;
import dev.tomislavmiksik.phoenixbe.security.JwtAuthenticationFilter;
import dev.tomislavmiksik.phoenixbe.security.JwtTokenProvider;
import dev.tomislavmiksik.phoenixbe.service.AuthService;
import dev.tomislavmiksik.phoenixbe.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Demonstrates:
 * - WebMvcTest for controller layer testing
 * - Testing REST endpoints with MockMvc
 * - JSON request/response validation
 * - HTTP status code verification
 * - Bean validation testing
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Test
    @DisplayName("Should successfully register a new user")
    @WithMockUser
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should return 400 when username already exists")
    @WithMockUser
    void testRegister_UsernameExists() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken"));
    }

    @Test
    @DisplayName("Should return 400 when registration data is invalid")
    @WithMockUser
    void testRegister_InvalidData() throws Exception {
        // Given - username too short
        RegisterRequest request = RegisterRequest.builder()
                .username("ab")  // Too short, minimum is 3
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    @WithMockUser
    void testRegister_InvalidEmail() throws Exception {
        // Given - invalid email format
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("invalid-email")  // Invalid email format
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    @WithMockUser
    void testLogin_Success() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 401 when login with invalid credentials")
    @WithMockUser
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    @DisplayName("Should return 400 when login data is missing")
    @WithMockUser
    void testLogin_MissingData() throws Exception {
        // Given - missing password
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is empty")
    @WithMockUser
    void testRegister_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
