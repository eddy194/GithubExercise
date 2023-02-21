package com.exercise.github.exceptions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    // HTTP status code for the error response
    private int status;

    // Error message for the error response
    private String message;
}