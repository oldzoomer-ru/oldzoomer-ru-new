package ru.oldzoomer.www.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.service.GitHubDisplayService;

@Route("repos")
@PageTitle("Репозитории - Егор Гаврилов")
@SuppressWarnings("this-escape")
public class ReposView extends BaseView {
    private static final String SECTION_REPOS_TITLE = "Репозитории";
    private static final String SECTION_GISTS_TITLE = "Gists";

    public ReposView(GitHubData gitHubData, GitHubDisplayService displayService) {
        init(gitHubData, displayService);
        setupUI();
        loadRepos();
    }

    private void loadRepos() {
        loadData("Загрузка репозиториев...", data -> {
            VerticalLayout container = buildScrollableContent();

            addSection(container, SECTION_REPOS_TITLE, data.allRepos());
            addSection(container, SECTION_GISTS_TITLE, data.gists());

            return container;
        });
    }
}
