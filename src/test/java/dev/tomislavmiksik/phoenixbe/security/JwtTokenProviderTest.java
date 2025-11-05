package dev.tomislavmiksik.phoenixbe.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtTokenProvider.
 * Demonstrates testing of:
 * - JWT token generation
 * - Token validation
 * - Token parsing and claims extraction
 * - Security edge cases (expired tokens, invalid tokens)
 */
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", testExpiration);
    }

    @Test
    @DisplayName("Should generate valid JWT token from username")
    void testGenerateToken_FromUsername() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should generate valid JWT token from Authentication")
    void testGenerateToken_FromAuthentication() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsernameFromToken_Success() {
        // Given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should validate a valid token")
    void testValidateToken_ValidToken() {
        // Given
        String token = jwtTokenProvider.generateToken("testuser");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject an invalid token")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject an expired token")
    void testValidateToken_ExpiredToken() {
        // Given - create token with -1 hour expiration (already expired)
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpirationMs", -3600000L);

        String expiredToken = expiredTokenProvider.generateToken("testuser");

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void testValidateToken_WrongSignature() {
        // Given - create token with different secret
        JwtTokenProvider differentProvider = new JwtTokenProvider();
        String differentSecret = "differentSecretKey12345678901234567890123456789012";
        ReflectionTestUtils.setField(differentProvider, "jwtSecret", differentSecret);
        ReflectionTestUtils.setField(differentProvider, "jwtExpirationMs", testExpiration);

        String tokenWithDifferentSignature = differentProvider.generateToken("testuser");

        // When
        boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSignature);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty or null token")
    void testValidateToken_EmptyToken() {
        // When & Then
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Should include issued at date in token")
    void testGenerateToken_IncludesIssuedAt() {
        // Given
        String username = "testuser";
        long beforeGeneration = System.currentTimeMillis();

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
        Date issuedAt = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuedAt();

        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() >= beforeGeneration);
        assertTrue(issuedAt.getTime() <= System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should include expiration date in token")
    void testGenerateToken_IncludesExpiration() {
        // Given
        String username = "testuser";
        long expectedExpiration = System.currentTimeMillis() + testExpiration;

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        assertNotNull(expiration);
        // Allow 1 second tolerance for test execution time
        assertTrue(Math.abs(expiration.getTime() - expectedExpiration) < 1000);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsersGetDifferentTokens() {
        // When
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");

        // Then
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals("user2", jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void testGenerateToken_SpecialCharactersInUsername() {
        // Given
        String username = "test.user+123@example.com";

        // When
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
        assertTrue(jwtTokenProvider.validateToken(token));
    }
}
