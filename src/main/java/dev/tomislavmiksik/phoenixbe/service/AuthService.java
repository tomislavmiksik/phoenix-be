package dev.tomislavmiksik.phoenixbe.service;

import dev.tomislavmiksik.phoenixbe.dto.AuthResponse;
import dev.tomislavmiksik.phoenixbe.dto.LoginRequest;
import dev.tomislavmiksik.phoenixbe.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
