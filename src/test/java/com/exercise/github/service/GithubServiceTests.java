package com.exercise.github.service;

import com.exercise.github.exceptions.GithubUserNotFoundException;
import com.exercise.github.models.*;
import com.exercise.github.services.GithubService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubServiceTests {
    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private GithubService githubService;

    String branchesUri = "branchesUri";
    String reposUri = "reposUri";
    String username = "testUser";
    String nonexistentUser = "nonexistent";
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec requestHeadersSpecRepo = Mockito.mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpecRepo = Mockito.mock(WebClient.ResponseSpec.class);
    WebClient.RequestHeadersSpec requestHeadersSpecBranches = Mockito.mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpecBranches = Mockito.mock(WebClient.ResponseSpec.class);
    Branch githubBranch1 = new Branch("name1", new Commit("sha1"));
    Branch githubBranch2 = new Branch("name2", new Commit("sha2"));
    GithubRepoResponse githubRepoResponse1 = new GithubRepoResponse("repo1", false, new User("owner1"));
    GithubRepoResponse githubRepoResponse2 = new GithubRepoResponse("repo2", false, new User("owner2"));

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(githubService, "branchesUri", branchesUri);
        ReflectionTestUtils.setField(githubService, "reposUri", reposUri);
    }

    @Test
    void getBranchesTest() {
        // given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString(), anyString())).thenReturn(requestHeadersSpecBranches);
        when(requestHeadersSpecBranches.header(anyString(), anyString())).thenReturn(requestHeadersSpecBranches);
        when(requestHeadersSpecBranches.retrieve()).thenReturn(responseSpecBranches);
        when(responseSpecBranches.bodyToFlux((Class<Object>) any())).thenReturn(Flux.just(githubBranch1, githubBranch2));

        // when
        Mono<List<Branch>> branchesMono = githubService.getBranches("owner", "repo");

        // then
        StepVerifier.create(branchesMono)
                .expectNext(Arrays.asList(new Branch("name1", new Commit("sha1")), new Branch("name2", new Commit("sha2"))))
                .verifyComplete();
        verify(webClient, times(1)).get();
        verify(webClient.get(), times(1)).uri(anyString(), anyString(), anyString());
        verify(webClient.get().uri(anyString(), anyString(), anyString()), times(1)).header(anyString(), anyString());
        verify(webClient.get().uri(anyString(), anyString(), anyString()).header(anyString(), anyString()), times(1)).retrieve();
        verify(webClient.get().uri(anyString(), anyString(), anyString()).header(anyString(), anyString()).retrieve(), times(1)).bodyToFlux((Class<Object>) any());
    }

    @Test
    void getNonForkRepositoriesTest() {
        // given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.header(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.retrieve()).thenReturn(responseSpecRepo);
        when(responseSpecRepo.bodyToFlux((Class<Object>) any())).thenReturn(Flux.just(githubRepoResponse1, githubRepoResponse2));

        when(requestHeadersUriSpec.uri(anyString(), anyString(), anyString())).thenReturn(requestHeadersSpecBranches);
        when(requestHeadersSpecBranches.header(anyString(), anyString())).thenReturn(requestHeadersSpecBranches);
        when(requestHeadersSpecBranches.retrieve()).thenReturn(responseSpecBranches);
        when(responseSpecBranches.bodyToFlux((Class<Object>) any())).thenReturn(Flux.just(githubBranch1, githubBranch2));

        // when
        Flux<Repository> repositoryFlux = githubService.getNonForkRepositories("username");

        // then
        StepVerifier.create(repositoryFlux)
                .expectNext(new Repository("repo1", "owner1", Arrays.asList(githubBranch1, githubBranch2)))
                .expectNext(new Repository("repo2", "owner2", Arrays.asList(githubBranch1, githubBranch2)))
                .verifyComplete();
        verify(webClient, times(3)).get();
        verify(webClient.get(), times(1)).uri(anyString(), anyString());
        verify(webClient.get().uri(anyString(), anyString()), times(1)).header(anyString(), anyString());
        verify(webClient.get().uri(anyString(), anyString()).header(anyString(), anyString()), times(1)).retrieve();
        verify(webClient.get().uri(anyString(), anyString()).header(anyString(), anyString()).retrieve(), times(1)).bodyToFlux((Class<Object>) any());
        verify(webClient.get(), times(2)).uri(anyString(), anyString(), anyString());
        verify(webClient.get().uri(anyString(), anyString(), anyString()), times(2)).header(anyString(), anyString());
        verify(webClient.get().uri(anyString(), anyString(), anyString()).header(anyString(), anyString()), times(2)).retrieve();
        verify(webClient.get().uri(anyString(), anyString(), anyString()).header(anyString(), anyString()).retrieve(), times(2)).bodyToFlux((Class<Object>) any());
    }

    @Test
    void getNonForkRepositories_WithUserNotFound_ReturnsEmpty() {
        // given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.header(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.retrieve()).thenReturn(responseSpecRepo);
        when(responseSpecRepo.bodyToFlux((Class<Object>) any())).thenReturn(Flux.empty());

        // when
        Flux<Repository> repositoryFlux = githubService.getNonForkRepositories(nonexistentUser);

        // then
        StepVerifier.create(repositoryFlux)
                .verifyComplete();
    }

    @Test
    void getNonForkRepositories_WithWebClientError_ReturnsError() {
        // given
        WebClientResponseException ex = new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.header(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.retrieve()).thenReturn(responseSpecRepo);
        when(responseSpecRepo.bodyToFlux((Class<Object>) any())).thenReturn(Flux.error(ex));

        // when
        Flux<Repository> repositoryFlux = githubService.getNonForkRepositories(username);

        // then
        StepVerifier.create(repositoryFlux)
                .verifyError();
    }

    @Test
    void getNonForkRepositories_WithWebClientError_ReturnsGithubUserNotFoundException() {
        // given
        WebClientResponseException ex = new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.header(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.retrieve()).thenReturn(responseSpecRepo);
        when(responseSpecRepo.bodyToFlux((Class<Object>) any())).thenReturn(Flux.error(ex));

        // when
        Flux<Repository> repositoryFlux = githubService.getNonForkRepositories(username);

        // then
        StepVerifier.create(repositoryFlux)
                .verifyError(GithubUserNotFoundException.class);
    }

    @Test
    void getNonForkRepositories_WithWebClientError_ReturnsGeneralException() {
        // given
        WebClientResponseException ex = new WebClientResponseException(HttpStatus.FORBIDDEN.value(), "Forbidden", null, null, null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.header(anyString(), anyString())).thenReturn(requestHeadersSpecRepo);
        when(requestHeadersSpecRepo.retrieve()).thenReturn(responseSpecRepo);
        when(responseSpecRepo.bodyToFlux((Class<Object>) any())).thenReturn(Flux.error(ex));

        // when
        Flux<Repository> repositoryFlux = githubService.getNonForkRepositories(username);

        // then
        StepVerifier.create(repositoryFlux)
                .verifyError();
    }

    @Test
    void getNonForkRepositories_WithNullUsername_ThrowsException() {
        // when
        Assertions.assertThrows(NullPointerException.class, () -> {
            githubService.getNonForkRepositories(null);
        });
    }

    @Test
    void getNonForkRepositories_WithEmptyUsername_ThrowsException() {
        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            githubService.getNonForkRepositories("");
        });
    }

    @Test
    void getBranches_WithNullOwner_ThrowsException() {
        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            githubService.getBranches(null, "test");
        });
    }

    @Test
    void getBranches_WithNullRepo_ThrowsException() {
        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            githubService.getBranches("test-owner", null);
        });
    }

    @Test
    void getBranches_WithEmptyOwner_ThrowsException() {
        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            githubService.getBranches("", "test");
        });
    }

    @Test
    void getBranches_WithEmptyRepo_ThrowsException() {
        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            githubService.getBranches("test-owner", "");
        });
    }



}