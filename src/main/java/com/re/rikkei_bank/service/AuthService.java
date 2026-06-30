package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken, RefreshTokenRequest request);
    com.re.rikkei_bank.dto.response.RegisterResponse register(com.re.rikkei_bank.dto.request.RegisterRequest request) throws java.io.IOException;
    void changePassword(com.re.rikkei_bank.dto.request.ChangePasswordRequest request, String username);
    String forgotPassword(com.re.rikkei_bank.dto.request.ForgotPasswordRequest request);
    void resetPassword(com.re.rikkei_bank.dto.request.ResetPasswordRequest request);
}
