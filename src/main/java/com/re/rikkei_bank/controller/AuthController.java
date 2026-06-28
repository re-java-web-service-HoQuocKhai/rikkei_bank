package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank.dto.response.ApiResponse;
import com.re.rikkei_bank.dto.response.AuthResponse;
import com.re.rikkei_bank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .data(authResponse)
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .data(authResponse)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request,
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest
    ) {
        String authHeader = request.getHeader("Authorization");
        authService.logout(authHeader, refreshTokenRequest);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .data("Logout thành công")
                        .build()
        );
    }
}
