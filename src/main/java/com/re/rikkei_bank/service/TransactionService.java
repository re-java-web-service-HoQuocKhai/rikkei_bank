package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.TransferRequest;
import com.re.rikkei_bank.dto.response.TransferResponse;

public interface TransactionService {
    TransferResponse transfer(TransferRequest request, String username);
}
