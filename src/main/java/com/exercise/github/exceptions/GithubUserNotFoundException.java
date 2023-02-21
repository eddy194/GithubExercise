package com.exercise.github.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GithubUserNotFoundException extends RuntimeException {

    public static final String USERNAME_NOT_FOUND_ERROR_MESSAGE = "The specified Github user '%s' could not be found";

    public GithubUserNotFoundException(String username) {
        super(String.format(USERNAME_NOT_FOUND_ERROR_MESSAGE, username));
    }

}