package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementRequest;
import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementResponse;
import dev.tomislavmiksik.phoenixbe.entity.Measurement;
import dev.tomislavmiksik.phoenixbe.entity.User;
import dev.tomislavmiksik.phoenixbe.repository.MeasurementRepository;
import dev.tomislavmiksik.phoenixbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MeasurementService.
 * Demonstrates testing of:
 * - CRUD operations
 * - Security context usage
 * - Access control logic
 * - Data mapping
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeasurementService Unit Tests")
class MeasurementServiceTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MeasurementServiceImpl measurementService;

    private User testUser;
    private User otherUser;
    private Measurement testMeasurement;
    private MeasurementRequest measurementRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.USER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .role(User.Role.USER)
                .build();

        testMeasurement = Measurement.builder()
                .id(1L)
                .user(testUser)
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .chestCircumference(new BigDecimal("100.0"))
                .armCircumference(new BigDecimal("35.0"))
                .legCircumference(new BigDecimal("60.0"))
                .waistCircumference(new BigDecimal("85.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        measurementRequest = MeasurementRequest.builder()
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .chestCircumference(new BigDecimal("100.0"))
                .armCircumference(new BigDecimal("35.0"))
                .legCircumference(new BigDecimal("60.0"))
                .waistCircumference(new BigDecimal("85.0"))
                .build();

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Should successfully create a measurement")
    void testCreateMeasurement_Success() {
        // Given
        when(measurementRepository.save(any(Measurement.class))).thenReturn(testMeasurement);

        // When
        MeasurementResponse response = measurementService.createMeasurement(measurementRequest);

        // Then
        assertNotNull(response);
        assertEquals(testMeasurement.getId(), response.getId());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(new BigDecimal("75.5"), response.getWeight());
        assertEquals(new BigDecimal("180.0"), response.getHeight());

        verify(measurementRepository).save(any(Measurement.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should use current datetime when measurement date is not provided")
    void testCreateMeasurement_DefaultDate() {
        // Given
        MeasurementRequest requestWithoutDate = MeasurementRequest.builder()
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .build();

        when(measurementRepository.save(any(Measurement.class))).thenReturn(testMeasurement);

        // When
        MeasurementResponse response = measurementService.createMeasurement(requestWithoutDate);

        // Then
        assertNotNull(response);
        verify(measurementRepository).save(any(Measurement.class));
    }

    @Test
    @DisplayName("Should get all measurements for current user")
    void testGetAllMeasurements_Success() {
        // Given
        Measurement measurement2 = Measurement.builder()
                .id(2L)
                .user(testUser)
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("180.0"))
                .measurementDate(LocalDateTime.now().minusDays(1))
                .build();

        when(measurementRepository.findByUserIdOrderByMeasurementDateDesc(testUser.getId()))
                .thenReturn(Arrays.asList(testMeasurement, measurement2));

        // When
        List<MeasurementResponse> responses = measurementService.getAllMeasurements();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(testMeasurement.getId(), responses.get(0).getId());
        assertEquals(measurement2.getId(), responses.get(1).getId());

        verify(measurementRepository).findByUserIdOrderByMeasurementDateDesc(testUser.getId());
    }

    @Test
    @DisplayName("Should get recent measurements with limit")
    void testGetRecentMeasurements_Success() {
        // Given
        when(measurementRepository.findTop10ByUserIdOrderByMeasurementDateDesc(testUser.getId()))
                .thenReturn(Arrays.asList(testMeasurement));

        // When
        List<MeasurementResponse> responses = measurementService.getRecentMeasurements(5);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testMeasurement.getId(), responses.get(0).getId());

        verify(measurementRepository).findTop10ByUserIdOrderByMeasurementDateDesc(testUser.getId());
    }

    @Test
    @DisplayName("Should get measurement by ID for current user")
    void testGetMeasurementById_Success() {
        // Given
        when(measurementRepository.findById(1L)).thenReturn(Optional.of(testMeasurement));

        // When
        MeasurementResponse response = measurementService.getMeasurementById(1L);

        // Then
        assertNotNull(response);
        assertEquals(testMeasurement.getId(), response.getId());
        assertEquals(testUser.getId(), response.getUserId());

        verify(measurementRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when measurement not found")
    void testGetMeasurementById_NotFound() {
        // Given
        when(measurementRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            measurementService.getMeasurementById(999L);
        });

        assertEquals("Measurement not found", exception.getMessage());
        verify(measurementRepository).findById(999L);
    }

    @Test
    @DisplayName("Should deny access to other user's measurement")
    void testGetMeasurementById_AccessDenied() {
        // Given
        Measurement otherUserMeasurement = Measurement.builder()
                .id(2L)
                .user(otherUser)
                .weight(new BigDecimal("80.0"))
                .height(new BigDecimal("175.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        when(measurementRepository.findById(2L)).thenReturn(Optional.of(otherUserMeasurement));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            measurementService.getMeasurementById(2L);
        });

        assertEquals("Access denied", exception.getMessage());
        verify(measurementRepository).findById(2L);
    }

    @Test
    @DisplayName("Should successfully update measurement")
    void testUpdateMeasurement_Success() {
        // Given
        MeasurementRequest updateRequest = MeasurementRequest.builder()
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("181.0"))
                .chestCircumference(new BigDecimal("101.0"))
                .build();

        Measurement updatedMeasurement = Measurement.builder()
                .id(1L)
                .user(testUser)
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("181.0"))
                .chestCircumference(new BigDecimal("101.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        when(measurementRepository.findById(1L)).thenReturn(Optional.of(testMeasurement));
        when(measurementRepository.save(any(Measurement.class))).thenReturn(updatedMeasurement);

        // When
        MeasurementResponse response = measurementService.updateMeasurement(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("76.0"), response.getWeight());
        assertEquals(new BigDecimal("181.0"), response.getHeight());

        verify(measurementRepository).findById(1L);
        verify(measurementRepository).save(any(Measurement.class));
    }

    @Test
    @DisplayName("Should deny update access to other user's measurement")
    void testUpdateMeasurement_AccessDenied() {
        // Given
        Measurement otherUserMeasurement = Measurement.builder()
                .id(2L)
                .user(otherUser)
                .weight(new BigDecimal("80.0"))
                .height(new BigDecimal("175.0"))
                .build();

        when(measurementRepository.findById(2L)).thenReturn(Optional.of(otherUserMeasurement));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            measurementService.updateMeasurement(2L, measurementRequest);
        });

        assertEquals("Access denied", exception.getMessage());
        verify(measurementRepository).findById(2L);
        verify(measurementRepository, never()).save(any(Measurement.class));
    }

    @Test
    @DisplayName("Should successfully delete measurement")
    void testDeleteMeasurement_Success() {
        // Given
        when(measurementRepository.findById(1L)).thenReturn(Optional.of(testMeasurement));

        // When
        measurementService.deleteMeasurement(1L);

        // Then
        verify(measurementRepository).findById(1L);
        verify(measurementRepository).delete(testMeasurement);
    }

    @Test
    @DisplayName("Should deny delete access to other user's measurement")
    void testDeleteMeasurement_AccessDenied() {
        // Given
        Measurement otherUserMeasurement = Measurement.builder()
                .id(2L)
                .user(otherUser)
                .weight(new BigDecimal("80.0"))
                .height(new BigDecimal("175.0"))
                .build();

        when(measurementRepository.findById(2L)).thenReturn(Optional.of(otherUserMeasurement));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            measurementService.deleteMeasurement(2L);
        });

        assertEquals("Access denied", exception.getMessage());
        verify(measurementRepository).findById(2L);
        verify(measurementRepository, never()).delete(any(Measurement.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found in security context")
    void testGetCurrentUser_UserNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            measurementService.getAllMeasurements();
        });

        assertEquals("User not found", exception.getMessage());
    }
}
