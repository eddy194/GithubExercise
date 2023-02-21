package com.exercise.github.controllers;

import com.exercise.github.exceptions.GithubUserNotFoundException;
import com.exercise.github.exceptions.InvalidUsernameException;
import com.exercise.github.exceptions.MediaTypeNotSupportedException;
import com.exercise.github.models.Repository;
import com.exercise.github.services.GithubService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GithubController {
    private final GithubService githubService;
    private static final Logger logger = LoggerFactory.getLogger(GithubController.class);

    /**
     * Get non-fork repositories for the specified user.
     *
     * @param username the username of the user to get repositories for
     * @param acceptHeader the accept header in the request
     * @return a Flux of Repository objects
     */
    @GetMapping(value = "/repositories/{username}")
    public Flux<Repository> getNonForkRepositories(@PathVariable String username,
                                                   @RequestHeader("Accept") String acceptHeader) {
        logger.info("Request received for user: {} with accept header: {}", username, acceptHeader);

        return Mono.just(acceptHeader)
                .filter(header -> !header.equals("application/xml"))
                .switchIfEmpty(Mono.error(new MediaTypeNotSupportedException("XML format is not supported")))
                .thenMany(githubService.getNonForkRepositories(username))
                .switchIfEmpty(Mono.error(new GithubUserNotFoundException(username)))
                .doOnError(ex -> logger.error("Error occurred while getting repositories for user: {}", username, ex));
    }

    /**
     * Returns a 400 Bad Request error with an error response body,
     * indicating that a username is required to access this endpoint.
     *
     * @param acceptHeader the accept header in the request
     * @return a 400 Bad Request error with an error response body
     * @throws InvalidUsernameException if a username is not provided in the request
     */
    @GetMapping({"/repositories", "/repositories/"})
    public Flux<Object> getNonForkRepositoriesWithoutUser(@RequestHeader("Accept") String acceptHeader) {
        logger.info("Request received without username with accept header: {}", acceptHeader);
        return Mono.just(acceptHeader)
                .filter(header -> !header.equals("application/xml"))
                .switchIfEmpty(Mono.error(new MediaTypeNotSupportedException("XML format is not supported")))
                .thenMany(Mono.error(new InvalidUsernameException()));
    }
}
