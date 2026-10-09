package ru.oldzoomer.www.model;

/**
 * Рекомендация/характеристика.
 */
public record Reference(
        String name,
        String reference,
        String email,
        String phone
) {
    public static Reference of(String name, String reference, String email, String phone) {
        return new Reference(
                blankToNull(name),
                blankToNull(reference),
                blankToNull(email),
                blankToNull(phone)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
