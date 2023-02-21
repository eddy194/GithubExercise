package com.exercise.github.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class MediaTypeNotSupportedException extends RuntimeException {

    public MediaTypeNotSupportedException(String message) {
        super(message);
    }
}
