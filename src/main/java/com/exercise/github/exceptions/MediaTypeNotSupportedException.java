package com.exercise.github.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class MediaTypeNotSupportedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MediaTypeNotSupportedException(String message) {
        super(message);
    }
}
