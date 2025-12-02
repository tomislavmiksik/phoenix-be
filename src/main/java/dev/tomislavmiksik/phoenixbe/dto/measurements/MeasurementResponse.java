package dev.tomislavmiksik.phoenixbe.dto.measurements;

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
public class MeasurementResponse {

    private Long id;
    private Long userId;
    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal chestCircumference;
    private BigDecimal armCircumference;
    private BigDecimal legCircumference;
    private BigDecimal waistCircumference;
    private LocalDateTime measurementDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
