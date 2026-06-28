package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.RegisterRequest;
import com.re.rikkei_bank.dto.response.RegisterResponse;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.exception.DuplicateResourceException;
import com.re.rikkei_bank.exception.InvalidFileException;
import com.re.rikkei_bank.mapper.RegisterMapper;
import com.re.rikkei_bank.model.Account;
import com.re.rikkei_bank.model.Gender;
import com.re.rikkei_bank.model.KycProfile;
import com.re.rikkei_bank.model.Role;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.AccountRepository;
import com.re.rikkei_bank.repository.KycProfileRepository;
import com.re.rikkei_bank.repository.RoleRepository;
import com.re.rikkei_bank.repository.UserRepository;
import com.re.rikkei_bank.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private KycProfileRepository kycProfileRepository;
    @Mock
    private UploadService uploadService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RegisterMapper registerMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest request;
    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        validFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        request = new RegisterRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("12345678");
        request.setConfirmPassword("12345678");
        request.setFullName("Nguyen Van A");
        request.setPhoneNumber("0123456789");
        request.setIdNumber("001099001234");
        request.setDob(LocalDate.of(2000, 1, 1));
        request.setAddress("Hanoi");
        request.setGender(Gender.MALE);
        request.setCccdFront(validFile);
        request.setCccdBack(validFile);
        request.setSelfie(validFile);
    }

    @Test
    void register_Success() throws IOException {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(kycProfileRepository.existsByIdNumber(anyString())).thenReturn(false);

        when(uploadService.uploadFile(any())).thenReturn("http://image.url");

        Role role = Role.builder().name("ROLE_CUSTOMER").build();
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(role));

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(kycProfileRepository.save(any(KycProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        RegisterResponse response = authService.register(request);
        
        assertNotNull(response);

        verify(uploadService, times(3)).uploadFile(any());
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
        verify(kycProfileRepository).save(any(KycProfile.class));
    }

    @Test
    void register_PasswordNotMatch_ThrowsException() {
        request.setConfirmPassword("wrong");
        assertThrows(CustomException.class, () -> authService.register(request));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void register_InvalidFile_ThrowsException() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes()
        );
        request.setCccdFront(invalidFile);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(kycProfileRepository.existsByIdNumber(anyString())).thenReturn(false);

        assertThrows(InvalidFileException.class, () -> authService.register(request));
    }
}
