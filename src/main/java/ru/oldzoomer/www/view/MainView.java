package ru.oldzoomer.www.view;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import ru.oldzoomer.www.model.Resume;
import ru.oldzoomer.www.service.ResumeDisplayService;
import ru.oldzoomer.www.service.ResumeDisplayService.ResumeDisplayData;
import ru.oldzoomer.www.service.ResumeDisplayService.SectionItem;
import ru.oldzoomer.www.service.ResumeParserService;
import ru.oldzoomer.www.service.ResumeParserService.ResumeFetchException;
import ru.oldzoomer.www.service.ResumeParserService.ResumeParseException;

import java.util.List;

@Route("")
@RegisterReflectionForBinding(Resume.class)
public class MainView extends VerticalLayout {

    private final ResumeParserService parserService;
    private final ResumeDisplayService displayService;

    public MainView(ResumeParserService parserService, ResumeDisplayService displayService) {
        this.parserService = parserService;
        this.displayService = displayService;

        // В Libadwaita контент центрируется и имеет строго ограниченную ширину
        addClassName("adwaita-window");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        loadResume();
    }

    private void loadResume() {
        Div loading = new Div();
        loading.setText("Загрузка резюме...");
        loading.addClassName("loading");
        add(loading);

        try {
            Resume resume = parserService.fetchAndParse();
            removeAll();

            List<String> issues = parserService.validate(resume);
            if (!issues.isEmpty()) {
                Div warning = new Div();
                warning.setText(String.join(" | ", issues));
                warning.addClassName("warning");
                add(warning);
            }

            ResumeDisplayData data = displayService.toDisplayData(resume);

            // Имитация окна GNOME: Сверху HeaderBar, снизу прокручиваемый контент
            add(buildHeaderBar(data));
            add(buildScrollableContent(data));

        } catch (ResumeFetchException | ResumeParseException e) {
            removeAll();
            Div error = new Div();
            error.setText("Ошибка: " + e.getMessage());
            error.addClassName("error");
            add(error);
        }
    }

    /**
     * Вместо простого заголовка строим нативный Libadwaita HeaderBar
     */
    private HorizontalLayout buildHeaderBar(ResumeDisplayData data) {
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.addClassName("adwaita-header-bar");
        headerBar.setWidthFull();

        // Заголовок окна (ФИО)
        H1 windowTitle = new H1(data.fullName());
        windowTitle.addClassName("adwaita-window-title");
        headerBar.add(windowTitle);

        // Имитация кнопок управления окном GNOME (Закрыть/Развернуть) для аутентичности
        Div windowControls = new Div();
        windowControls.addClassName("adwaita-window-controls");
        headerBar.add(windowControls);

        return headerBar;
    }

    /**
     * Прокручиваемая область под HeaderBar
     */
    private VerticalLayout buildScrollableContent(ResumeDisplayData data) {
        VerticalLayout container = new VerticalLayout();
        container.addClassName("adwaita-content-container");

        // Краткие контакты и summary сверху
        if (!data.contactInfo().isBlank() || !data.summary().isBlank()) {
            Div subHeader = createSubHeader(data);
            container.add(subHeader);
        }

        // Рендерим секции
        buildSections(container, data);

        return container;
    }

    private static @NonNull Div createSubHeader(ResumeDisplayData data) {
        Div subHeader = new Div();
        subHeader.addClassName("adwaita-sub-header");

        if (!data.contactInfo().isBlank()) {
            Paragraph contacts = new Paragraph(data.contactInfo());
            contacts.addClassName("adwaita-contacts");
            subHeader.add(contacts);
        }
        if (!data.summary().isBlank()) {
            Paragraph summary = new Paragraph(data.summary());
            summary.addClassName("adwaita-summary");
            subHeader.add(summary);
        }
        return subHeader;
    }

    private void buildSections(VerticalLayout container, ResumeDisplayData data) {
        addSection(container, "Опыт работы", data.workExperience());
        addSection(container, "Волонтёрство", data.volunteer());
        addSection(container, "Образование", data.education());
        addSection(container, "Навыки", data.skills());
        addSection(container, "Сертификаты", data.certifications());
        addSection(container, "Проекты", data.projects());
        addSection(container, "Языки", data.languages());
        addSection(container, "Награды", data.awards());
        addSection(container, "Рекомендации", data.references());
    }

    /**
     * Создает секцию в виде Adwaita Preferences Group (Заголовок + Карточка со строками)
     */
    private void addSection(VerticalLayout parent, String title, List<SectionItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        VerticalLayout sectionGroup = new VerticalLayout();
        sectionGroup.addClassName("adwaita-pref-group");
        sectionGroup.setPadding(false);
        sectionGroup.setSpacing(false);

        // Заголовок группы (маленький, серый, чуть левее карточки)
        H2 sectionTitle = new H2(title);
        sectionTitle.addClassName("adwaita-pref-group-title");
        sectionGroup.add(sectionTitle);

        // Карточка-контейнер (GtkListBox с рамкой и скруглением)
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

    /**
     * Индивидуальная строка внутри карточки (Adwaita List Row)
     */
    private Div buildSectionItem(SectionItem item) {
        Div row = new Div();
        row.addClassName("adwaita-row");

        // Мета-информация (Период/Компания) сдвигается вправо или наверх неброским шрифтом
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
