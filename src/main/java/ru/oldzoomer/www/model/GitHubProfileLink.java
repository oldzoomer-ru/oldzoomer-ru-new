package ru.oldzoomer.www.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public record GitHubProfileLink(
    String url,
    String title
) {
    public static GitHubProfileLink of(String url, String title) {
        return new GitHubProfileLink(url, title);
    }
}
