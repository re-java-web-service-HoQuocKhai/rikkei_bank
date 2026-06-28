package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank.dto.response.AuthResponse;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.model.RefreshToken;
import com.re.rikkei_bank.model.Role;
import com.re.rikkei_bank.model.RoleName;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.RefreshTokenRepository;
import com.re.rikkei_bank.repository.TokenBlacklistRepository;
import com.re.rikkei_bank.security.CustomUserDetails;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = Role.builder().id(1L).name(RoleName.ROLE_CUSTOMER.name()).build();
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .isActive(true)
                .role(mockRole)
                .build();
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password");
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtProvider.generateToken("testuser", "ROLE_CUSTOMER")).thenReturn("access-token-123");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token-123", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("testuser", response.getUsername());
        
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("testuser", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
        assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác", exception.getMessage());
    }

    @Test
    void login_UserLocked_ThrowsException() {
        mockUser.setIsActive(false);
        LoginRequest request = new LoginRequest("testuser", "password");
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
        assertEquals("Tài khoản của bạn đã bị khóa", exception.getMessage());
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        RefreshToken refreshToken = RefreshToken.builder()
                .token("valid-refresh-token")
                .revoked(false)
                .expiredTime(LocalDateTime.now().plusDays(1))
                .user(mockUser)
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtProvider.generateToken("testuser", "ROLE_CUSTOMER")).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(request);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("valid-refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshToken_Revoked_ThrowsException() {
        RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
        RefreshToken refreshToken = RefreshToken.builder()
                .token("revoked-token")
                .revoked(true)
                .expiredTime(LocalDateTime.now().plusDays(1))
                .user(mockUser)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(refreshToken));

        assertThrows(CustomException.class, () -> authService.refreshToken(request));
    }
}
