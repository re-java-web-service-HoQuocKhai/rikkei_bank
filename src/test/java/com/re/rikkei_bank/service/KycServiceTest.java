package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.RejectKycRequest;
import com.re.rikkei_bank.dto.request.ResubmitKycRequest;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.mapper.RegisterMapper;
import com.re.rikkei_bank.model.KycProfile;
import com.re.rikkei_bank.model.KycStatus;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.KycProfileRepository;
import com.re.rikkei_bank.repository.UserRepository;
import com.re.rikkei_bank.service.UploadService;
import com.re.rikkei_bank.service.impl.KycServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private KycProfileRepository kycProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegisterMapper registerMapper;

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private KycServiceImpl kycService;

    private KycProfile pendingProfile;
    private KycProfile confirmedProfile;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .isKyc(false)
                .build();

        pendingProfile = KycProfile.builder()
                .id(1L)
                .status(KycStatus.PENDING)
                .user(user)
                .build();

        confirmedProfile = KycProfile.builder()
                .id(2L)
                .status(KycStatus.CONFIRM)
                .user(user)
                .build();
    }

    @Test
    void approveKyc_Success() {
        when(kycProfileRepository.findById(1L)).thenReturn(Optional.of(pendingProfile));

        kycService.approveKyc(1L);

        assertEquals(KycStatus.CONFIRM, pendingProfile.getStatus());
        assertNotNull(pendingProfile.getVerifiedAt());
        assertTrue(user.getIsKyc());

        verify(kycProfileRepository).save(pendingProfile);
        verify(userRepository).save(user);
    }

    @Test
    void approveKyc_AlreadyConfirmed_ThrowsException() {
        when(kycProfileRepository.findById(2L)).thenReturn(Optional.of(confirmedProfile));

        assertThrows(CustomException.class, () -> kycService.approveKyc(2L));
        verify(kycProfileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectKyc_Success() {
        when(kycProfileRepository.findById(1L)).thenReturn(Optional.of(pendingProfile));

        RejectKycRequest request = new RejectKycRequest();
        request.setRejectReason("Ảnh CCCD bị mờ");

        kycService.rejectKyc(1L, request);

        assertEquals(KycStatus.REJECT, pendingProfile.getStatus());
        assertEquals("Ảnh CCCD bị mờ", pendingProfile.getRejectReason());
        assertNotNull(pendingProfile.getVerifiedAt());
        assertFalse(user.getIsKyc());

        verify(kycProfileRepository).save(pendingProfile);
        verify(userRepository).save(user);
    }

    @Test
    void rejectKyc_AlreadyConfirmed_ThrowsException() {
        when(kycProfileRepository.findById(2L)).thenReturn(Optional.of(confirmedProfile));

        RejectKycRequest request = new RejectKycRequest();
        request.setRejectReason("Ảnh CCCD bị mờ");

        assertThrows(CustomException.class, () -> kycService.rejectKyc(2L, request));
        verify(kycProfileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getKycDetail_NotFound_ThrowsException() {
        when(kycProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> kycService.getKycDetail(99L));
    }

    @Test
    void resubmitKyc_Success() throws java.io.IOException {
        User resubmitUser = User.builder().id(1L).username("testuser").isKyc(false).build();
        KycProfile rejectedProfile = KycProfile.builder()
                .id(1L)
                .idNumber("123456789012")
                .status(KycStatus.REJECT)
                .user(resubmitUser)
                .build();

        ResubmitKycRequest request = new ResubmitKycRequest();
        request.setFullName("Nguyen Van A");
        request.setIdNumber("123456789012");
        request.setDob(LocalDate.now());
        request.setAddress("Hanoi");
        request.setGender(com.re.rikkei_bank.model.Gender.MALE);
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        request.setCccdFront(file);
        request.setCccdBack(file);
        request.setSelfie(file);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(resubmitUser));
        when(kycProfileRepository.findByUserId(1L)).thenReturn(Optional.of(rejectedProfile));
        when(uploadService.uploadFile(any())).thenReturn("http://image.url");

        kycService.resubmitKyc(request, "testuser");

        assertEquals(KycStatus.PENDING, rejectedProfile.getStatus());
        assertNull(rejectedProfile.getRejectReason());
        verify(uploadService, times(3)).uploadFile(any());
        verify(kycProfileRepository).save(rejectedProfile);
    }
}
