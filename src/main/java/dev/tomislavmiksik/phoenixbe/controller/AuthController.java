package dev.tomislavmiksik.phoenixbe.controller;

import dev.tomislavmiksik.phoenixbe.dto.auth.AuthResponse;
import dev.tomislavmiksik.phoenixbe.dto.auth.LoginRequest;
import dev.tomislavmiksik.phoenixbe.dto.auth.RegisterRequest;
import dev.tomislavmiksik.phoenixbe.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
