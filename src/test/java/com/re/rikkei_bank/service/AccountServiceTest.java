package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.AccountUpdateRequest;
import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.exception.AccountNotFoundException;
import com.re.rikkei_bank.mapper.AccountMapper;
import com.re.rikkei_bank.model.Account;
import com.re.rikkei_bank.model.AccountStatus;
import com.re.rikkei_bank.repository.AccountRepository;
import com.re.rikkei_bank.service.impl.AccountServiceImpl;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private AccountResponse mockResponse;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .balance(java.math.BigDecimal.valueOf(50000.0))
                .status(AccountStatus.ACTIVE)
                .active(true)
                .build();
        
        mockResponse = new AccountResponse();
        mockResponse.setAccountNumber("1234567890");
    }

    @Test
    void getAccountById_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountMapper.toAccountResponse(testAccount)).thenReturn(mockResponse);

        AccountResponse response = accountService.getAccountById(1L);

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(1L));
    }

    @Test
    void updateAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(accountMapper.toAccountResponse(any(Account.class))).thenReturn(mockResponse);

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setStatus(AccountStatus.CLOSED);

        accountService.updateAccount(1L, request);

        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
        assertFalse(testAccount.getActive());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void lockAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        accountService.lockAccount(1L);

        assertEquals(AccountStatus.LOCKED, testAccount.getStatus());
        assertFalse(testAccount.getActive());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void unlockAccount_Success() {
        testAccount.setStatus(AccountStatus.LOCKED);
        testAccount.setActive(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        accountService.unlockAccount(1L);

        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
        assertTrue(testAccount.getActive());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void getBalance_Success() {
        com.re.rikkei_bank.model.User user = new com.re.rikkei_bank.model.User();
        user.setUsername("owner_user");
        testAccount.setUser(user);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        com.re.rikkei_bank.dto.response.BalanceResponse response = accountService.getBalance(1L, "owner_user");

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
        assertEquals(java.math.BigDecimal.valueOf(50000.0), response.getBalance());
    }

    @Test
    void getBalance_AccessDenied_ThrowsException() {
        com.re.rikkei_bank.model.User user = new com.re.rikkei_bank.model.User();
        user.setUsername("owner_user");
        testAccount.setUser(user);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        com.re.rikkei_bank.exception.CustomException exception = assertThrows(
                com.re.rikkei_bank.exception.CustomException.class,
                () -> accountService.getBalance(1L, "hacker_user")
        );

        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getMessage().contains("không có quyền"));
    }

    @Test
    void getBalance_AccountNotFound_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(1L, "owner_user"));
    }
}
