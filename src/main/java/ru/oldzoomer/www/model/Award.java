package ru.oldzoomer.www.model;

/**
 * Награда.
 */
public record Award(
        String title,
        String date,
        String awarder,
        String summary
) {
    public static Award of(String title, String date, String awarder, String summary) {
        return new Award(
                blankToNull(title),
                blankToNull(date),
                blankToNull(awarder),
                blankToNull(summary)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
