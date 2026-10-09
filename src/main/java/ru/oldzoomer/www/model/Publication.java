package ru.oldzoomer.www.model;

/**
 * Публикация.
 */
public record Publication(
        String name,
        String publisher,
        String releaseDate,
        String url,
        String summary
) {
    public static Publication of(
            String name, String publisher, String releaseDate,
            String url, String summary) {
        return new Publication(
                blankToNull(name),
                blankToNull(publisher),
                blankToNull(releaseDate),
                blankToNull(url),
                blankToNull(summary)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
