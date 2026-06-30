package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.TransferRequest;
import com.re.rikkei_bank.dto.response.TransactionHistoryResponse;
import com.re.rikkei_bank.dto.response.TransferResponse;

import java.time.LocalDateTime;

public interface TransactionService {
    TransferResponse transfer(TransferRequest request, String username);
    TransactionHistoryResponse getTransactionHistory(Long accountId, String type, LocalDateTime startDate, LocalDateTime endDate, int page, int size, String username);
}
