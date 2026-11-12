package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
public record GitHubGist(
    String id,
    String description,
    @JsonAlias({"html_url", "htmlUrl"}) String htmlUrl,
    @JsonAlias({"created_at", "createdAt"}) String createdAt,
    @JsonAlias({"updated_at", "updatedAt"}) String updatedAt,
    int comments,
    Map<String, GitHubGistFile> files
) {
    public GitHubGist {
        if (files == null) files = Collections.emptyMap();
    }

    public List<String> getFileNames() {
        return files != null ? List.copyOf(files.keySet()) : Collections.emptyList();
    }

    public static GitHubGist of(
        String id,
        String description,
        String htmlUrl,
        String createdAt,
        String updatedAt,
        int comments,
        Map<String, GitHubGistFile> files
    ) {
        return new GitHubGist(id, description, htmlUrl, createdAt, updatedAt, comments, files);
    }
}
