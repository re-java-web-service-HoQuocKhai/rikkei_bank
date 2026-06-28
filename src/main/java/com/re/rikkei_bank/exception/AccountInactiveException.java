package com.re.rikkei_bank.exception;

import org.springframework.http.HttpStatus;

public class AccountInactiveException extends CustomException {
    public AccountInactiveException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
