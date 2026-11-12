package ru.oldzoomer.www.view;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.model.GitHubRepo;
import ru.oldzoomer.www.service.GitHubDataService;
import ru.oldzoomer.www.service.GitHubDisplayService;
import ru.oldzoomer.www.service.GitHubDisplayService.GitHubDisplayData;

import java.util.List;
import java.util.stream.Collectors;

@Route("repos")
@PageTitle("Репозитории - Егор Гаврилов | GitHub Portfolio")
public class ReposView extends VerticalLayout {

    private final GitHubDataService gitHubService;
    private final GitHubDisplayService displayService;

    public ReposView(GitHubDataService gitHubService, GitHubDisplayService displayService) {
        this.gitHubService = gitHubService;
        this.displayService = displayService;

        addClassName("adwaita-window");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        loadRepos();
    }

    private void loadRepos() {
        Div loading = new Div();
        loading.setText("Загрузка репозиториев...");
        loading.addClassName("loading");
        add(loading);

        try {
            GitHubData gitHubData = gitHubService.fetchAll();
            removeAll();

            GitHubDisplayData data = displayService.toDisplayData(gitHubData);

            add(buildNavigation());
            add(buildHeaderBar());
            add(buildScrollableContent(data));

        } catch (Exception e) {
            removeAll();
            Div error = new Div();
            error.setText("Ошибка: " + e.getMessage());
            error.addClassName("error");
            add(error);
        }
    }

    private HorizontalLayout buildNavigation() {
        HorizontalLayout nav = new HorizontalLayout();
        nav.addClassName("adwaita-navigation");
        nav.setWidthFull();

        RouterLink mainLink = new RouterLink("Главная", MainView.class);
        RouterLink statsLink = new RouterLink("Статистика", StatsView.class);
        RouterLink gistsLink = new RouterLink("Gists", GistsView.class);

        mainLink.addClassName("nav-link");
        statsLink.addClassName("nav-link");
        gistsLink.addClassName("nav-link");

        nav.add(mainLink, statsLink, gistsLink);
        return nav;
    }

    private HorizontalLayout buildHeaderBar() {
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.addClassName("adwaita-header-bar");
        headerBar.setWidthFull();

        H1 windowTitle = new H1("Репозитории");
        windowTitle.addClassName("adwaita-window-title");
        headerBar.add(windowTitle);

        return headerBar;
    }

    private VerticalLayout buildScrollableContent(GitHubDisplayData data) {
        VerticalLayout container = new VerticalLayout();
        container.addClassName("adwaita-content-container");

        // Все репозитории
        if (!data.allRepos().isEmpty()) {
            addSection(container, "Все репозитории", data.allRepos());
        }

        // Карточки с информацией о репозиториях
        addRepoCards(container, data);

        return container;
    }

    private void addRepoCards(VerticalLayout container, GitHubDisplayData data) {
        List<GitHubRepo> repos = data.rawRepos();
        if (repos == null || repos.isEmpty()) {
            return;
        }

        // Группируем по языкам программирования
        var reposByLanguage = repos.stream()
                .filter(r -> r.language() != null && !r.language().isBlank())
                .collect(Collectors.groupingBy(GitHubRepo::language));

        for (var entry : reposByLanguage.entrySet()) {
            String language = entry.getKey();
            List<GitHubRepo> languageRepos = entry.getValue();

            VerticalLayout langCard = new VerticalLayout();
            langCard.addClassName("adwaita-lang-card");
            langCard.setPadding(true);
            langCard.setSpacing(true);

            H3 langTitle = new H3(language + " (" + languageRepos.size() + ")");
            langTitle.addClassName("adwaita-lang-title");
            langCard.add(langTitle);

            // Показываем топ-3 репозитория по языку
            languageRepos.stream()
                    .sorted((a, b) -> Integer.compare(b.stargazersCount(), a.stargazersCount()))
                    .limit(3)
                    .forEach(repo -> {
                        HorizontalLayout repoRow = new HorizontalLayout();
                        repoRow.addClassName("adwaita-repo-row");

                        Anchor repoLink = new Anchor(repo.htmlUrl(), repo.name());
                        repoLink.addClassName("adwaita-repo-link");
                        repoRow.add(repoLink);

                        Div stars = new Div();
                        stars.addClassName("adwaita-repo-stars");
                        stars.setText("⭐ " + repo.stargazersCount());
                        repoRow.add(stars);

                        langCard.add(repoRow);
                    });

            container.add(langCard);
        }
    }

    private void addSection(VerticalLayout parent, String title, List<GitHubDisplayService.SectionItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        VerticalLayout sectionGroup = new VerticalLayout();
        sectionGroup.addClassName("adwaita-pref-group");
        sectionGroup.setPadding(false);
        sectionGroup.setSpacing(false);

        H2 sectionTitle = new H2(title);
        sectionTitle.addClassName("adwaita-pref-group-title");
        sectionGroup.add(sectionTitle);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("adwaita-card");
        card.setPadding(false);
        card.setSpacing(false);

        for (GitHubDisplayService.SectionItem item : items) {
            card.add(buildSectionItem(item));
        }

        sectionGroup.add(card);
        parent.add(sectionGroup);
    }

    private Div buildSectionItem(GitHubDisplayService.SectionItem item) {
        Div row = new Div();
        row.addClassName("adwaita-row");

        VerticalLayout rowContent = new VerticalLayout();
        rowContent.addClassName("adwaita-row-content");
        rowContent.setPadding(false);
        rowContent.setSpacing(false);

        H3 entryTitle = new H3(item.title());
        entryTitle.addClassName("adwaita-row-title");
        rowContent.add(entryTitle);

        if (!item.subtitle().isBlank()) {
            Paragraph subtitle = new Paragraph(item.subtitle());
            subtitle.addClassName("adwaita-row-subtitle");
            rowContent.add(subtitle);
        }

        if (!item.description().isBlank()) {
            Paragraph desc = new Paragraph(item.description());
            desc.addClassName("adwaita-row-description");
            rowContent.add(desc);
        }

        if (item.highlights() != null && !item.highlights().isEmpty()) {
            UnorderedList list = new UnorderedList();
            list.addClassName("adwaita-row-highlights");
            for (String h : item.highlights()) {
                list.add(new ListItem(h));
            }
            rowContent.add(list);
        }

        row.add(rowContent);
        return row;
    }
}
