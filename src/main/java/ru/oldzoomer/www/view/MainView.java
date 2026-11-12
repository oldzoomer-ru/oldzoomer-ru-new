package ru.oldzoomer.www.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.service.GitHubDisplayService;
import ru.oldzoomer.www.service.GitHubDisplayService.GitHubDisplayData;

@Route("")
@PageTitle("Егор Гаврилов - Java-разработчик")
@SuppressWarnings("this-escape")
public class MainView extends BaseView {

    public MainView(GitHubData gitHubData, GitHubDisplayService displayService) {
        init(gitHubData, displayService);
        setupUI();
        loadGitHubData();
    }

    private void loadGitHubData() {
        loadData("Загрузка данных GitHub...", data -> {
            VerticalLayout container = buildScrollableContent(data);
            buildSections(container, data);
            return container;
        });
    }

    private void buildSections(VerticalLayout container, GitHubDisplayData data) {
        addSection(container, "О себе", data.profileReadme());
        addSection(container, "Репозитории", data.allRepos());
        addSection(container, "Активность", data.activity());
        addSection(container, "Статистика", data.stats());
    }
}
