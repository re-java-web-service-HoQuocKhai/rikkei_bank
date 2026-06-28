package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.request.RejectKycRequest;
import com.re.rikkei_bank.dto.response.ApiResponse;
import com.re.rikkei_bank.dto.response.KycDetailResponse;
import com.re.rikkei_bank.dto.response.KycResponse;
import com.re.rikkei_bank.model.KycStatus;
import com.re.rikkei_bank.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<KycResponse>>> getKycList(
            @RequestParam(required = false) KycStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<KycResponse> response = kycService.searchKycProfiles(status, keyword, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<KycResponse>>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<KycDetailResponse>> getKycDetail(@PathVariable Long id) {
        KycDetailResponse response = kycService.getKycDetail(id);
        return ResponseEntity.ok(
                ApiResponse.<KycDetailResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> approveKyc(@PathVariable Long id) {
        kycService.approveKyc(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Duyệt hồ sơ thành công")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> rejectKyc(
            @PathVariable Long id,
            @Valid @RequestBody RejectKycRequest request
    ) {
        kycService.rejectKyc(id, request);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Từ chối hồ sơ thành công")
                        .data(null)
                        .build()
        );
    }
}
