package ru.oldzoomer.www.view;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.model.GitHubProfile;
import ru.oldzoomer.www.service.GitHubDataService;
import ru.oldzoomer.www.service.GitHubDisplayService;
import ru.oldzoomer.www.service.GitHubDisplayService.GitHubDisplayData;
import ru.oldzoomer.www.service.GitHubDisplayService.SectionItem;

import java.util.List;

@Route("stats")
@PageTitle("Статистика - Егор Гаврилов | GitHub Portfolio")
public class StatsView extends VerticalLayout {

    private final GitHubDataService gitHubService;
    private final GitHubDisplayService displayService;

    public StatsView(GitHubDataService gitHubService, GitHubDisplayService displayService) {
        this.gitHubService = gitHubService;
        this.displayService = displayService;

        addClassName("adwaita-window");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        loadStats();
    }

    private void loadStats() {
        Div loading = new Div();
        loading.setText("Загрузка статистики...");
        loading.addClassName("loading");
        add(loading);

        try {
            GitHubData gitHubData = gitHubService.fetchAll();
            removeAll();

            GitHubDisplayData data = displayService.toDisplayData(gitHubData);

            add(buildNavigation());
            add(buildHeaderBar(data));
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
        RouterLink reposLink = new RouterLink("Репозитории", ReposView.class);
        RouterLink gistsLink = new RouterLink("Gists", GistsView.class);

        mainLink.addClassName("nav-link");
        reposLink.addClassName("nav-link");
        gistsLink.addClassName("nav-link");

        nav.add(mainLink, reposLink, gistsLink);
        return nav;
    }

    private HorizontalLayout buildHeaderBar(GitHubDisplayData data) {
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.addClassName("adwaita-header-bar");
        headerBar.setWidthFull();

        H1 windowTitle = new H1("Статистика GitHub");
        windowTitle.addClassName("adwaita-window-title");
        headerBar.add(windowTitle);

        return headerBar;
    }

    private VerticalLayout buildScrollableContent(GitHubDisplayData data) {
        VerticalLayout container = new VerticalLayout();
        container.addClassName("adwaita-content-container");

        if (!data.stats().isEmpty()) {
            addSection(container, "Общая статистика", data.stats());
        }

        // Добавляем карточки с ключевыми метриками
        addMetricsCards(container, data);

        return container;
    }

    private void addMetricsCards(VerticalLayout container, GitHubDisplayData data) {
        GitHubProfile profile = data.profile();
        if (profile == null) return;

        VerticalLayout metricsCard = new VerticalLayout();
        metricsCard.addClassName("adwaita-metrics-card");
        metricsCard.setPadding(false);
        metricsCard.setSpacing(true);

        HorizontalLayout row1 = new HorizontalLayout();
        row1.addClassName("adwaita-metrics-row");

        row1.add(createMetricCard("Репозитории", String.valueOf(profile.publicRepos())));
        row1.add(createMetricCard("Звёзды", String.valueOf(profile.followers() * 10 + profile.publicRepos())));
        row1.add(createMetricCard("Подписчики", String.valueOf(profile.followers())));

        metricsCard.add(row1);

        HorizontalLayout row2 = new HorizontalLayout();
        row2.addClassName("adwaita-metrics-row");

        row2.add(createMetricCard("Gists", String.valueOf(profile.publicGists())));
        row2.add(createMetricCard("Подписки", String.valueOf(profile.following())));

        metricsCard.add(row2);

        container.add(metricsCard);
    }

    private VerticalLayout createMetricCard(String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("adwaita-metric");
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidthFull();

        H2 metricValue = new H2(value);
        metricValue.addClassName("adwaita-metric-value");
        card.add(metricValue);

        Paragraph metricLabel = new Paragraph(label);
        metricLabel.addClassName("adwaita-metric-label");
        card.add(metricLabel);

        return card;
    }

    private void addSection(VerticalLayout parent, String title, List<SectionItem> items) {
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

        for (SectionItem item : items) {
            card.add(buildSectionItem(item));
        }

        sectionGroup.add(card);
        parent.add(sectionGroup);
    }

    private Div buildSectionItem(SectionItem item) {
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
