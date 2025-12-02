package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.dto.auth.AuthResponse;
import dev.tomislavmiksik.phoenixbe.dto.auth.LoginRequest;
import dev.tomislavmiksik.phoenixbe.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
