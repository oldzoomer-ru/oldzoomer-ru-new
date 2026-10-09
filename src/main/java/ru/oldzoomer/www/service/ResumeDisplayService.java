package ru.oldzoomer.www.service;

import org.springframework.stereotype.Service;
import ru.oldzoomer.www.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для подготовки данных резюме к отображению в UI.
 * <p>
 * Форматирует даты, объединяет данные, создаёт сводки.
 *
 * @see ResumeParserService
 */
@Service
public class ResumeDisplayService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM.yyyy");

    /**
     * Форматирует Resume для отображения в виде структурированных данных.
     */
    public ResumeDisplayData toDisplayData(Resume resume) {
        var meta = resume.meta();
        String fullName = Optional.ofNullable(meta)
                .map(Resume.Meta::name)
                .orElse("Не указано");

        String label = Optional.ofNullable(meta)
                .map(Resume.Meta::label)
                .map(l -> " — " + l)
                .orElse("");

        String contactInfo = buildContactInfo(meta);

        return ResumeDisplayData.of(
                fullName + label,
                contactInfo,
                buildSummary(resume),
                formatWorkExperience(resume.work()),
                formatVolunteer(resume.volunteer()),
                formatEducation(resume.education()),
                formatSkills(resume.skills()),
                formatProjects(resume.projects()),
                formatLanguages(resume.languages()),
                formatAwards(resume.awards()),
                formatReferences(resume.references()),
                formatCertifications(resume.certifications())
        );
    }

    /**
     * Строит сводку: meta.summary или top-level summary.
     */
    private String buildSummary(Resume resume) {
        var meta = resume.meta();
        String metaSummary = Optional.ofNullable(meta)
                .map(Resume.Meta::summary)
                .orElse(null);
        return metaSummary != null && !metaSummary.isBlank()
                ? metaSummary
                : Optional.ofNullable(resume.summary()).orElse("");
    }

    /**
     * Форматирует опыт работы.
     */
    private List<SectionItem> formatWorkExperience(List<Work> works) {
        if (works == null || works.isEmpty()) {
            return Collections.emptyList();
        }
        return works.stream()
                .map(w -> {
                    String period = formatPeriod(w.start(), w.end());
                    String position = Optional.ofNullable(w.position()).orElse("");
                    String company = Optional.ofNullable(w.company()).orElse("");
                    String title = (position.isEmpty() && company.isEmpty())
                            ? ""
                            : (company.isEmpty() ? position : (position.isEmpty() ? company : position + " в " + company));
                    String location = Optional.ofNullable(w.location()).orElse("");
                    String detail = Optional.ofNullable(w.summary())
                            .map(String::trim)
                            .orElse("");
                    if (!location.isBlank()) {
                        detail = location + (detail.isBlank() ? "" : "\n" + detail);
                    }
                    List<String> highlights = Optional.ofNullable(w.highlights()).orElse(List.of());
                    return SectionItem.of(title, period, detail, highlights);
                })
                .collect(Collectors.toList());
    }

    /**
     * Форматирует волонтёрский опыт.
     */
    private List<SectionItem> formatVolunteer(List<Volunteer> volunteers) {
        if (volunteers == null || volunteers.isEmpty()) {
            return Collections.emptyList();
        }
        return volunteers.stream()
                .map(v -> {
                    String period = formatPeriod(v.start(), v.end());
                    String organization = Optional.ofNullable(v.organization()).orElse("");
                    String position = Optional.ofNullable(v.position()).orElse("");
                    String title = position.isEmpty() ? organization : (organization.isEmpty() ? position : position + " в " + organization);
                    String detail = Optional.ofNullable(v.summary())
                            .map(String::trim)
                            .orElse("");
                    List<String> highlights = Optional.ofNullable(v.highlights()).orElse(List.of());
                    return SectionItem.of(title, period, detail, highlights);
                })
                .collect(Collectors.toList());
    }

    /**
     * Форматирует образование.
     */
    private List<SectionItem> formatEducation(List<Education> educations) {
        if (educations == null || educations.isEmpty()) {
            return Collections.emptyList();
        }
        return educations.stream()
                .map(e -> {
                    String period = formatPeriod(e.start(), e.end());
                    String institution = Optional.ofNullable(e.institution()).orElse("");
                    String area = Optional.ofNullable(e.area()).orElse("");
                    String studyType = Optional.ofNullable(e.studyType()).orElse("");
                    String title = buildEducationTitle(institution, area, studyType);
                    String detail = Optional.ofNullable(e.score())
                            .map(s -> "Бал/оценка: " + s)
                            .orElse("");
                    List<String> courses = Optional.ofNullable(e.courses()).orElse(List.of());
                    return SectionItem.of(title, period, detail, courses);
                })
                .collect(Collectors.toList());
    }

    /**
     * Форматирует навыки.
     */
    private List<SectionItem> formatSkills(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return Collections.emptyList();
        }
        // Группируем по уровню
        var grouped = skills.stream()
                .collect(Collectors.groupingBy(
                        s -> Optional.ofNullable(s.level()).orElse("Не указано"),
                        Collectors.mapping(Skill::name, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .sorted((a, b) -> {
                    // Порядок: Expert > Senior > Mid > Junior > Basic
                    String order = "Expert,Senior,Mid,Junior,Basic,Не указано";
                    assert a.getKey() != null;
                    int ia = order.indexOf(a.getKey());
                    assert b.getKey() != null;
                    int ib = order.indexOf(b.getKey());
                    return Integer.compare(ia < 0 ? 999 : ia, ib < 0 ? 999 : ib);
                })
                .map(entry -> SectionItem.of(
                        entry.getKey(),
                        "",
                        String.join(", ", entry.getValue()),
                        Collections.emptyList()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Форматирует проекты.
     */
    private List<SectionItem> formatProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return Collections.emptyList();
        }
        return projects.stream()
                .map(p -> {
                    String period = formatPeriod(p.start(), p.end());
                    String name = Optional.ofNullable(p.name()).orElse("");
                    String role = Optional.ofNullable(p.role()).orElse("");
                    String title = role.isEmpty() ? name : (name.isEmpty() ? role : name + " (" + role + ")");
                    String detail = Optional.ofNullable(p.description())
                            .map(d -> truncate(d, 300))
                            .orElse("");
                    List<String> highlights = Optional.ofNullable(p.highlights()).orElse(List.of());
                    return SectionItem.of(title, period, detail, highlights);
                })
                .collect(Collectors.toList());
    }

    /**
     * Форматирует сертификаты.
     */
    private List<SectionItem> formatCertifications(List<Certification> certifications) {
        if (certifications == null || certifications.isEmpty()) {
            return Collections.emptyList();
        }
        return certifications.stream()
                .map(c -> {
                    String title = Optional.ofNullable(c.name()).orElse("");
                    String issuer = Optional.ofNullable(c.issuer()).orElse("");
                    LocalDate date = c.date();
                    String subtitle = dateToString(date);
                    String detail = (issuer.isEmpty() ? "" : issuer + ", ") + dateToString(date);
                    String url = Optional.ofNullable(c.url())
                            .map(Object::toString)
                            .orElse("");
                    return SectionItem.of(title, subtitle, detail, List.of(url));
                })
                .collect(Collectors.toList());
    }

    private String dateToString(LocalDate date) {
        if (date == null) {
            return "";
        }
        try {
            return date.format(DATE_FMT);
        } catch (Exception e) {
            return date.toString();
        }
    }

    /**
     * Форматирует языки.
     */
    private List<SectionItem> formatLanguages(List<Language> languages) {
        if (languages == null || languages.isEmpty()) {
            return Collections.emptyList();
        }
        return languages.stream()
                .map(l -> SectionItem.of(
                        Optional.ofNullable(l.language()).orElse(""),
                        Optional.ofNullable(l.level()).orElse(""),
                        "",
                        Collections.emptyList()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Форматирует награды.
     */
    private List<SectionItem> formatAwards(List<Award> awards) {
        if (awards == null || awards.isEmpty()) {
            return Collections.emptyList();
        }
        return awards.stream()
                .map(a -> {
                    String title = Optional.ofNullable(a.title()).orElse("");
                    String awarder = Optional.ofNullable(a.awarder()).orElse("");
                    String date = Optional.ofNullable(a.date()).orElse("");
                    String detail = (awarder.isEmpty() ? "" : awarder + ", ") + date;
                    String summary = Optional.ofNullable(a.summary()).orElse("");
                    return SectionItem.of(title, detail, summary, Collections.emptyList());
                })
                .collect(Collectors.toList());
    }

    /**
     * Форматирует рекомендации.
     */
    private List<SectionItem> formatReferences(List<Reference> references) {
        if (references == null || references.isEmpty()) {
            return Collections.emptyList();
        }
        return references.stream()
                .map(r -> {
                    String name = Optional.ofNullable(r.name()).orElse("");
                    String email = Optional.ofNullable(r.email()).orElse("");
                    String title = name + (email.isEmpty() ? "" : " (" + email + ")");
                    String ref = Optional.ofNullable(r.reference()).orElse("");
                    return SectionItem.of(title, "", ref, Collections.emptyList());
                })
                .collect(Collectors.toList());
    }

    /**
     * Форматирует период (начало — конец).
     */
    private String formatPeriod(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "";
        }
        try {
            String s = start != null ? formatLocalDate(start) : "н.в.";
            String e = end != null ? formatLocalDate(end) : "н.в.";
            return s + (start != null && end != null ? " — " + e : "");
        } catch (Exception ignored) {
            return "";
        }
    }

    private String formatLocalDate(LocalDate date) {
        try {
            return date.format(DATE_FMT);
        } catch (Exception e) {
            return date.toString();
        }
    }

    /**
     * Строит контактную информацию.
     */
    private String buildContactInfo(Resume.Meta meta) {
        if (meta == null) {
            return "";
        }
        var parts = new java.util.ArrayList<String>();
        Optional.ofNullable(meta.email())
                .filter(e -> !e.isBlank())
                .ifPresent(parts::add);
        Optional.ofNullable(meta.url())
                .filter(u -> !u.isBlank())
                .ifPresent(parts::add);
        // Локация
        Optional.ofNullable(meta.location())
                .map(Resume.Location::city)
                .filter(c -> !c.isBlank())
                .ifPresent(parts::add);
        return String.join("  |  ", parts);
    }

    /**
     * Строит заголовок образования.
     */
    private String buildEducationTitle(String institution, String area, String studyType) {
        var parts = new java.util.ArrayList<String>();
        if (studyType != null && !studyType.isBlank()) {
            parts.add(studyType);
        }
        if (area != null && !area.isBlank()) {
            parts.add("в " + area);
        }
        if (institution != null && !institution.isBlank()) {
            parts.add("(" + institution + ")");
        }
        return String.join(" ", parts);
    }

    /**
     * Обрезает строку до максимальной длины.
     */
    private String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen - 1) + "…";
    }

    /**
     * Структура данных для отображения одной секции резюме.
     */
    public record SectionItem(
            String title,
            String subtitle,
            String description,
            List<String> highlights
    ) {
        public static SectionItem of(String title, String subtitle, String description, List<String> highlights) {
            return new SectionItem(
                    title,
                    subtitle,
                    description,
                    highlights != null ? highlights : List.of()
            );
        }
    }

    /**
     * Полная структура данных для отображения резюме в UI.
     */
    public record ResumeDisplayData(
            String fullName,
            String contactInfo,
            String summary,
            List<SectionItem> workExperience,
            List<SectionItem> volunteer,
            List<SectionItem> education,
            List<SectionItem> skills,
            List<SectionItem> projects,
            List<SectionItem> languages,
            List<SectionItem> awards,
            List<SectionItem> references,
            List<SectionItem> certifications
    ) {
        public static ResumeDisplayData of(
                String fullName, String contactInfo, String summary,
                List<SectionItem> workExperience, List<SectionItem> volunteer,
                List<SectionItem> education, List<SectionItem> skills,
                List<SectionItem> projects, List<SectionItem> languages,
                List<SectionItem> awards, List<SectionItem> references,
                List<SectionItem> certifications) {
            return new ResumeDisplayData(
                    fullName, contactInfo, summary,
                    workExperience != null ? workExperience : List.of(),
                    volunteer != null ? volunteer : List.of(),
                    education != null ? education : List.of(),
                    skills != null ? skills : List.of(),
                    projects != null ? projects : List.of(),
                    languages != null ? languages : List.of(),
                    awards != null ? awards : List.of(),
                    references != null ? references : List.of(),
                    certifications != null ? certifications : List.of()
            );
        }
    }
}
