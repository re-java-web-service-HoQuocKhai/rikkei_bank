package com.re.rikkei_bank.exception;

import org.springframework.http.HttpStatus;

public class DuplicateTransactionException extends CustomException {
    public DuplicateTransactionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
