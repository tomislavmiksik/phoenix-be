package dev.tomislavmiksik.phoenixbe.controller;

import dev.tomislavmiksik.phoenixbe.dto.keygen.ApiKeyRequest;
import dev.tomislavmiksik.phoenixbe.dto.keygen.ApiKeyResponse;
import dev.tomislavmiksik.phoenixbe.entity.ApiKey;
import dev.tomislavmiksik.phoenixbe.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController()
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    final private AdminService adminService;

    @PostMapping("/keygen")
    public ResponseEntity<?> createApiKey(@Valid @RequestBody ApiKeyRequest body){
        try {
            ApiKey createdKey = adminService.createApiKey(body.getLabel());

            ApiKeyResponse response = ApiKeyResponse
                    .builder()
                    .apiKey(createdKey.getKeyHash())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
