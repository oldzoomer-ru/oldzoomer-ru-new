package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public record GitHubGistFile(
    String filename,
    String language,
    @JsonProperty("content_type") String contentType,
    int size
) {
    public static GitHubGistFile of(String filename, String language, String contentType, int size) {
        return new GitHubGistFile(filename, language, contentType, size);
    }
}
