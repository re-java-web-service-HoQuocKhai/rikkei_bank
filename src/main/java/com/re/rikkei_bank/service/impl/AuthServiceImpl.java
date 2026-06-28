package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank.dto.response.AuthResponse;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.model.RefreshToken;
import com.re.rikkei_bank.model.TokenBlackList;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.RefreshTokenRepository;
import com.re.rikkei_bank.repository.TokenBlacklistRepository;
import com.re.rikkei_bank.security.CustomUserDetails;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (user.getIsActive() == null || !user.getIsActive()) {
                throw new CustomException("Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN);
            }

            String role = user.getRole().getName();
            String accessToken = jwtProvider.generateToken(user.getUsername(), role);

            String refreshTokenStr = UUID.randomUUID().toString();
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenStr)
                    .expiredTime(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .user(user)
                    .build();
            refreshTokenRepository.save(refreshToken);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenStr)
                    .username(user.getUsername())
                    .role(role)
                    .build();

        } catch (BadCredentialsException ex) {
            throw new CustomException("Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED);
        } catch (DisabledException ex) {
            throw new CustomException("Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new CustomException("Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED));

        if (refreshToken.getRevoked()) {
            throw new CustomException("Refresh token đã bị thu hồi", HttpStatus.UNAUTHORIZED);
        }

        if (refreshToken.getExpiredTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("Refresh token đã hết hạn", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();
        String role = user.getRole().getName();
        String newAccessToken = jwtProvider.generateToken(user.getUsername(), role);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .username(user.getUsername())
                .role(role)
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken, RefreshTokenRequest request) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            if (!tokenBlacklistRepository.existsByToken(accessToken)) {
                TokenBlackList blackList = TokenBlackList.builder()
                        .token(accessToken)
                        .expiredAt(LocalDateTime.now().plusHours(1))
                        .build();
                tokenBlacklistRepository.save(blackList);
            }
        }

        if (request != null && request.getRefreshToken() != null) {
            refreshTokenRepository.findByToken(request.getRefreshToken())
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
    }
}
