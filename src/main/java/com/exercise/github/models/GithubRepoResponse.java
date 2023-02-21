package com.exercise.github.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GithubRepoResponse {
    private String name;
    private boolean fork;
    private User owner;
}
