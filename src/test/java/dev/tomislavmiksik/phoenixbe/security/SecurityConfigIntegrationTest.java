package dev.tomislavmiksik.phoenixbe.security;

import dev.tomislavmiksik.phoenixbe.entity.User;
import dev.tomislavmiksik.phoenixbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Spring Security configuration.
 * Demonstrates testing of:
 * - Public vs protected endpoints
 * - JWT authentication flow
 * - Security filter chain
 * - Access control
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Spring Security Configuration Integration Tests")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.USER)
                .enabled(true)
                .build();

        validToken = jwtTokenProvider.generateToken("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Should allow access to public auth endpoints without token")
    void testPublicEndpoints_NoAuthRequired() throws Exception {
        // Registration endpoint should be public
        mockMvc.perform(post("/api/auth/register"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, not unauthorized

        // Login endpoint should be public
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, not unauthorized
    }

    @Test
    @DisplayName("Should deny access to protected endpoints without token")
    void testProtectedEndpoints_RequireAuth() throws Exception {
        // Measurements endpoint should require authentication
        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/measurements/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to protected endpoints with valid token")
    void testProtectedEndpoints_WithValidToken() throws Exception {
        // When accessing with valid JWT token
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny access with invalid token")
    void testProtectedEndpoints_WithInvalidToken() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with expired token")
    void testProtectedEndpoints_WithExpiredToken() throws Exception {
        // Given - create provider with negative expiration to generate expired token
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        org.springframework.test.util.ReflectionTestUtils.setField(
                expiredProvider,
                "jwtSecret",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        );
        org.springframework.test.util.ReflectionTestUtils.setField(
                expiredProvider,
                "jwtExpirationMs",
                -3600000L
        );
        String expiredToken = expiredProvider.generateToken("testuser");

        // When & Then
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with malformed Authorization header")
    void testProtectedEndpoints_WithMalformedHeader() throws Exception {
        // Missing "Bearer " prefix
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", validToken))
                .andExpect(status().isUnauthorized());

        // Wrong prefix
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Basic " + validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access when Authorization header is missing")
    void testProtectedEndpoints_WithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle token with non-existent user")
    void testProtectedEndpoints_WithNonExistentUser() throws Exception {
        // Given
        String tokenForNonExistentUser = jwtTokenProvider.generateToken("nonexistent");

        // When & Then
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + tokenForNonExistentUser))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials via full flow")
    void testAuthenticationFlow_Success() throws Exception {
        // This test verifies the complete authentication flow
        // 1. User sends credentials to login endpoint (public)
        // 2. Server validates and returns JWT token
        // 3. User includes token in subsequent requests
        // 4. Server validates token and grants access

        String loginJson = """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """;

        // Step 1 & 2: Login should work (public endpoint)
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk());

        // Step 3 & 4: Access protected resource with token
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should use stateless session management")
    void testStatelessSessionManagement() throws Exception {
        // Make first request with token
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        // Make second request without token - should fail (proving stateless)
        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should extract username from valid token in security context")
    void testTokenParsing_ExtractsUsername() throws Exception {
        // Given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        org.junit.jupiter.api.Assertions.assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should work with different token for different user")
    void testMultipleUsers_DifferentTokens() throws Exception {
        // Given
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .email("another@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.USER)
                .enabled(true)
                .build();

        String anotherToken = jwtTokenProvider.generateToken("anotheruser");
        when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));

        // When & Then - both tokens should work for protected endpoints
        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/measurements")
                        .header("Authorization", "Bearer " + anotherToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle OPTIONS requests for CORS preflight")
    void testCorsPreflightRequests() throws Exception {
        // OPTIONS requests should be allowed for CORS
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/api/measurements")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
