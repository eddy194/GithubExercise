package com.exercise.github.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Repository {
    private String name;
    private String owner;
    private List<Branch> branches;

}