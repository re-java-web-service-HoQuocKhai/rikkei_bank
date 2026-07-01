package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.request.RefreshTokenRequest;
import com.re.rikkei_bank.dto.request.RegisterRequest;
import com.re.rikkei_bank.dto.response.AuthResponse;
import com.re.rikkei_bank.dto.response.RegisterResponse;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.exception.DuplicateResourceException;
import com.re.rikkei_bank.exception.InvalidFileException;
import com.re.rikkei_bank.mapper.RegisterMapper;
import com.re.rikkei_bank.model.*;
import com.re.rikkei_bank.repository.*;
import com.re.rikkei_bank.security.CustomUserDetails;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.AuthService;
import com.re.rikkei_bank.service.RedisTokenBlacklistService;
import com.re.rikkei_bank.service.UploadService;
import com.re.rikkei_bank.util.GenerateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTokenBlacklistService redisTokenBlacklistService;
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final KycProfileRepository kycProfileRepository;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;
    private final RegisterMapper registerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request) throws IOException {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã được sử dụng");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Số điện thoại đã được sử dụng");
        }
        if (kycProfileRepository.existsByIdNumber(request.getIdNumber())) {
            throw new DuplicateResourceException("Số CCCD đã được đăng ký");
        }

        validateImage(request.getCccdFront());
        validateImage(request.getCccdBack());
        validateImage(request.getSelfie());

        String cccdFrontUrl = uploadService.uploadFile(request.getCccdFront());
        String cccdBackUrl = uploadService.uploadFile(request.getCccdBack());
        String selfieUrl = uploadService.uploadFile(request.getSelfie());

        Role roleCustomer = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new CustomException("Role không tồn tại", HttpStatus.INTERNAL_SERVER_ERROR));

        String username;
        do {
            username = "CUS" + GenerateUtils.generateRandomNumber(8);
        } while (userRepository.existsByUsername(username));

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isKyc(false)
                .role(roleCustomer)
                .build();
        user = userRepository.save(user);

        String accountNumber;
        do {
            accountNumber = GenerateUtils.generateRandomNumber(10);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .transactionPin(passwordEncoder.encode("123456"))
                .status(AccountStatus.ACTIVE)
                .active(true)
                .user(user)
                .build();
        account = accountRepository.save(account);

        KycProfile kycProfile = KycProfile.builder()
                .idNumber(request.getIdNumber())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .gender(request.getGender())
                .address(request.getAddress())
                .cccdFrontUrl(cccdFrontUrl)
                .cccdBackUrl(cccdBackUrl)
                .selfieUrl(selfieUrl)
                .status(KycStatus.PENDING)
                .user(user)
                .build();
        kycProfile = kycProfileRepository.save(kycProfile);

        return RegisterResponse.builder()
                .message("Đăng ký thành công. Vui lòng chờ phê duyệt KYC.")
                .user(registerMapper.toUserResponse(user))
                .account(registerMapper.toAccountResponse(account))
                .kycProfile(registerMapper.toKycResponse(kycProfile))
                .build();
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File không được rỗng");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new InvalidFileException("File không được vượt quá 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new InvalidFileException("Chỉ chấp nhận định dạng ảnh JPEG hoặc PNG");
        }
    }

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
            try {
                if (!redisTokenBlacklistService.isTokenBlacklisted(accessToken)) {
                    long remainingTime = jwtProvider.getRemainingTime(accessToken);
                    redisTokenBlacklistService.saveTokenToBlacklist(accessToken, remainingTime);
                }
            } catch (Exception e) {
                // Redis không khả dụng, log cảnh báo nhưng vẫn tiếp tục logout (revoke refresh token)
                org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class)
                        .warn("Lỗi kết nối Redis khi blacklist token trong logout: {}", e.getMessage());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(com.re.rikkei_bank.dto.request.ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("Tài khoản không tồn tại", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException("Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Xác nhận mật khẩu mới không khớp", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String forgotPassword(com.re.rikkei_bank.dto.request.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Không tìm thấy tài khoản với email này", HttpStatus.NOT_FOUND));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(com.re.rikkei_bank.dto.request.ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new CustomException("Mã xác nhận không hợp lệ", HttpStatus.BAD_REQUEST));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Mã xác nhận đã hết hạn", HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Xác nhận mật khẩu mới không khớp", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
