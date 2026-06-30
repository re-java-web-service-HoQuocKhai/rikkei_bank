package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.dto.request.TransferRequest;
import com.re.rikkei_bank.dto.response.TransferResponse;
import com.re.rikkei_bank.exception.*;
import com.re.rikkei_bank.model.Account;
import com.re.rikkei_bank.model.AccountStatus;
import com.re.rikkei_bank.model.Transaction;
import com.re.rikkei_bank.model.TransactionStatus;
import com.re.rikkei_bank.model.TransactionType;
import com.re.rikkei_bank.repository.AccountRepository;
import com.re.rikkei_bank.repository.TransactionRepository;
import com.re.rikkei_bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transfer(TransferRequest request, String username) {
        
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new CustomException("Cannot transfer to the same account", HttpStatus.BAD_REQUEST);
        }

        String firstLock = request.getFromAccountNumber().compareTo(request.getToAccountNumber()) < 0 
            ? request.getFromAccountNumber() : request.getToAccountNumber();
        String secondLock = request.getFromAccountNumber().compareTo(request.getToAccountNumber()) < 0 
            ? request.getToAccountNumber() : request.getFromAccountNumber();

        Account firstAccount = accountRepository.findByAccountNumberWithLock(firstLock)
                .orElseThrow(() -> new ReceiverNotFoundException("Account not found: " + firstLock));
        
        Account secondAccount = accountRepository.findByAccountNumberWithLock(secondLock)
                .orElseThrow(() -> new ReceiverNotFoundException("Account not found: " + secondLock));

        Account senderAccount = firstAccount.getAccountNumber().equals(request.getFromAccountNumber()) ? firstAccount : secondAccount;
        Account receiverAccount = secondAccount.getAccountNumber().equals(request.getToAccountNumber()) ? secondAccount : firstAccount;

        if (!senderAccount.getUser().getUsername().equals(username)) {
            throw new CustomException("You do not own this account", HttpStatus.FORBIDDEN);
        }

        if (senderAccount.getStatus() == AccountStatus.LOCKED || receiverAccount.getStatus() == AccountStatus.LOCKED) {
            throw new AccountLockedException("One of the accounts is locked");
        }

        if (!senderAccount.getActive() || !receiverAccount.getActive() || 
            senderAccount.getStatus() == AccountStatus.CLOSED || receiverAccount.getStatus() == AccountStatus.CLOSED) {
            throw new AccountInactiveException("One of the accounts is inactive or closed");
        }

        if (senderAccount.getTransactionPin() == null || !passwordEncoder.matches(request.getPin(), senderAccount.getTransactionPin())) {
            throw new InvalidPinException("Invalid transaction PIN");
        }

        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        String txCode = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionCode(txCode)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.INTERNAL)
                .fromAccount(senderAccount)
                .toAccount(receiverAccount)
                .build();
        
        transactionRepository.save(transaction);

        return TransferResponse.builder()
                .transactionCode(txCode)
                .status("SUCCESS")
                .message("Transfer successful")
                .build();
    }

    @Override
    public com.re.rikkei_bank.dto.response.TransactionHistoryResponse getTransactionHistory(Long accountId, String type, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, int page, int size, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new com.re.rikkei_bank.exception.AccountNotFoundException("Không tìm thấy tài khoản với id: " + accountId));
        
        if (!account.getUser().getUsername().equals(username)) {
            throw new CustomException("Bạn không có quyền xem sao kê tài khoản này", HttpStatus.FORBIDDEN);
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Transaction> transactionPage = transactionRepository.getTransactionHistory(accountId, type, startDate, endDate, pageable);

        java.util.List<com.re.rikkei_bank.dto.response.TransactionItemDTO> dtoList = transactionPage.getContent().stream().map(t -> {
            com.re.rikkei_bank.dto.response.TransactionItemDTO dto = new com.re.rikkei_bank.dto.response.TransactionItemDTO();
            dto.setTransactionCode(t.getTransactionCode());
            dto.setCurrency("VND"); // Assuming VND for now
            dto.setDescription(t.getDescription());
            dto.setTransactionDate(t.getCreatedAt());

            if (t.getFromAccount() != null && t.getFromAccount().getId().equals(accountId)) {
                dto.setType("DEBIT");
                dto.setAmount(t.getAmount().negate());
                dto.setRelatedAccount(t.getToAccount() != null ? t.getToAccount().getAccountNumber() : null);
            } else {
                dto.setType("CREDIT");
                dto.setAmount(t.getAmount());
                dto.setRelatedAccount(t.getFromAccount() != null ? t.getFromAccount().getAccountNumber() : null);
            }
            return dto;
        }).toList();

        com.re.rikkei_bank.dto.response.PageResponse<com.re.rikkei_bank.dto.response.TransactionItemDTO> pageResponse = 
                new com.re.rikkei_bank.dto.response.PageResponse<>(
                dtoList,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );

        return com.re.rikkei_bank.dto.response.TransactionHistoryResponse.builder()
                .accountNumber(account.getAccountNumber())
                .currentBalance(account.getBalance())
                .transactions(pageResponse)
                .build();
    }
}
