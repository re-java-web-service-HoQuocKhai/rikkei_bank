package com.re.rikkei_bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {
    private String idNumber;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String address;
    private String status;
}
