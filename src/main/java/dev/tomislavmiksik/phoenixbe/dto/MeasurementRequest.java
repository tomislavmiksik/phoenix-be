package dev.tomislavmiksik.phoenixbe.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementRequest {

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    private BigDecimal weight;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    private BigDecimal height;

    @DecimalMin(value = "0.0", message = "Chest circumference must be non-negative")
    private BigDecimal chestCircumference;

    @DecimalMin(value = "0.0", message = "Arm circumference must be non-negative")
    private BigDecimal armCircumference;

    @DecimalMin(value = "0.0", message = "Leg circumference must be non-negative")
    private BigDecimal legCircumference;

    @DecimalMin(value = "0.0", message = "Waist circumference must be non-negative")
    private BigDecimal waistCircumference;

    private LocalDateTime measurementDate;
}
