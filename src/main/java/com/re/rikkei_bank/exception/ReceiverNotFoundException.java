package com.re.rikkei_bank.exception;

import org.springframework.http.HttpStatus;

public class ReceiverNotFoundException extends CustomException {
    public ReceiverNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
