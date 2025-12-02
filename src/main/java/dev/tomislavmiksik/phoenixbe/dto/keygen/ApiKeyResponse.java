package dev.tomislavmiksik.phoenixbe.dto.keygen;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiKeyResponse {

    private String apiKey;
}
