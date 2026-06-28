package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken, RefreshTokenRequest request);
}
