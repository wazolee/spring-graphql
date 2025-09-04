package com.learn.springgraphql.controller;

import com.learn.springgraphql.repository.GitHubRepository;
import com.learn.springgraphql.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GitHubController {
    private final GitHubService gitHubService;

    @Autowired
    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/users/{username}/repositories")
    public Mono<List<GitHubRepository>> getUserRepositories(@PathVariable String username) {
        return gitHubService.fetchUserRepositories(username);
    }
}