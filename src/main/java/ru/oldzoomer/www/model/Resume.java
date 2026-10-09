package ru.oldzoomer.www.model;

import java.util.List;

/**
 * Корневой объект JSON-резюме.
 *
 * @see <a href="https://jsonresume.org/schema/">JSON Resume Schema</a>
 */
public record Resume(
        Meta meta,
        String summary,
        List<Work> work,
        List<Education> education,
        List<Skill> skills,
        List<Project> projects,
        List<Volunteer> volunteer,
        List<Award> awards,
        List<Publication> publications,
        List<Language> languages,
        List<Reference> references,
        List<Certification> certifications
) {
    public static Resume of(
            Meta meta, String summary,
            List<Work> work, List<Education> education,
            List<Skill> skills, List<Project> projects,
            List<Volunteer> volunteer, List<Award> awards,
            List<Publication> publications,
            List<Language> languages, List<Reference> references,
            List<Certification> certifications) {
        return new Resume(
                meta,
                blankToNull(summary),
                list(work, List.of()),
                list(education, List.of()),
                list(skills, List.of()),
                list(projects, List.of()),
                list(volunteer, List.of()),
                list(awards, List.of()),
                list(publications, List.of()),
                list(languages, List.of()),
                list(references, List.of()),
                list(certifications, List.of())
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static <T> List<T> list(List<T> src, List<T> fallback) {
        return src != null ? src : fallback;
    }

    /**
     * Метаданные резюме (корневой объект JSON Resume).
     * Содержит имя, заголовок, контакты и локацию.
     */
    public record Meta(
            String name,
            String label,
            String summary,
            String url,
            String email,
            Location location,
            List<Profile> profiles
    ) {
        public static Meta of(String name, String label, String summary, String url,
                              String email, Location location, List<Profile> profiles) {
            return new Meta(
                    blankToNull(name),
                    blankToNull(label),
                    blankToNull(summary),
                    blankToNull(url),
                    blankToNull(email),
                    location,
                    profiles != null ? profiles : List.of()
            );
        }
    }

    /**
     * Локация из meta.
     */
    public record Location(
            String country,
            String city
    ) {
        public static Location of(String country, String city) {
            return new Location(
                    blankToNull(country),
                    blankToNull(city)
            );
        }

        private static String blankToNull(String s) {
            return (s == null || s.isBlank()) ? null : s;
        }
    }

    /**
     * Профиль (social network) — для полноты схемы.
     */
    public record Profile(
            String network,
            String url
    ) {
        public static Profile of(String network, String url) {
            return new Profile(
                    blankToNull(network),
                    blankToNull(url)
            );
        }

        private static String blankToNull(String s) {
            return (s == null || s.isBlank()) ? null : s;
        }
    }
}
