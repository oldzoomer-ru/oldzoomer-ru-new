package ru.oldzoomer.www.model;

import java.util.List;

/**
 * Навык в резюме.
 */
public record Skill(
        String name,
        String level,
        List<String> keywords
) {
    public static Skill of(String name, String level, List<String> keywords) {
        return new Skill(
                blankToNull(name),
                blankToNull(level),
                keywords != null && !keywords.isEmpty() ? keywords : null
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
