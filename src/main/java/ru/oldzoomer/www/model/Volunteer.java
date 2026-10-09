package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Волонтёрский опыт.
 */
public record Volunteer(
        String organization,
        String position,
        String url,
        String summary,
        List<String> highlights,
        @JsonProperty("startDate") LocalDate start,
        @JsonProperty("endDate") LocalDate end,
        String role
) {
    public static Volunteer of(
            String organization, String position, String url,
            String summary, List<String> highlights,
            LocalDate start, LocalDate end, String role) {
        return new Volunteer(
                blankToNull(organization),
                blankToNull(position),
                blankToNull(url),
                blankToNull(summary),
                highlights != null ? highlights : List.of(),
                start, end,
                blankToNull(role)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
