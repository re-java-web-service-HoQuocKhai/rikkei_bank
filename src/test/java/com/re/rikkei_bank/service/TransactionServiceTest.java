package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.TransferRequest;
import com.re.rikkei_bank.dto.response.TransferResponse;
import com.re.rikkei_bank.exception.*;
import com.re.rikkei_bank.model.*;
import com.re.rikkei_bank.repository.AccountRepository;
import com.re.rikkei_bank.repository.TransactionRepository;
import com.re.rikkei_bank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account senderAccount;
    private Account receiverAccount;
    private User sender;

    @BeforeEach
    void setUp() {
        sender = User.builder().username("senderUser").build();

        senderAccount = Account.builder()
                .id(1L)
                .accountNumber("111111")
                .balance(new BigDecimal("1000.00"))
                .transactionPin("encodedPin")
                .status(AccountStatus.ACTIVE)
                .active(true)
                .user(sender)
                .build();

        receiverAccount = Account.builder()
                .id(2L)
                .accountNumber("222222")
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .active(true)
                .build();
    }

    @Test
    void transfer_Success() {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("200.00"), "Pay", "123456");

        when(accountRepository.findByAccountNumberWithLock("111111")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumberWithLock("222222")).thenReturn(Optional.of(receiverAccount));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);

        TransferResponse response = transactionService.transfer(request, "senderUser");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), receiverAccount.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_BalanceNotEnough_ThrowsException() {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("2000.00"), "Pay", "123456");

        when(accountRepository.findByAccountNumberWithLock("111111")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumberWithLock("222222")).thenReturn(Optional.of(receiverAccount));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);

        assertThrows(InsufficientBalanceException.class, () -> transactionService.transfer(request, "senderUser"));
    }

    @Test
    void transfer_InvalidPin_ThrowsException() {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("200.00"), "Pay", "wrongPin");

        when(accountRepository.findByAccountNumberWithLock("111111")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumberWithLock("222222")).thenReturn(Optional.of(receiverAccount));
        when(passwordEncoder.matches("wrongPin", "encodedPin")).thenReturn(false);

        assertThrows(InvalidPinException.class, () -> transactionService.transfer(request, "senderUser"));
    }

    @Test
    void transfer_InvalidReceiver_ThrowsException() {
        TransferRequest request = new TransferRequest("111111", "333333", new BigDecimal("200.00"), "Pay", "123456");

        when(accountRepository.findByAccountNumberWithLock("111111")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumberWithLock("333333")).thenReturn(Optional.empty());

        assertThrows(ReceiverNotFoundException.class, () -> transactionService.transfer(request, "senderUser"));
    }

    @Test
    void transfer_Self_ThrowsException() {
        TransferRequest request = new TransferRequest("111111", "111111", new BigDecimal("200.00"), "Pay", "123456");

        assertThrows(CustomException.class, () -> transactionService.transfer(request, "senderUser"));
    }

    @Test
    void transfer_Unauthorized_ThrowsException() {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("200.00"), "Pay", "123456");

        when(accountRepository.findByAccountNumberWithLock("111111")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumberWithLock("222222")).thenReturn(Optional.of(receiverAccount));

        assertThrows(CustomException.class, () -> transactionService.transfer(request, "otherUser"));
    }

    @Test
    void getTransactionHistory_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));

        Transaction tx = Transaction.builder()
                .transactionCode("TX123")
                .amount(new BigDecimal("100.00"))
                .fromAccount(senderAccount)
                .toAccount(receiverAccount)
                .description("Test tx")
                .build();
        tx.setCreatedAt(java.time.LocalDateTime.now());

        org.springframework.data.domain.Page<Transaction> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(tx));
        when(transactionRepository.getTransactionHistory(eq(1L), any(), any(), any(), any())).thenReturn(page);

        com.re.rikkei_bank.dto.response.TransactionHistoryResponse response = transactionService.getTransactionHistory(1L, null, null, null, 0, 10, "senderUser");

        assertNotNull(response);
        assertEquals("111111", response.getAccountNumber());
        assertEquals(1, response.getTransactions().getContent().size());
        assertEquals("DEBIT", response.getTransactions().getContent().get(0).getType());
        assertEquals(new BigDecimal("-100.00"), response.getTransactions().getContent().get(0).getAmount());
    }

    @Test
    void getTransactionHistory_Empty_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));

        org.springframework.data.domain.Page<Transaction> page = org.springframework.data.domain.Page.empty();
        when(transactionRepository.getTransactionHistory(eq(1L), any(), any(), any(), any())).thenReturn(page);

        com.re.rikkei_bank.dto.response.TransactionHistoryResponse response = transactionService.getTransactionHistory(1L, null, null, null, 0, 10, "senderUser");

        assertNotNull(response);
        assertEquals(0, response.getTransactions().getContent().size());
    }

    @Test
    void getTransactionHistory_Unauthorized_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));

        assertThrows(CustomException.class, () -> transactionService.getTransactionHistory(1L, null, null, null, 0, 10, "hackerUser"));
    }
}
