package com.exercise.github.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)

public class InvalidUsernameException extends RuntimeException{

    public static final String INVALID_USERNAME_ERROR_MESSAGE = "The username must not be empty or null";

    public InvalidUsernameException() {
        super(INVALID_USERNAME_ERROR_MESSAGE);
    }
}
