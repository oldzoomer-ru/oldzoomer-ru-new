package ru.oldzoomer.www.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.oldzoomer.www.model.Resume;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Сервис для загрузки и парсинга JSON-резюме с указанного URL.
 * <p>
 * Загружает JSON по HTTP, парсит в объект Resume по схеме JSON Resume.
 * URL берётся из конфигурации (property {@code resume.json.url}).
 *
 * @see <a href="https://jsonresume.org/schema/">JSON Resume Schema</a>
 */
@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String resumeUrl;

    public ResumeParserService(
            @Value("${resume.json.url}") String resumeUrl,
            ObjectMapper objectMapper) {
        this.resumeUrl = resumeUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Загружает и парсит резюме с настроенного URL.
     *
     * @return распарсенный объект Resume
     * @throws ResumeFetchException если не удалось загрузить или распарсить
     */
    public Resume fetchAndParse() {
        log.info("Загрузка резюме с URL: {}", resumeUrl);

        String json = fetchJson(resumeUrl);
        return parse(json);
    }

    /**
     * Загружает текст с URL.
     */
    private String fetchJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResumeFetchException(
                        "Не удалось загрузить резюме: HTTP " + response.statusCode());
            }

            String body = response.body();
            log.debug("Получен JSON длиной {} байт", body.length());
            return body;
        } catch (IllegalArgumentException e) {
            throw new ResumeFetchException("Некорректный URL: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResumeFetchException("Загрузка прервана", e);
        } catch (IOException e) {
            throw new ResumeFetchException("Ошибка сети при загрузке: " + e.getMessage(), e);
        }
    }

    /**
     * Парсит JSON-строку в объект Resume.
     */
    public Resume parse(String json) {
        log.debug("Парсинг JSON-резюме (длина: {} символов)", json != null ? json.length() : 0);
        return objectMapper.readValue(json, Resume.class);
    }

    /**
     * Валидирует Resume: проверяет наличие ключевых полей.
     *
     * @param resume объект резюме
     * @return список предупреждений
     */
    public java.util.List<String> validate(Resume resume) {
        var issues = new java.util.ArrayList<String>();

        if (resume.meta() == null) {
            issues.add("Отсутствует блок 'meta' — имя и контакты");
        } else {
            if (resume.meta().name() == null || resume.meta().name().isBlank()) {
                issues.add("Поле 'name' в meta обязательно");
            }
        }

        return issues;
    }

    public static class ResumeFetchException extends RuntimeException {
        public ResumeFetchException(String message) {
            super(message);
        }

        public ResumeFetchException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ResumeParseException extends RuntimeException {
        public ResumeParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
