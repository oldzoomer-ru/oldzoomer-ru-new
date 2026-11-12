package ru.oldzoomer.www.view;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.jspecify.annotations.NonNull;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.service.GitHubDisplayService;
import ru.oldzoomer.www.service.GitHubDisplayService.GitHubDisplayData;
import ru.oldzoomer.www.service.GitHubDisplayService.SectionItem;

import java.util.List;

@Route("")
@PageTitle("Егор Гаврилов - Java-разработчик | GitHub Portfolio")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private final GitHubData gitHubData;
    private final GitHubDisplayService displayService;

    public MainView(GitHubData gitHubData, GitHubDisplayService displayService) {
        this.gitHubData = gitHubData;
        this.displayService = displayService;

        addClassName("adwaita-window");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        loadGitHubData();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // SEO: Устанавливаем мета-данные при входе на страницу
        applySeoMeta();
    }

    private void loadGitHubData() {
        Div loading = new Div();
        loading.setText("Загрузка данных GitHub...");
        loading.addClassName("loading");
        add(loading);

        try {
            removeAll();

            GitHubDisplayData data = displayService.toDisplayData(gitHubData);

            add(buildHeaderBar(data));
            add(buildNavigation());
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

        RouterLink statsLink = new RouterLink("Статистика", StatsView.class);
        RouterLink reposLink = new RouterLink("Репозитории", ReposView.class);
        RouterLink gistsLink = new RouterLink("Gists", GistsView.class);

        statsLink.addClassName("nav-link");
        reposLink.addClassName("nav-link");
        gistsLink.addClassName("nav-link");

        nav.add(statsLink, reposLink, gistsLink);
        return nav;
    }

    private HorizontalLayout buildHeaderBar(GitHubDisplayData data) {
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.addClassName("adwaita-header-bar");
        headerBar.setWidthFull();

        H1 windowTitle = new H1(data.fullName());
        windowTitle.addClassName("adwaita-window-title");
        headerBar.add(windowTitle);

        Div windowControls = new Div();
        windowControls.addClassName("adwaita-window-controls");
        headerBar.add(windowControls);

        return headerBar;
    }

    private VerticalLayout buildScrollableContent(GitHubDisplayData data) {
        VerticalLayout container = new VerticalLayout();
        container.addClassName("adwaita-content-container");

        if (!data.contactInfo().isBlank() || !data.bio().isBlank()) {
            Div subHeader = createSubHeader(data);
            container.add(subHeader);
        }

        buildSections(container, data);

        return container;
    }

    private static @NonNull Div createSubHeader(GitHubDisplayData data) {
        Div subHeader = new Div();
        subHeader.addClassName("adwaita-sub-header");

        if (!data.contactInfo().isBlank()) {
            Paragraph contacts = new Paragraph(data.contactInfo());
            contacts.addClassName("adwaita-contacts");
            subHeader.add(contacts);
        }
        if (!data.bio().isBlank()) {
            Paragraph bio = new Paragraph(data.bio());
            bio.addClassName("adwaita-bio");
            subHeader.add(bio);
        }
        return subHeader;
    }

    private void buildSections(VerticalLayout container, GitHubDisplayData data) {
        addSection(container, "О себе", data.profileReadme());
        addSection(container, "Репозитории", data.allRepos());
        addSection(container, "Активность", data.activity());
        addSection(container, "Статистика", data.stats());
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
            if (item.isHtml()) {
                Div desc = new Div();
                desc.addClassName("adwaita-row-description");
                desc.getElement().setProperty("innerHTML", item.description());
                rowContent.add(desc);
            } else {
                Paragraph desc = new Paragraph(item.description());
                desc.addClassName("adwaita-row-description");
                rowContent.add(desc);
            }
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

    /**
     * SEO: Применяет мета-данные для поисковых систем.
     */
    private void applySeoMeta() {
        // Meta description для поисковиков
        metaDescription("Портфолио Java-разработчика с данными из GitHub: репозитории, проекты, статистика");
        // Geo-метаданные
        geoLocation("Russia", "Kstovo");
        // Open Graph для соцсетей
        ogTitle("Егор Гаврилов - Java-разработчик");
        ogType("profile");
        ogSiteName("GitHub Portfolio");
    }

    private void metaDescription(String description) {
        // Добавляем meta tag через Vaadin
        getElement().executeJs(
            "var meta = document.createElement('meta');" +
            "meta.name = 'description';" +
            "meta.content = $0;" +
            "document.head.appendChild(meta);",
            description
        );
    }

    private void geoLocation(String region, String placename) {
        getElement().executeJs(
            "var meta1 = document.createElement('meta');" +
            "meta1.name = 'geo.region';" +
            "meta1.content = $0;" +
            "document.head.appendChild(meta1);" +
            "var meta2 = document.createElement('meta');" +
            "meta2.name = 'geo.placename';" +
            "meta2.content = $1;" +
            "document.head.appendChild(meta2);",
            region,
            placename
        );
    }

    private void ogTitle(String title) {
        getElement().executeJs(
            "var meta = document.createElement('meta');" +
            "meta.property = 'og:title';" +
            "meta.content = $0;" +
            "document.head.appendChild(meta);",
            title
        );
    }

    private void ogType(String type) {
        getElement().executeJs(
            "var meta = document.createElement('meta');" +
            "meta.property = 'og:type';" +
            "meta.content = $0;" +
            "document.head.appendChild(meta);",
            type
        );
    }

    private void ogSiteName(String name) {
        getElement().executeJs(
            "var meta = document.createElement('meta');" +
            "meta.property = 'og:site_name';" +
            "meta.content = $0;" +
            "document.head.appendChild(meta);",
            name
        );
    }
}
