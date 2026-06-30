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
public class TransactionItemDTO {
    private String transactionCode;
    private String type; // "DEBIT" (-) hoặc "CREDIT" (+)
    private BigDecimal amount; // Sẽ được parse thành số âm nếu là DEBIT
    private String currency;
    private String description;
    private LocalDateTime transactionDate;
    private String relatedAccount; // Gửi tới ai hoặc nhận từ ai
}
