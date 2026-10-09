package ru.oldzoomer.www.model;

import java.net.URI;
import java.time.LocalDate;

/**
 * Сертификат/аккредитация в резюме.
 */
public record Certification(
        String name,
        LocalDate date,
        String issuer,
        URI url
) {
    public static Certification of(String name, LocalDate date, String issuer, URI url) {
        return new Certification(
                blankToNull(name),
                date,
                blankToNull(issuer),
                url
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
