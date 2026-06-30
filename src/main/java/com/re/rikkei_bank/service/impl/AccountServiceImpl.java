package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.dto.request.AccountUpdateRequest;
import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.exception.AccountNotFoundException;
import com.re.rikkei_bank.mapper.AccountMapper;
import com.re.rikkei_bank.model.Account;
import com.re.rikkei_bank.model.AccountStatus;
import com.re.rikkei_bank.repository.AccountRepository;
import com.re.rikkei_bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với id: " + id));
        return accountMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse updateAccount(Long id, AccountUpdateRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với id: " + id));

        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
            if (request.getStatus() == AccountStatus.CLOSED) {
                account.setActive(false);
            }
        }
        
        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toAccountResponse(updatedAccount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với id: " + id));
        account.setStatus(AccountStatus.LOCKED);
        account.setActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với id: " + id));
        account.setStatus(AccountStatus.ACTIVE);
        account.setActive(true);
        accountRepository.save(account);
    }

    @Override
    public com.re.rikkei_bank.dto.response.BalanceResponse getBalance(Long accountId, String currentUsername) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với id: " + accountId));
        
        if (!account.getUser().getUsername().equals(currentUsername)) {
            throw new com.re.rikkei_bank.exception.CustomException("Bạn không có quyền xem số dư của tài khoản này", org.springframework.http.HttpStatus.FORBIDDEN);
        }

        return com.re.rikkei_bank.dto.response.BalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .lastUpdated(account.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePin(Long accountId, com.re.rikkei_bank.dto.request.ChangePinRequest request, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với id: " + accountId));
        
        if (!account.getUser().getUsername().equals(username)) {
            throw new com.re.rikkei_bank.exception.CustomException("Bạn không có quyền đổi mã PIN của tài khoản này", org.springframework.http.HttpStatus.FORBIDDEN);
        }

        if (account.getTransactionPin() == null || !passwordEncoder.matches(request.getOldPin(), account.getTransactionPin())) {
            throw new com.re.rikkei_bank.exception.CustomException("Mã PIN cũ không chính xác", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new com.re.rikkei_bank.exception.CustomException("Xác nhận mã PIN mới không khớp", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        account.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        accountRepository.save(account);
    }
}
