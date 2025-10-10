package com.theexpanse.datapack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lightweight datapack validation to ensure JEI cross-links remain consistent.
 */
public final class DatapackValidationTest {
    private static final Pattern EXTRA_RECIPE_PATTERN = Pattern.compile(
        "\\\"extra_recipe_categories\\\"\\s*:\\s*\\[(.*?)\\]",
        Pattern.DOTALL);
    private static final Pattern OVERLAY_KEY_PATTERN = Pattern.compile("\\\"jei:cyclic:hud_overlay\\\"");
    private static final Pattern TRANSLATION_KEY_PATTERN = Pattern.compile("cyclic\\.overlay\\.patchouli\\.link");
    private static final Pattern LANG_ENTRY_PATTERN = Pattern.compile(
        "\\\"cyclic\\.overlay\\.patchouli\\.link\\\"\\s*:\\s*\\\"[^\\\"]+\\\"");

    private DatapackValidationTest() {
    }

    public static void main(String[] args) throws IOException {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path resourcesRoot = resolveResourcesRoot(projectRoot);
        Path patchouliRoot = resourcesRoot.resolve("assets/cyclic/patchouli_books");
        requireDirectory(patchouliRoot, "Patchouli book directory");

        List<Path> machineEntries;
        try (Stream<Path> stream = Files.walk(patchouliRoot)) {
            machineEntries = stream
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                .filter(path -> path.toString().replace('\\', '/').contains("/entries/machines/"))
                .sorted(Comparator.comparing(path -> patchouliRoot.relativize(path).toString()))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        if (machineEntries.isEmpty()) {
            throw new IllegalStateException("No machine overlay entries found under " + patchouliRoot);
        }

        for (Path entry : machineEntries) {
            validateEntry(entry);
        }

        Path langFile = resourcesRoot.resolve("assets/cyclic/lang/en_us.json");
        requireFile(langFile, "cyclic language file");
        String langContent = Files.readString(langFile, StandardCharsets.UTF_8);
        Matcher langMatcher = LANG_ENTRY_PATTERN.matcher(langContent);
        int langCount = countMatches(langMatcher);
        if (langCount == 0) {
            throw new IllegalStateException("Missing localization for cyclic.overlay.patchouli.link in " + langFile);
        }
        if (langCount > 1) {
            throw new IllegalStateException("Duplicate localization keys for cyclic.overlay.patchouli.link in "
                + langFile);
        }

        System.out.println("DatapackValidationTest: " + machineEntries.size()
            + " machine overlay entries validated successfully.");
    }

    private static void validateEntry(Path entry) throws IOException {
        String content = Files.readString(entry, StandardCharsets.UTF_8);

        Matcher extraMatcher = EXTRA_RECIPE_PATTERN.matcher(content);
        if (!extraMatcher.find()) {
            throw new IllegalStateException("Missing extra_recipe_categories in " + entry);
        }
        String categoryBlock = extraMatcher.group(1);
        int overlayCount = countMatches(OVERLAY_KEY_PATTERN.matcher(categoryBlock));
        if (overlayCount == 0) {
            throw new IllegalStateException("No JEI overlay category present in " + entry);
        }
        if (overlayCount > 1) {
            throw new IllegalStateException("Duplicate JEI overlay categories found in " + entry);
        }

        Matcher translationMatcher = TRANSLATION_KEY_PATTERN.matcher(content);
        int translationCount = countMatches(translationMatcher);
        if (translationCount == 0) {
            throw new IllegalStateException("Missing localized link reference in " + entry);
        }
        if (translationCount > 1) {
            throw new IllegalStateException("Duplicate localized link references in " + entry);
        }
    }

    private static int countMatches(Matcher matcher) {
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static void requireDirectory(Path directory, String description) {
        if (!Files.isDirectory(directory)) {
            throw new IllegalStateException(description + " not found: " + directory.toAbsolutePath());
        }
    }

    private static void requireFile(Path file, String description) {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(description + " not found: " + file.toAbsolutePath());
        }
    }

    private static Path resolveResourcesRoot(Path projectRoot) {
        Path[] candidates = new Path[] {
            projectRoot.resolve("src/main/resources"),
            projectRoot.resolve("template/src/main/resources")
        };
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to locate resources directory under " + projectRoot);
    }
}
