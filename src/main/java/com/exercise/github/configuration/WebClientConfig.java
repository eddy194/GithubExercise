package com.exercise.github.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * A configuration class for creating a `WebClient` instance with the base URL set to the GitHub API base URL.
 * This configuration class uses the `github.api.base.url` property to set the base URL for the `WebClient` instance.
 */
@Configuration
public class WebClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${github.api.base.url}")
    private String githubApiBaseUrl; // The base URL for the GitHub API

    /**
     * Creates a new WebClient with the base URL set to the GitHub API base URL.
     *
     * @return a new WebClient instance
     */
    @Bean
    public WebClient webClient() {
        logger.info("Creating WebClient with base URL: {}", githubApiBaseUrl); // Log the base URL
        return WebClient.builder()
                .baseUrl(githubApiBaseUrl) // Set the base URL for the client
                .build();
    }
}
