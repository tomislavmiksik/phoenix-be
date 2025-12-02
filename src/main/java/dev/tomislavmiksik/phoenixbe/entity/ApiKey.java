package dev.tomislavmiksik.phoenixbe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_hash", nullable = false, length = 64, unique = true)
    private String keyHash;

    private String label;
    private Instant createdAt = Instant.now();
    private Instant expiresAt;
    private boolean active = true;
    private Instant lastUsedAt;

    public ApiKey() {

    }
}