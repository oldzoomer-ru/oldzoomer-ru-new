package ru.oldzoomer.www.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public record GitHubData(
    GitHubProfile profile,
    String profileReadme,
    List<GitHubRepo> pinnedRepos,
    List<GitHubRepo> allRepos,
    List<GitHubGist> gists,
    GitHubActivity activity
) {
    public GitHubData {
        if (pinnedRepos == null) pinnedRepos = Collections.emptyList();
        if (allRepos == null) allRepos = Collections.emptyList();
        if (gists == null) gists = Collections.emptyList();
    }

    public static GitHubData of(
        GitHubProfile profile,
        String profileReadme,
        List<GitHubRepo> pinnedRepos,
        List<GitHubRepo> allRepos,
        List<GitHubGist> gists,
        GitHubActivity activity
    ) {
        return new GitHubData(profile, profileReadme, pinnedRepos, allRepos, gists, activity);
    }
}
