package ru.oldzoomer.www.model;

import java.net.URI;
import java.util.List;

/**
 * Контактная информация кандидата.
 */
public record PersonalInfo(
        String name,
        String title,
        String email,
        String phone,
        String url,
        Address address,
        List<URI> profiles
) {
    public static PersonalInfo of(
            String name, String title, String email, String phone,
            String url, Address address, List<URI> profiles) {
        return new PersonalInfo(
                blankToNull(name),
                blankToNull(title),
                blankToNull(email),
                blankToNull(phone),
                blankToNull(url),
                address,
                profiles != null ? profiles : List.of()
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
