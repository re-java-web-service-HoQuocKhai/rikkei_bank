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

    @PostMapping(value = "/register", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<com.re.rikkei_bank.dto.response.RegisterResponse>> register(
            @Valid @ModelAttribute com.re.rikkei_bank.dto.request.RegisterRequest request
    ) throws java.io.IOException {
        com.re.rikkei_bank.dto.response.RegisterResponse registerResponse = authService.register(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(
                ApiResponse.<com.re.rikkei_bank.dto.response.RegisterResponse>builder()
                        .success(true)
                        .data(registerResponse)
                        .build()
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody com.re.rikkei_bank.dto.request.ChangePasswordRequest request,
            java.security.Principal principal
    ) {
        authService.changePassword(request, principal.getName());
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Đổi mật khẩu thành công")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody com.re.rikkei_bank.dto.request.ForgotPasswordRequest request
    ) {
        String token = authService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Mã xác nhận đã được gửi")
                        .data(token) // Trả thẳng token ra để dễ test
                        .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody com.re.rikkei_bank.dto.request.ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Đặt lại mật khẩu thành công")
                        .data(null)
                        .build()
        );
    }
}
