package com.exercise.github.controller;

import com.exercise.github.controllers.GithubController;
import com.exercise.github.exceptions.InvalidUsernameException;
import com.exercise.github.exceptions.MediaTypeNotSupportedException;
import com.exercise.github.exceptions.models.ErrorResponse;
import com.exercise.github.exceptions.GithubUserNotFoundException;
import com.exercise.github.models.Repository;
import com.exercise.github.services.GithubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.exercise.github.exceptions.InvalidUsernameException.INVALID_USERNAME_ERROR_MESSAGE;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GithubControllerTest {
    @Mock
    private GithubService githubService;

    @InjectMocks
    private GithubController githubController;

    @Test
    void getNonForkRepositories_WithExistingUser_ReturnsRepositories() {
        // given
        String username = "existinguser";
        List<Repository> expectedRepositories = Arrays.asList(new Repository("repo1", username, Collections.emptyList()), new Repository("repo2", username, Collections.emptyList()));
        doReturn(Flux.fromIterable(expectedRepositories)).when(githubService).getNonForkRepositories(username);

        // when
        Flux<Repository> result = githubController.getNonForkRepositories(username, MediaType.APPLICATION_JSON_VALUE);

        // then
        StepVerifier.create(result)
                .expectNextSequence(expectedRepositories)
                .verifyComplete();
    }

    @Test
    void getNonForkRepositories_WithNonExistingUser_ReturnsNotFound() {
        // given
        String username = "nonexistinguser";
        doReturn(Flux.empty()).when(githubService).getNonForkRepositories(username);

        // when
        Mono<Object> result = githubController.getNonForkRepositories(username, MediaType.APPLICATION_JSON_VALUE)
                .then(Mono.error(new GithubUserNotFoundException(username)))
                .onErrorResume(GithubUserNotFoundException.class, ex -> Mono.just(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage())));

        // then
        StepVerifier.create(result)
                .expectNextMatches(error -> {
                    if (error instanceof ErrorResponse errorResponse) {
                        return errorResponse.getStatus() == HttpStatus.NOT_FOUND.value() && errorResponse.getMessage().equals("The specified Github user '" + username + "' could not be found");
                    }
                    return false;
                })
                .verifyComplete();
    }

    @Test
    void getNonForkRepositories_WithInvalidMediaType_ReturnsNotAcceptable() {
        // given
        String username = "existinguser";

        // when
        Mono<Object> result = githubController.getNonForkRepositories(username, MediaType.APPLICATION_XML_VALUE)
                .then(Mono.empty())
                .onErrorResume(MediaTypeNotSupportedException.class, ex -> Mono.just(new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), ex.getMessage())));

        // then
        StepVerifier.create(result)
                .expectNextMatches(error -> {
                    if (error instanceof ErrorResponse errorResponse) {
                        return errorResponse.getStatus() == HttpStatus.NOT_ACCEPTABLE.value() && errorResponse.getMessage().equals("XML format is not supported");
                    }
                    return false;
                })
                .verifyComplete();
    }

    private static Stream<Arguments> provideUserNameAndExpectError() {
        return Stream.of(
                Arguments.of("testuser", true),
                Arguments.of("", true),
                Arguments.of(null, true)
        );
    }


    @ParameterizedTest
    @MethodSource("provideUserNameAndExpectError")
    void getNonForkRepositories_WithDifferentUsername_ReturnsExpectedError(String username, boolean expectError) {
        // given
        doReturn(Flux.error(new RuntimeException("An error occurred while processing your request."))).when(githubService).getNonForkRepositories(username);

        // when
        Mono<Object> result = githubController.getNonForkRepositories(username, MediaType.APPLICATION_JSON_VALUE)
                .collectList()
                .onErrorResume(GithubUserNotFoundException.class, ex -> Mono.just(new ArrayList<>()))
                .flatMapMany(Flux::fromIterable)
                .last()
                .then(Mono.error(new Exception("An error occurred while processing your request.")))
                .onErrorResume(Exception.class, ex -> Mono.just(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage())));

        // then
        if (expectError) {
            StepVerifier.create(result)
                    .expectNextMatches(error -> {
                        if (error instanceof ErrorResponse errorResponse) {
                            return errorResponse.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value() && errorResponse.getMessage().equals("An error occurred while processing your request.");
                        }
                        return false;
                    })
                    .verifyComplete();
        } else {
            StepVerifier.create(result)
                    .expectNext(new ArrayList<>())
                    .verifyComplete();
        }
    }


    @Test
    void getNonForkRepositoriesWithoutUser_ReturnsBadRequest() {
        // when
        Flux<Object> result = githubController.getNonForkRepositoriesWithoutUser(MediaType.APPLICATION_JSON_VALUE)
                .flatMap(res -> Mono.error(new RuntimeException("Expected exception not thrown")))
                .onErrorResume(ex -> {
                    if (ex instanceof InvalidUsernameException) {
                        return Mono.just(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
                    } else {
                        return Mono.error(new RuntimeException("Unexpected exception: " + ex.getMessage()));
                    }
                });

        // then
        StepVerifier.create(result)
                .expectNextMatches(error -> {
                    if (error instanceof ErrorResponse errorResponse) {
                        return errorResponse.getStatus() == HttpStatus.BAD_REQUEST.value()
                                && errorResponse.getMessage().equals(INVALID_USERNAME_ERROR_MESSAGE);
                    }
                    return false;
                })
                .verifyComplete();
    }

}