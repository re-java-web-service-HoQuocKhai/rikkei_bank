package com.re.rikkei_bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDetailResponse {
    private Long id;
    private String idNumber;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String address;
    private String cccdFrontUrl;
    private String cccdBackUrl;
    private String selfieUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime verifiedAt;
    
    // User info
    private String username;
    private String email;
    private String phoneNumber;
}
