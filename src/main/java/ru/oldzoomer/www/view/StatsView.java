package ru.oldzoomer.www.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.oldzoomer.www.model.GitHubData;
import ru.oldzoomer.www.service.GitHubDisplayService;

@Route("stats")
@PageTitle("Статистика - Егор Гаврилов")
@SuppressWarnings("this-escape")
public class StatsView extends BaseView {

    public StatsView(GitHubData gitHubData, GitHubDisplayService displayService) {
        init(gitHubData, displayService);
        setupUI();
        loadStats();
    }

    private void loadStats() {
        loadData("Загрузка статистики...", data -> {
            VerticalLayout container = buildScrollableContent(data);
            addSection(container, "Общая статистика", data.stats());
            return container;
        });
    }
}
