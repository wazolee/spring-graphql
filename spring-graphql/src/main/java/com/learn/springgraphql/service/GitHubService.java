package com.learn.springgraphql.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.springgraphql.repository.GitHubRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GitHubService(@Value("${github.api.url}") String apiUrl, @Value("${github.api.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", token)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<List<GitHubRepository>> fetchUserRepositories(String username) {
        String query = """
                {
                    "query": "query { user(login: \\"%s\\") { repositories(first: 10) { nodes { name url } } } }"
                }
                """.formatted(username);

        return webClient.post()
                .bodyValue(query)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("GitHub API error: " + response.statusCode().value())))
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(response);
                        JsonNode reposNode = jsonNode.get("data").get("user").get("repositories").get("nodes");
                        return objectMapper.convertValue(reposNode, objectMapper.getTypeFactory().constructCollectionType(List.class, GitHubRepository.class));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse GitHub API response", e);
                    }
                });
    }
}