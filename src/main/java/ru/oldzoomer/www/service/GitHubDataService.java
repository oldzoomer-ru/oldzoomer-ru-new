package ru.oldzoomer.www.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import ru.oldzoomer.www.model.GitHubActivity;
import ru.oldzoomer.www.model.GitHubCommit;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.model.GitHubGist;
import ru.oldzoomer.www.model.GitHubProfile;
import ru.oldzoomer.www.model.GitHubRepo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Сервис для загрузки данных из GitHub API с кэшированием через Spring Cache.
 * <p>
 * Загружает профиль пользователя, README профиля, pinned-репозитории,
 * статистику, активность и gists. Результаты кэшируются через Spring Cache.
 *
 * @see <a href="https://docs.github.com/en/rest">GitHub REST API</a>
 */
@Service
public class GitHubDataService {

    private static final Logger log = LoggerFactory.getLogger(GitHubDataService.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String USERS_PATH = "/users/";
    private static final String REPOS_PATH = "/repos/";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String githubUsername;
    private final String githubToken;

    public GitHubDataService(
            @Value("${github.username}") String githubUsername,
            @Value("${github.token:}") String githubToken,
            ObjectMapper objectMapper) {
        this.githubUsername = githubUsername;
        this.githubToken = githubToken.isBlank() ? null : githubToken;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Получает все данные GitHub для текущего пользователя.
     * Использует Spring Cache для уменьшения количества запросов к API.
     */
    public GitHubData fetchAll() {
        GitHubProfile profile = fetchProfile();
        String profileReadme = fetchProfileReadme();
        List<GitHubRepo> pinnedRepos = fetchPinnedRepos();
        List<GitHubRepo> allRepos = fetchAllRepos();
        List<GitHubGist> gists = fetchGists();
        GitHubActivity activity = fetchActivity();

        return new GitHubData(profile, profileReadme, pinnedRepos, allRepos, gists, activity);
    }

    // ─── Profile ───────────────────────────────────────────────────────────────

    @Cacheable("profile")
    public GitHubProfile fetchProfile() {
        String url = GITHUB_API_BASE + USERS_PATH + githubUsername;
        log.info("Загрузка профиля GitHub: {}", url);

        String json = get(url);
        return objectMapper.readValue(json, GitHubProfile.class);
    }

    // ─── Profile README ────────────────────────────────────────────────────────

    @Cacheable("readme")
    public String fetchProfileReadme() {
        String url = GITHUB_API_BASE + REPOS_PATH + githubUsername + "/" + githubUsername + "/readme";
        log.info("Загрузка README профиля: {}", url);

        String json = get(url);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        if (map == null) {
            return "";
        }
        String content = (String) map.get("content");
        String encoding = (String) map.get("encoding");

        if (content != null && "base64".equals(encoding)) {
            return new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
        }
        return content != null ? content : "";
    }

    // ─── Pinned Repositories ───────────────────────────────────────────────────

    @Cacheable("pinned")
    public List<GitHubRepo> fetchPinnedRepos() {
        // GitHub не предоставляет pinned repos через публичный API.
        // Используем starred repos как fallback.
        log.info("Pinned repos недоступны через API, используем starred repos для {}", githubUsername);
        return fetchAllRepos().stream()
                .filter(r -> !r.isFork())
                .sorted((a, b) -> Integer.compare(b.stargazersCount(), a.stargazersCount()))
                .limit(6)
                .toList();
    }

    // ─── All Repositories ──────────────────────────────────────────────────────

    @Cacheable("repos")
    public List<GitHubRepo> fetchAllRepos() {
        String url = GITHUB_API_BASE + USERS_PATH + githubUsername + "/repos?per_page=100&type=owner&sort=updated";
        log.info("Загрузка репозиториев: {}", url);

        String json = get(url);
        return List.of(objectMapper.readValue(json, GitHubRepo[].class));
    }

    // ─── Gists ─────────────────────────────────────────────────────────────────

    @Cacheable("gists")
    public List<GitHubGist> fetchGists() {
        String url = GITHUB_API_BASE + USERS_PATH + githubUsername + "/gists?per_page=100";
        log.info("Загрузка gists: {}", url);

        String json = get(url);
        return List.of(objectMapper.readValue(json, GitHubGist[].class));
    }

    // ─── Activity ──────────────────────────────────────────────────────────────

    @Cacheable("activity")
    public GitHubActivity fetchActivity() {
        String url = GITHUB_API_BASE + USERS_PATH + githubUsername + "/events?per_page=100";
        log.info("Загрузка активности: {}", url);

        String json = get(url);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = List.of(objectMapper.readValue(json, Map[].class));

        List<GitHubCommit> commits = events.stream()
                .filter(e -> "PushEvent".equals(e.get("type")))
                .map(this::extractCommit)
                .limit(10)
                .toList();

        List<GitHubRepo> recentRepos = events.stream()
                .filter(e -> "CreateEvent".equals(e.get("type")))
                .map(e -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> repo = (Map<String, Object>) e.get("repo");
                    if (repo == null)
                        return null;
                    String jsonRepo = objectMapper.writeValueAsString(repo);
                    return objectMapper.readValue(jsonRepo, GitHubRepo.class);
                })
                .filter(r -> r != null)
                .limit(5)
                .toList();

        return new GitHubActivity(commits, recentRepos);
    }

    private GitHubCommit extractCommit(Map<String, Object> event) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        if (payload == null)
            return null;

        @SuppressWarnings("unchecked")
        List<String> commitIds = (List<String>) payload.get("commit_ids");
        if (commitIds == null || commitIds.isEmpty())
            return null;

        String sha = commitIds.get(0);
        String commitUrl = GITHUB_API_BASE + REPOS_PATH + event.get("repo") + "/commits/" + sha;

        String json = get(commitUrl);
        @SuppressWarnings("unchecked")
        Map<String, Object> commitMap = objectMapper.readValue(json, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> commitObj = (Map<String, Object>) commitMap.get("commit");
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) commitMap.get("author");

        String message = commitObj != null ? (String) commitObj.get("message") : "";
        String authorLogin = author != null ? (String) author.get("login") : "";
        String avatarUrl = author != null ? (String) author.get("avatar_url") : "";
        String committedDate = commitObj != null ? (String) commitObj.get("committed_date") : "";

        return new GitHubCommit(
                sha, message, (String) commitMap.get("html_url"),
                authorLogin, avatarUrl, committedDate);
    }

    // ─── HTTP Helpers ──────────────────────────────────────────────────────────

    private String get(String url) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET();

            if (githubToken != null) {
                builder.header("Authorization", "Bearer " + githubToken);
                builder.header("Accept", "application/vnd.github+json");
                builder.header("X-GitHub-Api-Version", "2022-11-28");
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 403) {
                log.warn("Лимит запросов GitHub API исчерпан. Проверьте токен.");
            }
            if (response.statusCode() != 200) {
                throw new RuntimeException("GitHub API вернул HTTP " + response.statusCode());
            }

            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запрос прерван", e);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сети: " + e.getMessage(), e);
        }
    }

    /**
     * Очищает кэш для указанного ключа.
     */
    @CacheEvict(value = {"profile", "readme", "pinned", "repos", "gists", "activity"}, allEntries = true)
    public void clearCache() {
        log.info("Кэш полностью очищен");
    }

    /**
     * Очищает кэш для указанного ключа.
     */
    @CacheEvict(value = "#key", key = "#key")
    public void invalidateCache(String key) {
        log.info("Кэш для '{}' очищен", key);
    }
}
