package ru.oldzoomer.www.model;

/**
 * Язык и уровень владения.
 */
public record Language(
        String language,
        String level
) {
    public static Language of(String language, String level) {
        return new Language(
                blankToNull(language),
                blankToNull(level)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
