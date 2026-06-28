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
}
