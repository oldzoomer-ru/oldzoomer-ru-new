package ru.oldzoomer.www.view;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.model.GitHubGist;
import ru.oldzoomer.www.service.GitHubDataService;
import ru.oldzoomer.www.service.GitHubDisplayService;
import ru.oldzoomer.www.service.GitHubDisplayService.GitHubDisplayData;

import java.util.List;

@Route("gists")
@PageTitle("Gists - Егор Гаврилов | GitHub Portfolio")
public class GistsView extends VerticalLayout {

    private final GitHubDataService gitHubService;
    private final GitHubDisplayService displayService;

    public GistsView(GitHubDataService gitHubService, GitHubDisplayService displayService) {
        this.gitHubService = gitHubService;
        this.displayService = displayService;

        addClassName("adwaita-window");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        loadGists();
    }

    private void loadGists() {
        Div loading = new Div();
        loading.setText("Загрузка gists...");
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
        RouterLink reposLink = new RouterLink("Репозитории", ReposView.class);

        mainLink.addClassName("nav-link");
        statsLink.addClassName("nav-link");
        reposLink.addClassName("nav-link");

        nav.add(mainLink, statsLink, reposLink);
        return nav;
    }

    private HorizontalLayout buildHeaderBar() {
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.addClassName("adwaita-header-bar");
        headerBar.setWidthFull();

        H1 windowTitle = new H1("Gists");
        windowTitle.addClassName("adwaita-window-title");
        headerBar.add(windowTitle);

        return headerBar;
    }

    private VerticalLayout buildScrollableContent(GitHubDisplayData data) {
        VerticalLayout container = new VerticalLayout();
        container.addClassName("adwaita-content-container");

        if (!data.gists().isEmpty()) {
            addSection(container, "Все Gists", data.gists());
        }

        // Карточки с gists
        addGistCards(container, data);

        return container;
    }

    private void addGistCards(VerticalLayout container, GitHubDisplayData data) {
        List<GitHubGist> gists = data.rawGists();
        if (gists == null || gists.isEmpty()) {
            return;
        }

        for (GitHubGist gist : gists) {
            VerticalLayout gistCard = new VerticalLayout();
            gistCard.addClassName("adwaita-gist-card");
            gistCard.setPadding(true);
            gistCard.setSpacing(true);

            Anchor gistLink = new Anchor(gist.htmlUrl(), "Gist: " + gist.id());
            gistLink.addClassName("adwaita-gist-link");
            gistCard.add(gistLink);

            if (gist.description() != null && !gist.description().isBlank()) {
                Paragraph desc = new Paragraph(gist.description());
                desc.addClassName("adwaita-gist-description");
                gistCard.add(desc);
            }

            HorizontalLayout meta = new HorizontalLayout();
            meta.addClassName("adwaita-gist-meta");

            Div files = new Div();
            files.addClassName("adwaita-gist-meta-item");
            files.setText("Файлов: " + gist.files().size());
            meta.add(files);

            Div comments = new Div();
            comments.addClassName("adwaita-gist-meta-item");
            comments.setText("Комментариев: " + gist.comments());
            meta.add(comments);

            gistCard.add(meta);

            // Список файлов
            if (!gist.getFileNames().isEmpty()) {
                UnorderedList fileList = new UnorderedList();
                fileList.addClassName("adwaita-gist-files");
                for (String fileName : gist.getFileNames()) {
                    fileList.add(new ListItem(fileName));
                }
                gistCard.add(fileList);
            }

            container.add(gistCard);
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
