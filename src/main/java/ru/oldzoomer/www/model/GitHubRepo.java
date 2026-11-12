package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public record GitHubRepo(
    String name,
    String description,
    @JsonAlias({"html_url", "htmlUrl"}) String htmlUrl,
    String language,
    @JsonAlias({"stargazers_count", "stargazersCount"}) int stargazersCount,
    @JsonAlias({"forks_count", "forksCount"}) int forksCount,
    boolean isFork,
    boolean isPrivate,
    @JsonAlias({"default_branch", "defaultBranch"}) String defaultBranch,
    @JsonAlias({"updated_at", "updatedAt"}) String updatedAt,
    List<String> topics
) {
    public GitHubRepo {
        if (topics == null) topics = Collections.emptyList();
    }

    public static GitHubRepo of(
        String name,
        String description,
        String htmlUrl,
        String language,
        int stargazersCount,
        int forksCount,
        boolean isFork,
        boolean isPrivate,
        String defaultBranch,
        String updatedAt,
        List<String> topics
    ) {
        return new GitHubRepo(name, description, htmlUrl, language, stargazersCount, forksCount,
            isFork, isPrivate, defaultBranch, updatedAt, topics);
    }
}
