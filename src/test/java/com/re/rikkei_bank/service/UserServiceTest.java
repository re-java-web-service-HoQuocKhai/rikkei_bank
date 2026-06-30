package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.UserUpdateRequest;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.exception.DuplicateResourceException;
import com.re.rikkei_bank.mapper.RegisterMapper;
import com.re.rikkei_bank.model.Role;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.AccountRepository;
import com.re.rikkei_bank.repository.KycProfileRepository;
import com.re.rikkei_bank.repository.RoleRepository;
import com.re.rikkei_bank.repository.TransactionRepository;
import com.re.rikkei_bank.repository.UserRepository;
import com.re.rikkei_bank.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private KycProfileRepository kycProfileRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RegisterMapper registerMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder().id(2L).name("ROLE_CUSTOMER").build();
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@gmail.com")
                .phoneNumber("0987654321")
                .isActive(true)
                .role(role)
                .build();
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(userRepository.existsByPhoneNumberAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("new@gmail.com");
        request.setPhoneNumber("0912345678");
        request.setRoleId(2L);

        userService.updateUser(1L, request);

        assertEquals("new@gmail.com", testUser.getEmail());
        assertEquals("0912345678", testUser.getPhoneNumber());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_DuplicateEmail_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(true);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("dup@gmail.com");

        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    void deleteUser_WithTransactions_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.hasTransactionsByUserId(1L)).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> userService.deleteUser(1L));
        assertTrue(exception.getMessage().contains("Không thể xóa cứng"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_NoTransactions_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.hasTransactionsByUserId(1L)).thenReturn(false);
        when(kycProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        userService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void lockUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.lockUser(1L);

        assertFalse(testUser.getIsActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void unlockUser_Success() {
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.unlockUser(1L);

        assertTrue(testUser.getIsActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void searchUsers_Success() {
        com.re.rikkei_bank.dto.projection.UserProjection projection = new com.re.rikkei_bank.dto.projection.UserProjection(
                1L, "testuser", "test@gmail.com", "0987654321", "ROLE_CUSTOMER", true, false, null);
        org.springframework.data.domain.Page<com.re.rikkei_bank.dto.projection.UserProjection> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(projection));
        
        when(userRepository.searchUsers(any(), any(), any(), any(), any())).thenReturn(page);

        com.re.rikkei_bank.dto.response.PageResponse<com.re.rikkei_bank.dto.projection.UserProjection> response = 
                userService.searchUsers("test", null, true, null, org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("testuser", response.getContent().get(0).getUsername());
    }
}
