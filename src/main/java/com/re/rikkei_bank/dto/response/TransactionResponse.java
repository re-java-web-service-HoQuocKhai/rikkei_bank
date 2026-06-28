package com.re.rikkei_bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionCode;
    private BigDecimal amount;
    private String description;
    private String status;
    private String type;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;
}
