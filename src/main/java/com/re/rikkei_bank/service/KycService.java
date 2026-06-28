package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.RejectKycRequest;
import com.re.rikkei_bank.dto.response.KycDetailResponse;
import com.re.rikkei_bank.dto.response.KycResponse;
import com.re.rikkei_bank.model.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KycService {
    Page<KycResponse> searchKycProfiles(KycStatus status, String keyword, Pageable pageable);
    KycDetailResponse getKycDetail(Long id);
    void approveKyc(Long id);
    void rejectKyc(Long id, RejectKycRequest request);
}
