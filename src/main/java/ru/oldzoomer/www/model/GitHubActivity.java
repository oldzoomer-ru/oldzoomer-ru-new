package ru.oldzoomer.www.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public record GitHubActivity(
    List<GitHubCommit> recentCommits,
    List<GitHubRepo> recentRepos
) {
    public GitHubActivity {
        if (recentCommits == null) recentCommits = Collections.emptyList();
        if (recentRepos == null) recentRepos = Collections.emptyList();
    }

    public static GitHubActivity of(List<GitHubCommit> recentCommits, List<GitHubRepo> recentRepos) {
        return new GitHubActivity(recentCommits, recentRepos);
    }
}
