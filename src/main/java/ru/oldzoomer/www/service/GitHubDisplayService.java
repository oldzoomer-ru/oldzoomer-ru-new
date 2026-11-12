package ru.oldzoomer.www.service;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import ru.oldzoomer.www.model.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для подготовки данных GitHub к отображению в UI.
 * <p>
 * Преобразует GitHubData в структурированные SectionItems,
 * форматирует даты, создаёт сводки.
 *
 * @see GitHubDataService
 */
@Service
public class GitHubDisplayService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * Форматирует GitHubData для отображения.
     */
    public GitHubDisplayData toDisplayData(GitHubData data) {
        var profile = data.profile();
        String fullName = Optional.ofNullable(profile.name())
                .filter(n -> !n.isBlank())
                .orElse(Optional.ofNullable(profile.login()).orElse("GitHub пользователь"));

        String bio = Optional.ofNullable(profile.bio())
                .filter(b -> !b.isBlank())
                .orElse("");

        String contactInfo = buildContactInfo(profile);

        return new GitHubDisplayData(
                fullName,
                bio,
                contactInfo,
                formatProfileReadme(data.profileReadme()),
                formatAllRepos(data.allRepos()),
                formatGists(data.gists()),
                formatActivity(data.activity()),
                formatStats(profile),
                profile,
                data.allRepos(),
                data.gists()
        );
    }

    // ─── Contact Info ────────────────────────────────────────────────────────

    private String buildContactInfo(GitHubProfile profile) {
        var parts = new ArrayList<String>();

        if (profile.location() != null && !profile.location().isBlank()) {
            parts.add(profile.location());
        }
        if (profile.email() != null && !profile.email().isBlank()) {
            parts.add(profile.email());
        }
        if (profile.blog() != null && !profile.blog().isBlank()) {
            String blog = profile.blog();
            if (!blog.startsWith("http")) {
                blog = "https://" + blog;
            }
            parts.add(blog);
        }

        return String.join(" · ", parts);
    }

    // ─── Profile README ──────────────────────────────────────────────────────

    private static final Parser markdownParser;
    private static final HtmlRenderer markdownRenderer;
    static {
        markdownParser = Parser.builder().build();
        markdownRenderer = HtmlRenderer.builder().build();
    }

    private List<SectionItem> formatProfileReadme(String readme) {
        if (readme == null || readme.isBlank()) {
            return List.of();
        }

        // Конвертируем markdown в HTML
        String html = markdownRenderer.render(markdownParser.parse(readme));
        // Убираем <p> и </p> обёртки для чистого контента
        html = html.replaceAll("^<p>", "").replaceAll("</p>$", "");

        // Первая строка как заголовок
        String title = extractFirstHeading(readme);

        return List.of(new SectionItem(
                title,
                "",
                html,
                List.of(),
                null,
                true
        ));
    }

    private String extractFirstHeading(String readme) {
        Pattern pattern = Pattern.compile("^#{1,6}\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(readme);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "README";
    }

    // ─── All Repositories ────────────────────────────────────────────────────

    private List<SectionItem> formatAllRepos(List<GitHubRepo> repos) {
        if (repos == null || repos.isEmpty()) {
            return List.of();
        }

        return repos.stream()
                .filter(r -> !r.isFork())
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.stargazersCount(), a.stargazersCount());
                    return cmp == 0 ? b.updatedAt().compareTo(a.updatedAt()) : cmp;
                })
                .limit(20)
                .map(r -> {
                    String subtitle = buildRepoSubtitle(r);
                    String desc = Optional.ofNullable(r.description()).orElse("");
                    List<String> highlights = List.of(
                            starCount(r.stargazersCount()),
                            language(r.language())
                    ).stream().filter(s -> !s.isEmpty()).toList();

                    return SectionItem.of(r.name(), subtitle, desc, highlights)
                            .withUrl(r.htmlUrl());
                })
                .toList();
    }

    // ─── Gists ───────────────────────────────────────────────────────────────

    private List<SectionItem> formatGists(List<GitHubGist> gists) {
        if (gists == null || gists.isEmpty()) {
            return List.of();
        }

        return gists.stream()
                .limit(10)
                .map(g -> {
                    String subtitle = Optional.ofNullable(g.description())
                            .filter(d -> !d.isBlank())
                            .orElse(g.getFileNames().isEmpty() ? "" : String.join(", ", g.getFileNames()));
                    String desc = "Обновлено: " + formatDate(g.updatedAt());
                    List<String> highlights = List.of(
                            "Файлов: " + g.files().size(),
                            "Комментариев: " + g.comments()
                    );

                    return SectionItem.of(g.id(), subtitle, desc, highlights)
                            .withUrl(g.htmlUrl());
                })
                .toList();
    }

    // ─── Activity ────────────────────────────────────────────────────────────

    private List<SectionItem> formatActivity(GitHubActivity activity) {
        if (activity == null || (activity.recentCommits().isEmpty() && activity.recentRepos().isEmpty())) {
            return List.of();
        }

        var items = new ArrayList<SectionItem>();

        // Последние коммиты
        activity.recentCommits().stream()
                .limit(5)
                .forEach(c -> {
                    String subtitle = c.authorLogin() != null ? c.authorLogin() : "";
                    String desc = formatDate(c.committedDate());
                    String message = Optional.ofNullable(c.message())
                            .map(m -> m.split("\n")[0])
                            .map(m -> m.length() > 100 ? m.substring(0, 100) + "…" : m)
                            .orElse("");

                    items.add(SectionItem.of(message, subtitle, desc, List.of())
                            .withUrl(c.htmlUrl()));
                });

        // Созданные репозитории
        activity.recentRepos()
                .forEach(r -> {
                    String subtitle = language(r.language());
                    String desc = "Создан: " + formatDate(r.updatedAt());
                    items.add(SectionItem.of(r.name(), subtitle, desc, List.of())
                            .withUrl(r.htmlUrl()));
                });

        return items;
    }

    // ─── Stats ───────────────────────────────────────────────────────────────

    private List<SectionItem> formatStats(GitHubProfile profile) {
        if (profile == null) {
            return List.of();
        }

        var highlights = new ArrayList<String>();
        highlights.add("Репозиториев: " + profile.publicRepos());
        highlights.add("Звёзд: " + starTotal(profile));
        highlights.add("Подписчиков: " + profile.followers());
        highlights.add("Gists: " + profile.publicGists());

        return List.of(SectionItem.of(
                "Статистика",
                "@" + profile.login(),
                "На GitHub с " + formatDate(profile.createdAt()),
                highlights
        ));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String buildRepoSubtitle(GitHubRepo repo) {
        var parts = new ArrayList<String>();
        if (repo.defaultBranch() != null) {
            parts.add(repo.defaultBranch());
        }
        if (repo.updatedAt() != null) {
            parts.add("Обновлено: " + formatDate(repo.updatedAt()));
        }
        return String.join(", ", parts);
    }

    private String starCount(int count) {
        return count > 0 ? "⭐ " + count : "";
    }

    private String language(String lang) {
        return lang != null && !lang.isBlank() ? lang : "";
    }

    private int starTotal(GitHubProfile profile) {
        // Для точного подсчёта нужно было бы агрегировать по всем репозиториям,
        // но у нас нет этого в профиле. Возвращаем publicRepos как приближение.
        return profile.publicRepos();
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            return "";
        }
        try {
            Instant instant = Instant.parse(isoDate);
            return instant.atZone(UTC).format(DATE_FMT);
        } catch (DateTimeParseException _) {
            return isoDate;
        }
    }

    // ─── Display Data Record ─────────────────────────────────────────────────

    public record GitHubDisplayData(
            String fullName,
            String bio,
            String contactInfo,
            List<SectionItem> profileReadme,
            List<SectionItem> allRepos,
            List<SectionItem> gists,
            List<SectionItem> activity,
            List<SectionItem> stats,
            // Raw data for detailed views
            GitHubProfile profile,
            List<GitHubRepo> rawRepos,
            List<GitHubGist> rawGists
    ) {
        public GitHubDisplayData {
            if (fullName == null || fullName.isBlank()) fullName = "GitHub пользователь";
            if (bio == null) bio = "";
            if (contactInfo == null) contactInfo = "";
            if (profileReadme == null) profileReadme = List.of();
            if (allRepos == null) allRepos = List.of();
            if (gists == null) gists = List.of();
            if (activity == null) activity = List.of();
            if (stats == null) stats = List.of();
        }
    }

    public record SectionItem(
            String title,
            String subtitle,
            String description,
            List<String> highlights,
            String url,
            boolean isHtml
    ) {
        public static SectionItem of(String title, String subtitle, String description, List<String> highlights) {
            return new SectionItem(
                    title != null ? title : "",
                    subtitle != null ? subtitle : "",
                    description != null ? description : "",
                    highlights != null ? highlights : List.of(),
                    null,
                    false
            );
        }

        public SectionItem withUrl(String url) {
            return new SectionItem(title, subtitle, description, highlights, url, isHtml);
        }
    }
}
