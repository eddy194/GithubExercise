package com.exercise.github.controller;

import com.exercise.github.models.Branch;
import com.exercise.github.models.Commit;
import com.exercise.github.models.Repository;
import com.exercise.github.services.GithubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

import static com.exercise.github.exceptions.GithubUserNotFoundException.USERNAME_NOT_FOUND_ERROR_MESSAGE;
import static com.exercise.github.exceptions.InvalidUsernameException.INVALID_USERNAME_ERROR_MESSAGE;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubControllerIntegrationTests {

    @MockBean
    private GithubService githubService;

    @Autowired
    private WebTestClient webTestClient;

    Branch githubBranch1 = new Branch("name1", new Commit("sha1"));
    Branch githubBranch2 = new Branch("name2", new Commit("sha2"));

    List branches = List.of(githubBranch1, githubBranch2);
    String username = "username";
    String nonexistentUser = "nonexistent";
    String emptyUsername = "";

    @Test
    void testGetNonForkRepositoriesSuccess() {
        List<Repository> expectedRepositories = Arrays.asList(new Repository("repo1", "owner", branches),
                new Repository("repo2", "owner", branches));
        given(githubService.getNonForkRepositories(username)).willReturn(Flux.fromIterable(expectedRepositories));

        webTestClient.get().uri("/api/repositories/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Repository.class)
                .isEqualTo(expectedRepositories);
    }

    @Test
    void testGetNonForkRepositoriesUserNotFound() {
        given(githubService.getNonForkRepositories(nonexistentUser)).willReturn(Flux.empty());

        webTestClient.get().uri("/api/repositories/{username}", nonexistentUser)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo(String.format(USERNAME_NOT_FOUND_ERROR_MESSAGE, nonexistentUser));
    }

    @Test
    void testGetNonForkRepositoriesInternalServerError() {
        given(githubService.getNonForkRepositories(username)).willThrow(new RuntimeException("Something went wrong"));

        webTestClient.get().uri("/api/repositories/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.message").isEqualTo("Something went wrong");
    }

    @Test
    void testGetNonForkRepositoriesXmlFormatNotSupported() {
        webTestClient.get().uri("/api/repositories/{username}", username)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_TYPE, "application/xml;charset=UTF-8")
                .expectBody()
                .xpath("/ErrorResponse/status").isEqualTo("406")
                .xpath("/ErrorResponse/message").isEqualTo("XML format is not supported");

    }

    @Test
    void testGetNonForkRepositoriesNullUsername() {
        String username = null;

        webTestClient.get().uri("/api/repositories/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo(INVALID_USERNAME_ERROR_MESSAGE);
    }

    @Test
    void testGetNonForkRepositoriesEmptyUsername() {
        webTestClient.get().uri("/api/repositories/{username}", emptyUsername)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo(INVALID_USERNAME_ERROR_MESSAGE);
    }
}