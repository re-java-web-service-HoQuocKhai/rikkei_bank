package com.re.rikkei_bank.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends CustomException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
