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
}
