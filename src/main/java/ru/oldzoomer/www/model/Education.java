package ru.oldzoomer.www.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Образование.
 */
public record Education(
        String institution,
        String url,
        String studyType,
        String area,
        String score,
        @JsonProperty("startDate") LocalDate start,
        @JsonProperty("endDate") LocalDate end,
        List<String> courses
) {
    public static Education of(
            String institution, String url, String studyType,
            String area, String score, LocalDate start, LocalDate end,
            List<String> courses) {
        return new Education(
                blankToNull(institution),
                blankToNull(url),
                blankToNull(studyType),
                blankToNull(area),
                blankToNull(score),
                start, end,
                courses != null ? courses : List.of()
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
