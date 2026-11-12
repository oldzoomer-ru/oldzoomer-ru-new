package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public record GitHubProfile(
    String login,
    String name,
    String bio,
    @JsonAlias({"avatar_url", "avatarUrl"}) String avatarUrl,
    @JsonAlias({"html_url", "htmlUrl"}) String htmlUrl,
    String location,
    String email,
    String blog,
    int followers,
    int following,
    @JsonAlias({"public_repos", "publicRepos"}) int publicRepos,
    @JsonAlias({"public_gists", "publicGists"}) int publicGists,
    @JsonAlias({"created_at", "createdAt"}) String createdAt,
    @JsonAlias({"updated_at", "updatedAt"}) String updatedAt,
    List<GitHubProfileLink> links
) {
    public GitHubProfile {
        if (links == null) links = Collections.emptyList();
    }

    public static GitHubProfile of(
        String login,
        String name,
        String bio,
        String avatarUrl,
        String htmlUrl,
        String location,
        String email,
        String blog,
        int followers,
        int following,
        int publicRepos,
        int publicGists,
        String createdAt,
        String updatedAt,
        List<GitHubProfileLink> links
    ) {
        return new GitHubProfile(login, name, bio, avatarUrl, htmlUrl, location, email, blog,
            followers, following, publicRepos, publicGists, createdAt, updatedAt, links);
    }
}
