package com.exercise.github.services;

import com.exercise.github.exceptions.GithubUserNotFoundException;
import com.exercise.github.models.*;
import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * A service class for making requests to the GitHub API to retrieve information about repositories and their branches.
 */
@Service
@RequiredArgsConstructor
public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    @Value("${github.api.base.url}")
    private String githubApiBaseUrl; // The base URL for the GitHub API

    @Value("${github.api.branches.uri}")
    private String branchesUri;

    @Value("${github.api.repos.uri}")
    private String reposUri;

    private final WebClient webClient; // The WebClient instance used to make requests to the GitHub API

    /**
     * Retrieves the non-fork repositories of a given GitHub user and returns a Flux of Repository objects.
     *
     * @param username the GitHub username of the user whose repositories are to be retrieved
     * @return a Flux of Repository objects
     * @throws IllegalArgumentException if the provided username is null or empty
     */
    public Flux<Repository> getNonForkRepositories(@NonNull @NotEmpty String username) {

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        logger.info("Retrieving repositories for user: {}", username);

        return webClient.get()
                .uri(reposUri, username)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(GithubRepoResponse.class)
                .filter(repo -> !repo.isFork())
                .flatMap(this::getRepositoryWithBranches)
                .onErrorResume(WebClientResponseException.class, ex -> handleWebClientResponseException(ex, username));
    }

    /**
     * Returns a Mono of a Repository object with its branches included.
     *
     * @param repo the GitHubRepoResponse object whose branches are to be retrieved
     * @return a Mono of a Repository object
     * @throws IllegalArgumentException if the provided repo is null
     */
    private Mono<Repository> getRepositoryWithBranches(GithubRepoResponse repo) {
        if (repo == null) {
            throw new IllegalArgumentException("Repo cannot be null");
        }

        return getBranches(repo.getOwner().getLogin(), repo.getName())
                .map(branches -> branches.stream()
                        .map(branch -> new Branch(branch.getName(), new Commit(branch.getCommit().getSha())))
                        .toList())
                .map(branches -> new Repository(repo.getName(), repo.getOwner().getLogin(), branches));
    }

    /**
     * Handles the WebClientResponseException that is thrown if an error occurs while retrieving repositories
     * for a given user.
     *
     * @param ex the WebClientResponseException that was thrown
     * @param username the GitHub username of the user whose repositories were being retrieved
     * @return a Mono of a Repository object
     */
    private Mono<Repository> handleWebClientResponseException(WebClientResponseException ex, String username) {
        logger.error("Error occurred while retrieving repositories for user: {}", username, ex);

        HttpStatus status = (HttpStatus) ex.getStatusCode();

        if (status == HttpStatus.NOT_FOUND) {
            return Mono.error(new GithubUserNotFoundException(username));
        } else {
            return Mono.error(new Exception("An error occurred while processing your request."));
        }

    }

    /**
     * Retrieves the branches of a given GitHub repository and returns a Mono of a list of Branch objects.
     *
     * @param owner the owner of the repository
     * @param repo the name of the repository
     * @return a Mono of a list of Branch objects
     * @throws IllegalArgumentException if either owner or repo is null or empty
     */
    public Mono<List<Branch>> getBranches(String owner, String repo) {
        if (StringUtils.isBlank(owner) || StringUtils.isBlank(repo) ) {
            throw new IllegalArgumentException("Owner and Repo cannot be null or empty");
        }

        logger.info("Retrieving branches for repository: {}/{}", owner, repo); // Log the request

        return webClient.get()
                .uri(branchesUri, owner, repo)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(Branch.class)
                .collectList();
    }
}
