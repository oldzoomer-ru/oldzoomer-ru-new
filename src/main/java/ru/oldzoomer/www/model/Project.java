package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Проект.
 */
public record Project(
        String name,
        String description,
        String url,
        List<String> highlights,
        String keywords,
        @JsonProperty("startDate") LocalDate start,
        @JsonProperty("endDate") LocalDate end,
        String role
) {
    public static Project of(
            String name, String description, String url,
            List<String> highlights, String keywords,
            LocalDate start, LocalDate end, String role) {
        return new Project(
                blankToNull(name),
                blankToNull(description),
                blankToNull(url),
                highlights != null ? highlights : List.of(),
                blankToNull(keywords),
                start, end,
                blankToNull(role)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
