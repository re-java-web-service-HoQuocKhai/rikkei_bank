package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.request.TransferRequest;
import com.re.rikkei_bank.dto.response.ApiResponse;
import com.re.rikkei_bank.dto.response.TransferResponse;
import com.re.rikkei_bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        TransferResponse transferResponse = transactionService.transfer(request, username);
        
        return ResponseEntity.ok(
                ApiResponse.<TransferResponse>builder()
                        .success(true)
                        .data(transferResponse)
                        .build()
        );
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<com.re.rikkei_bank.dto.response.TransactionHistoryResponse>> getTransactionHistory(
            @RequestParam Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String username = authentication.getName();
        com.re.rikkei_bank.dto.response.TransactionHistoryResponse response = transactionService.getTransactionHistory(accountId, type, startDate, endDate, page, size, username);

        return ResponseEntity.ok(
                ApiResponse.<com.re.rikkei_bank.dto.response.TransactionHistoryResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }
}
