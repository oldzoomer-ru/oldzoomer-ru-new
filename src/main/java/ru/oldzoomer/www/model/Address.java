package ru.oldzoomer.www.model;

/**
 * Адрес проживания.
 */
public record Address(
        String street,
        String city,
        String region,
        String postalCode,
        String country,
        String latitude,
        String longitude
) {
    public static Address of(
            String street, String city, String region,
            String postalCode, String country, String latitude, String longitude) {
        return new Address(
                blankToNull(street),
                blankToNull(city),
                blankToNull(region),
                blankToNull(postalCode),
                blankToNull(country),
                blankToNull(latitude),
                blankToNull(longitude)
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
