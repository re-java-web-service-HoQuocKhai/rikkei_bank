package com.re.rikkei_bank.exception;

import org.springframework.http.HttpStatus;

public class InvalidPinException extends CustomException {
    public InvalidPinException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
