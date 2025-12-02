package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.entity.ApiKey;
import dev.tomislavmiksik.phoenixbe.repository.ApiKeyRepository;
import dev.tomislavmiksik.phoenixbe.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    @Value("${apikey.expiration-offset-ms}")
    private long expirationDateOffset;

    private final ApiKeyRepository apiKeyRepository;


    @Override
    public ApiKey createApiKey(String label) {
        Duration expirationOffset = Duration.of(expirationDateOffset, ChronoUnit.MILLIS);
        String rawKey = ApiKeyGenerator.generateKey();

        ApiKey apiKey = ApiKey.builder()
                .label(label)
                .keyHash(DigestUtils.sha256Hex(rawKey))
                .expiresAt(Instant.now().plus(expirationOffset))
                .createdAt(Instant.now())
                .active(true)
                .build();

        return apiKeyRepository.save(apiKey);
    }
}
