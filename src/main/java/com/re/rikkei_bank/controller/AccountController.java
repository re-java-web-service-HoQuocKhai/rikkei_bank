package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.request.AccountUpdateRequest;
import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.dto.response.ApiResponse;
import com.re.rikkei_bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountUpdateRequest request
    ) {
        AccountResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Cập nhật trạng thái tài khoản thành công")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> lockAccount(@PathVariable Long id) {
        accountService.lockAccount(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Khóa tài khoản thành công")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> unlockAccount(@PathVariable Long id) {
        accountService.unlockAccount(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Mở khóa tài khoản thành công")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<com.re.rikkei_bank.dto.response.BalanceResponse>> getBalance(
            @PathVariable Long id,
            java.security.Principal principal
    ) {
        com.re.rikkei_bank.dto.response.BalanceResponse response = accountService.getBalance(id, principal.getName());
        return ResponseEntity.ok(
                ApiResponse.<com.re.rikkei_bank.dto.response.BalanceResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }
}
