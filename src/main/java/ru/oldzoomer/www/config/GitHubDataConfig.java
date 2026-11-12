package ru.oldzoomer.www.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import ru.oldzoomer.www.model.GitHubActivity;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.model.GitHubGist;
import ru.oldzoomer.www.model.GitHubProfile;
import ru.oldzoomer.www.model.GitHubRepo;
import ru.oldzoomer.www.service.GitHubDataService;

import java.util.List;

@Configuration 
public class GitHubDataConfig {
     /**
     * Получает все данные GitHub для текущего пользователя.
     * Использует Spring Cache для уменьшения количества запросов к API.
     */
    @Bean @VaadinSessionScope GitHubData fetchAll(GitHubDataService gitHubDataService) {
        GitHubProfile profile = gitHubDataService.fetchProfile();
        String profileReadme = gitHubDataService.fetchProfileReadme();
        List<GitHubRepo> allRepos = gitHubDataService.fetchAllRepos();
        List<GitHubGist> gists = gitHubDataService.fetchGists();
        GitHubActivity activity = gitHubDataService.fetchActivity();

        return new GitHubData(profile, profileReadme, allRepos, gists, activity);
    }
}
