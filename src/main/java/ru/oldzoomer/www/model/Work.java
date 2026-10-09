package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Опыт работы.
 */
public record Work(
        @JsonProperty("name") String company,
        String position,
        String url,
        String summary,
        List<String> highlights,
        @JsonProperty("startDate") LocalDate start,
        @JsonProperty("endDate") LocalDate end,
        String location,
        String role
) {
    public static Work of(
            String company, String position, String url, String summary,
            List<String> highlights, LocalDate start, LocalDate end,
            String location, String role) {
        return new Work(
                blankToNull(company),
                blankToNull(position),
                blankToNull(url),
                blankToNull(summary),
                highlights != null ? highlights : List.of(),
                start, end,
                blankToNull(location),
                blankToNull(role)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
