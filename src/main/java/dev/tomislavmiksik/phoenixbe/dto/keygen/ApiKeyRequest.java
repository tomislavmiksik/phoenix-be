package dev.tomislavmiksik.phoenixbe.dto.keygen;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * The type Api key request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyRequest {

    @NotBlank(message = "Label is required")
    private String label;

    private Duration validFor;
}
