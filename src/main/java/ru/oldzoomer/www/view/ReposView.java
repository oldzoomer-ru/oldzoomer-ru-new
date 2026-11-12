package ru.oldzoomer.www.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.service.GitHubDisplayService;

import java.util.ArrayList;
import java.util.List;

@Route("repos")
@PageTitle("Репозитории - Егор Гаврилов")
@SuppressWarnings("this-escape")
public class ReposView extends BaseView {

    private static final String FILTER_ALL = "all";
    private static final String FILTER_REPOS = "repos";
    private static final String FILTER_GISTS = "gists";
    private static final String BTN_CLASS = "filter-btn";
    private static final String BTN_ACTIVE = "active";
    private static final String SECTION_REPOS_TITLE = "Репозитории";
    private static final String SECTION_GISTS_TITLE = "Gists";

    public ReposView(GitHubData gitHubData, GitHubDisplayService displayService) {
        init(gitHubData, displayService);
        setupUI();
        loadRepos();
    }

    private void loadRepos() {
        loadData("Загрузка репозиториев...", data -> {
            VerticalLayout container = buildScrollableContent(data);

            HorizontalLayout filterPanel = buildFilterPanel();
            container.add(filterPanel);

            addSection(container, SECTION_REPOS_TITLE, data.allRepos());
            addSection(container, SECTION_GISTS_TITLE, data.gists());

            return container;
        });
    }

    private HorizontalLayout buildFilterPanel() {
        HorizontalLayout panel = new HorizontalLayout();
        panel.addClassName("adwaita-filter-panel");
        panel.setPadding(false);
        panel.setSpacing(false);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Поиск по названию или описанию...");
        searchField.addClassName("adwaita-search-field");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterContent(e.getValue()));

        Div allBtn = createFilterButton(FILTER_ALL, "Все");
        Div reposBtn = createFilterButton(FILTER_REPOS, SECTION_REPOS_TITLE);
        Div gistsBtn = createFilterButton(FILTER_GISTS, SECTION_GISTS_TITLE);

        allBtn.addClassName(BTN_CLASS + " " + BTN_ACTIVE);
        reposBtn.addClassName(BTN_CLASS);
        gistsBtn.addClassName(BTN_CLASS);

        addClickHandler(allBtn, FILTER_ALL, allBtn, reposBtn, gistsBtn);
        addClickHandler(reposBtn, FILTER_REPOS, allBtn, reposBtn, gistsBtn);
        addClickHandler(gistsBtn, FILTER_GISTS, allBtn, reposBtn, gistsBtn);

        panel.add(searchField, allBtn, reposBtn, gistsBtn);
        return panel;
    }

    private Div createFilterButton(String filterType, String text) {
        Div btn = new Div();
        btn.setText(text);
        btn.addClassName(BTN_CLASS);
        btn.getElement().executeJs("this.style.cursor='pointer'");
        btn.getElement().setAttribute("data-filter", filterType);
        return btn;
    }

    private void addClickHandler(Div btn, String filterType, Div... buttons) {
        btn.getElement().addEventListener("click", e -> {
            for (Div b : buttons) {
                b.getClassNames().remove(BTN_ACTIVE);
            }
            btn.getClassNames().add(BTN_ACTIVE);
            filterSectionsByType(filterType);
        });
    }

    private void filterSectionsByType(String filterType) {
        for (Component child : getContentComponents()) {
            if (child instanceof VerticalLayout section) {
                String title = getSectionTitle(section);
                boolean visible = switch (filterType) {
                    case FILTER_ALL -> true;
                    case FILTER_REPOS -> SECTION_REPOS_TITLE.equals(title);
                    case FILTER_GISTS -> SECTION_GISTS_TITLE.equals(title);
                    default -> true;
                };
                section.setVisible(visible);
            }
        }
    }

    private String getSectionTitle(VerticalLayout section) {
        for (Component item : section.getChildren().toList()) {
            if (item instanceof H2 h2) {
                return h2.getText();
            }
        }
        return "";
    }

    private void filterContent(String query) {
        String lowerQuery = query.toLowerCase().trim();

        for (Component child : getContentComponents()) {
            if (child instanceof VerticalLayout section && section.isVisible()) {
                filterSection(section, lowerQuery);
            }
        }
    }

    private void filterSection(VerticalLayout section, String query) {
        boolean anyVisible = false;

        for (Component item : section.getChildren().toList()) {
            if (item instanceof VerticalLayout card && card.getClassNames().contains("adwaita-card")) {
                boolean cardVisible = query.isEmpty() || isCardMatch(card, query);
                card.setVisible(cardVisible);
                if (cardVisible) anyVisible = true;
            }
        }

        section.setVisible(anyVisible || query.isEmpty());
    }

    private boolean isCardMatch(VerticalLayout card, String query) {
        for (Component row : card.getChildren().toList()) {
            if (row instanceof Div rowDiv && rowDiv.getText().toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    private List<Component> getContentComponents() {
        List<Component> components = new ArrayList<>();
        for (Component c : getChildren().toList()) {
            if (!(c instanceof HorizontalLayout)) {
                components.add(c);
            }
        }
        return components;
    }
}
