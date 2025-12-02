package dev.tomislavmiksik.phoenixbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementRequest;
import dev.tomislavmiksik.phoenixbe.dto.measurements.MeasurementResponse;
import dev.tomislavmiksik.phoenixbe.security.ApiKeyAuthenticationFilter;
import dev.tomislavmiksik.phoenixbe.security.JwtAuthenticationFilter;
import dev.tomislavmiksik.phoenixbe.security.JwtTokenProvider;
import dev.tomislavmiksik.phoenixbe.service.MeasurementService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MeasurementController.
 * Demonstrates:
 * - Testing secured endpoints with @WithMockUser
 * - CRUD operations testing
 * - Request parameter handling
 * - Path variable validation
 * - Bean validation testing
 */
@WebMvcTest(MeasurementController.class)
@ActiveProfiles("test")
@DisplayName("MeasurementController Integration Tests")
class MeasurementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MeasurementService measurementService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Test
    @DisplayName("Should successfully create a measurement")
    @WithMockUser(username = "testuser")
    void testCreateMeasurement_Success() throws Exception {
        // Given
        MeasurementRequest request = MeasurementRequest.builder()
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .chestCircumference(new BigDecimal("100.0"))
                .armCircumference(new BigDecimal("35.0"))
                .legCircumference(new BigDecimal("60.0"))
                .waistCircumference(new BigDecimal("85.0"))
                .build();

        MeasurementResponse response = MeasurementResponse.builder()
                .id(1L)
                .userId(1L)
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .chestCircumference(new BigDecimal("100.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        when(measurementService.createMeasurement(any(MeasurementRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/measurements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.weight").value(75.5))
                .andExpect(jsonPath("$.height").value(180.0))
                .andExpect(jsonPath("$.chestCircumference").value(100.0));
    }

    @Test
    @DisplayName("Should return 400 when weight is invalid")
    @WithMockUser(username = "testuser")
    void testCreateMeasurement_InvalidWeight() throws Exception {
        // Given - weight is zero (must be > 0)
        MeasurementRequest request = MeasurementRequest.builder()
                .weight(new BigDecimal("0"))
                .height(new BigDecimal("180.0"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/measurements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all measurements for current user")
    @WithMockUser(username = "testuser")
    void testGetAllMeasurements_Success() throws Exception {
        // Given
        MeasurementResponse measurement1 = MeasurementResponse.builder()
                .id(1L)
                .userId(1L)
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        MeasurementResponse measurement2 = MeasurementResponse.builder()
                .id(2L)
                .userId(1L)
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("180.0"))
                .measurementDate(LocalDateTime.now().minusDays(1))
                .build();

        List<MeasurementResponse> measurements = Arrays.asList(measurement1, measurement2);
        when(measurementService.getAllMeasurements()).thenReturn(measurements);

        // When & Then
        mockMvc.perform(get("/api/measurements")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("Should get recent measurements with default limit")
    @WithMockUser(username = "testuser")
    void testGetRecentMeasurements_DefaultLimit() throws Exception {
        // Given
        MeasurementResponse measurement = MeasurementResponse.builder()
                .id(1L)
                .userId(1L)
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        when(measurementService.getRecentMeasurements(10)).thenReturn(Arrays.asList(measurement));

        // When & Then
        mockMvc.perform(get("/api/measurements/recent")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Should get recent measurements with custom limit")
    @WithMockUser(username = "testuser")
    void testGetRecentMeasurements_CustomLimit() throws Exception {
        // Given
        when(measurementService.getRecentMeasurements(5)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/measurements/recent")
                        .param("limit", "5")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get measurement by ID")
    @WithMockUser(username = "testuser")
    void testGetMeasurementById_Success() throws Exception {
        // Given
        MeasurementResponse response = MeasurementResponse.builder()
                .id(1L)
                .userId(1L)
                .weight(new BigDecimal("75.5"))
                .height(new BigDecimal("180.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        when(measurementService.getMeasurementById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/measurements/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.weight").value(75.5));
    }

    @Test
    @DisplayName("Should return 404 when measurement not found")
    @WithMockUser(username = "testuser")
    void testGetMeasurementById_NotFound() throws Exception {
        // Given
        when(measurementService.getMeasurementById(999L))
                .thenThrow(new RuntimeException("Measurement not found"));

        // When & Then
        mockMvc.perform(get("/api/measurements/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Measurement not found"));
    }

    @Test
    @DisplayName("Should successfully update measurement")
    @WithMockUser(username = "testuser")
    void testUpdateMeasurement_Success() throws Exception {
        // Given
        MeasurementRequest request = MeasurementRequest.builder()
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("181.0"))
                .build();

        MeasurementResponse response = MeasurementResponse.builder()
                .id(1L)
                .userId(1L)
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("181.0"))
                .measurementDate(LocalDateTime.now())
                .build();

        when(measurementService.updateMeasurement(eq(1L), any(MeasurementRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/measurements/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.weight").value(76.0))
                .andExpect(jsonPath("$.height").value(181.0));
    }

    @Test
    @DisplayName("Should return 400 when update with invalid data")
    @WithMockUser(username = "testuser")
    void testUpdateMeasurement_AccessDenied() throws Exception {
        // Given
        MeasurementRequest request = MeasurementRequest.builder()
                .weight(new BigDecimal("76.0"))
                .height(new BigDecimal("181.0"))
                .build();

        when(measurementService.updateMeasurement(eq(1L), any(MeasurementRequest.class)))
                .thenThrow(new RuntimeException("Access denied"));

        // When & Then
        mockMvc.perform(put("/api/measurements/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Access denied"));
    }

    @Test
    @DisplayName("Should successfully delete measurement")
    @WithMockUser(username = "testuser")
    void testDeleteMeasurement_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/measurements/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when delete non-existent measurement")
    @WithMockUser(username = "testuser")
    void testDeleteMeasurement_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Measurement not found"))
                .when(measurementService).deleteMeasurement(999L);

        // When & Then
        mockMvc.perform(delete("/api/measurements/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Measurement not found"));
    }

    @Test
    @DisplayName("Should require authentication for all endpoints")
    void testEndpoints_RequireAuthentication() throws Exception {
        // Test that endpoints without authentication fail
        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should validate required fields")
    @WithMockUser(username = "testuser")
    void testCreateMeasurement_MissingRequiredFields() throws Exception {
        // Given - missing required weight and height
        MeasurementRequest request = MeasurementRequest.builder()
                .chestCircumference(new BigDecimal("100.0"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/measurements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
