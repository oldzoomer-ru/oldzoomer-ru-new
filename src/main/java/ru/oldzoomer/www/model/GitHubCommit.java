package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public record GitHubCommit(
    String sha,
    @JsonProperty("commit.message") String message,
    @JsonAlias({"html_url", "htmlUrl"}) String htmlUrl,
    @JsonAlias({"author.login", "authorLogin"}) String authorLogin,
    @JsonAlias({"author.avatar_url", "authorAvatarUrl"}) String authorAvatarUrl,
    @JsonAlias({"commit.committed_date", "committedDate"}) String committedDate
) {
    public static GitHubCommit of(
        String sha,
        String message,
        String htmlUrl,
        String authorLogin,
        String authorAvatarUrl,
        String committedDate
    ) {
        return new GitHubCommit(sha, message, htmlUrl, authorLogin, authorAvatarUrl, committedDate);
    }
}
