package ru.oldzoomer.www;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.PageTitle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@StyleSheet("styles.css")
@Push
@PageTitle("Егор Гаврилов - Java-разработчик")
@Viewport("width=device-width, initial-scale=1")
public class Application implements AppShellConfigurator {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
