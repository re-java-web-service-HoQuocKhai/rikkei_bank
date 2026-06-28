package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.dto.request.RejectKycRequest;
import com.re.rikkei_bank.dto.response.KycDetailResponse;
import com.re.rikkei_bank.dto.response.KycResponse;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.mapper.RegisterMapper;
import com.re.rikkei_bank.model.KycProfile;
import com.re.rikkei_bank.model.KycStatus;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.KycProfileRepository;
import com.re.rikkei_bank.repository.UserRepository;
import com.re.rikkei_bank.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;

    @Override
    public Page<KycResponse> searchKycProfiles(KycStatus status, String keyword, Pageable pageable) {
        Page<KycProfile> kycProfiles = kycProfileRepository.searchKycProfiles(status, keyword, pageable);
        return kycProfiles.map(registerMapper::toKycResponse);
    }

    @Override
    public KycDetailResponse getKycDetail(Long id) {
        KycProfile profile = kycProfileRepository.findById(id)
                .orElseThrow(() -> new CustomException("Hồ sơ KYC không tồn tại", HttpStatus.NOT_FOUND));

        User user = profile.getUser();

        return KycDetailResponse.builder()
                .id(profile.getId())
                .idNumber(profile.getIdNumber())
                .fullName(profile.getFullName())
                .dob(profile.getDob())
                .gender(profile.getGender().name())
                .address(profile.getAddress())
                .cccdFrontUrl(profile.getCccdFrontUrl())
                .cccdBackUrl(profile.getCccdBackUrl())
                .selfieUrl(profile.getSelfieUrl())
                .status(profile.getStatus().name())
                .rejectReason(profile.getRejectReason())
                .verifiedAt(profile.getVerifiedAt())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveKyc(Long id) {
        KycProfile profile = kycProfileRepository.findById(id)
                .orElseThrow(() -> new CustomException("Hồ sơ KYC không tồn tại", HttpStatus.NOT_FOUND));

        if (profile.getStatus() != KycStatus.PENDING) {
            throw new CustomException("Chỉ có thể duyệt hồ sơ ở trạng thái PENDING", HttpStatus.BAD_REQUEST);
        }

        profile.setStatus(KycStatus.CONFIRM);
        profile.setVerifiedAt(LocalDateTime.now());
        kycProfileRepository.save(profile);

        User user = profile.getUser();
        user.setIsKyc(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectKyc(Long id, RejectKycRequest request) {
        KycProfile profile = kycProfileRepository.findById(id)
                .orElseThrow(() -> new CustomException("Hồ sơ KYC không tồn tại", HttpStatus.NOT_FOUND));

        if (profile.getStatus() != KycStatus.PENDING) {
            throw new CustomException("Chỉ có thể từ chối hồ sơ ở trạng thái PENDING", HttpStatus.BAD_REQUEST);
        }

        profile.setStatus(KycStatus.REJECT);
        profile.setRejectReason(request.getRejectReason());
        profile.setVerifiedAt(LocalDateTime.now());
        kycProfileRepository.save(profile);

        User user = profile.getUser();
        user.setIsKyc(false);
        userRepository.save(user);
    }
}
