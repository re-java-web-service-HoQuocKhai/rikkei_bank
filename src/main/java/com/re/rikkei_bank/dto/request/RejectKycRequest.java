package com.re.rikkei_bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectKycRequest {
    @NotBlank(message = "Reject reason is required")
    private String rejectReason;
}
