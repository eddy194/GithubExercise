package com.exercise.github.handlers;

import com.exercise.github.exceptions.InvalidUsernameException;
import com.exercise.github.exceptions.models.ErrorResponse;
import com.exercise.github.exceptions.GithubUserNotFoundException;
import com.exercise.github.exceptions.MediaTypeNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * A class containing exception handlers for errors that may occur during REST API requests.
 * The methods in this class handle exceptions and return a response with an appropriate HTTP status code and an error message.
 */
@RestControllerAdvice
public class GithubControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GithubControllerAdvice.class);

    /**
     * Handles the case where a GitHub user is not found, and returns an ErrorResponse with a 404 status code.
     *
     * @param ex the GithubUserNotFoundException that was thrown
     * @return an ErrorResponse with a 404 status code and the message from the exception
     */
    @ExceptionHandler(GithubUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGithubUserNotFoundException(GithubUserNotFoundException ex) {
        logger.error("Github user not found", ex); // log the exception
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /**
     * Handles the case where a media type is not supported, and returns an ErrorResponse with a 406 status code.
     *
     * @param ex the MediaTypeNotSupportedException that was thrown
     * @return an ErrorResponse with a 406 status code and the message from the exception
     */
    @ExceptionHandler(MediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse handleMediaTypeNotSupportedException(MediaTypeNotSupportedException ex) {
        logger.error("Media type not supported", ex); // log the exception
        return new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), ex.getMessage());
    }

    /**
     * Exception handler that handles {@link IllegalArgumentException} and {@link InvalidUsernameException}
     * by returning a 400 Bad Request error with an error response body.
     *
     * @param ex the exception that was thrown
     * @return an ErrorResponse with a 400 status code and the message from the exception
     */
    @ExceptionHandler({IllegalArgumentException.class, InvalidUsernameException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(Exception ex) {
        logger.error("Illegal argument exception", ex); // log the exception
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Handles any other exceptions that are thrown, and returns an ErrorResponse with a 500 status code.
     *
     * @param ex the Exception that was thrown
     * @return an ErrorResponse with a 500 status code and the message from the exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        logger.error("Internal server error", ex); // log the exception
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }
}
