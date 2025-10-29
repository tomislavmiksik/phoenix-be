package dev.tomislavmiksik.phoenixbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    @DecimalMin(value = "0.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal chestCircumference;

    @DecimalMin(value = "0.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal armCircumference;

    @DecimalMin(value = "0.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal legCircumference;

    @DecimalMin(value = "0.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal waistCircumference;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime measurementDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (measurementDate == null) {
            measurementDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
