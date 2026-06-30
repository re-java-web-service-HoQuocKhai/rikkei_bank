package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.AccountUpdateRequest;
import com.re.rikkei_bank.dto.response.AccountResponse;

import com.re.rikkei_bank.dto.response.BalanceResponse;

public interface AccountService {
    AccountResponse getAccountById(Long id);
    AccountResponse updateAccount(Long id, AccountUpdateRequest request);
    void lockAccount(Long id);
    void unlockAccount(Long id);
    BalanceResponse getBalance(Long accountId, String currentUsername);
}
