package ru.oldzoomer.www.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.service.GitHubDisplayService;
import ru.oldzoomer.www.service.GitHubDisplayService.GitHubDisplayData;
import ru.oldzoomer.www.service.GitHubDisplayService.SectionItem;

import java.io.Serializable;
import java.util.List;

/**
 * Базовый класс для всех представлений.
 * Содержит общий хедер, навигацию и логику отображения секций.
 */
public abstract class BaseView extends VerticalLayout implements BeforeEnterObserver, Serializable {

    private transient GitHubData gitHubData;
    private transient GitHubDisplayService displayService;

    protected BaseView() {
    }

    protected void init(GitHubData gitHubData, GitHubDisplayService displayService) {
        this.gitHubData = gitHubData;
        this.displayService = displayService;
    }

    @SuppressWarnings("this-escape")
    protected void setupUI() {
        addClassName("adwaita-window");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        applySeoMeta();
    }

    // ─── Загрузка данных ──────────────────────────────────────────────────

    protected void loadData(String loadingText, ContentBuilder contentBuilder) {
        Div loading = new Div();
        loading.setText(loadingText);
        loading.addClassName("loading");
        add(loading);

        try {
            removeAll();

            GitHubDisplayData data = displayService.toDisplayData(gitHubData);

            add(buildHeaderBar(data));
            add(buildNavigation());
            VerticalLayout content = contentBuilder.build(data);
            add(content);

        } catch (Exception e) {
            removeAll();
            Div error = new Div();
            error.setText("Ошибка: " + e.getMessage());
            error.addClassName("error");
            add(error);
        }
    }

    @FunctionalInterface
    protected interface ContentBuilder extends Serializable {
        VerticalLayout build(GitHubDisplayData data);
    }

    // ─── Хедер ─────────────────────────────────────────────────────────────

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

    // ─── Навигация ─────────────────────────────────────────────────────────

    private HorizontalLayout buildNavigation() {
        HorizontalLayout nav = new HorizontalLayout();
        nav.addClassName("adwaita-navigation");
        nav.setWidthFull();

        RouterLink mainLink = new RouterLink("Главная", MainView.class);
        RouterLink reposLink = new RouterLink("Репозитории", ReposView.class);
        RouterLink statsLink = new RouterLink("Статистика", StatsView.class);

        String navLinkClass = "nav-link";
        mainLink.addClassName(navLinkClass);
        reposLink.addClassName(navLinkClass);
        statsLink.addClassName(navLinkClass);

        nav.add(mainLink, reposLink, statsLink);
        return nav;
    }

    // ─── Секции (переиспользуемые) ─────────────────────────────────────────

    protected VerticalLayout buildScrollableContent(GitHubDisplayData data) {
        VerticalLayout container = new VerticalLayout();
        container.addClassName("adwaita-content-container");

        if (!data.contactInfo().isBlank() || !data.bio().isBlank()) {
            Div subHeader = createSubHeader(data);
            container.add(subHeader);
        }

        return container;
    }

    protected void addSection(VerticalLayout parent, String title, List<SectionItem> items) {
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

    protected Div buildSectionItem(SectionItem item) {
        Div row = new Div();
        row.addClassName("adwaita-row");

        VerticalLayout rowContent = new VerticalLayout();
        rowContent.addClassName("adwaita-row-content");
        rowContent.setPadding(false);
        rowContent.setSpacing(false);

        H3 entryTitle = new H3(item.title());
        entryTitle.addClassName("adwaita-row-title");

        if (item.url() != null && !item.url().isBlank()) {
            Anchor link = new Anchor(item.url(), item.title());
            link.addClassName("adwaita-row-title");
            link.getElement().setAttribute("target", "_blank");
            rowContent.add(link);
        } else {
            rowContent.add(entryTitle);
        }

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

    private static Div createSubHeader(GitHubDisplayData data) {
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

    // ─── SEO ───────────────────────────────────────────────────────────────

    private void applySeoMeta() {
        metaDescription("Портфолио Java-разработчика с данными из GitHub: репозитории, проекты, статистика");
        geoLocation("Russia", "Kstovo");
        ogTitle("Егор Гаврилов - Java-разработчик");
        ogType("profile");
        ogSiteName("GitHub Portfolio");
    }

    private void metaDescription(String description) {
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
            region, placename
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
