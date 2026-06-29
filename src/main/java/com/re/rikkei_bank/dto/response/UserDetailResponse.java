package com.re.rikkei_bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private Boolean isActive;
    private Boolean isKyc;
    private LocalDateTime createdAt;

    private AccountResponse account;
    private KycResponse kycProfile;
}
