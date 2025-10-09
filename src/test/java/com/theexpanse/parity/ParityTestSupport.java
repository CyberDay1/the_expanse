package com.theexpanse.parity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ParityTestSupport {
    private static final Path PROJECT_ROOT = locateProjectRoot();
    private static final Path BASE_RESOURCES = PROJECT_ROOT.resolve("src/main/resources");
    private static final Path VERSIONS_DIR = PROJECT_ROOT.resolve("versions");

    private ParityTestSupport() {
    }

    static Path projectRoot() {
        return PROJECT_ROOT;
    }

    static Path baseResources() {
        return BASE_RESOURCES;
    }

    static Path versionsDirectory() {
        return VERSIONS_DIR;
    }

    static List<String> listVariants() throws IOException {
        if (!Files.isDirectory(VERSIONS_DIR)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(VERSIONS_DIR)) {
            return stream
                .filter(Files::isDirectory)
                .map(path -> path.getFileName().toString())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
    }

    static Path resolveVariantResources(String variant) {
        Path variantRoot = VERSIONS_DIR.resolve(variant).resolve("src/main/resources");
        return Files.isDirectory(variantRoot) ? variantRoot : null;
    }

    static Path resolveVariantBuildResources(String variant) {
        Path buildRoot = VERSIONS_DIR.resolve(variant).resolve("build/resources/main");
        return Files.isDirectory(buildRoot) ? buildRoot : null;
    }

    static Path resolveSharedBuildResources() {
        Path buildRoot = PROJECT_ROOT.resolve("build/resources/main");
        return Files.isDirectory(buildRoot) ? buildRoot : null;
    }

    static Map<String, JsonElement> loadMergedJson(Path baseResources,
                                                   Path variantResources,
                                                   Path relativeRoot) throws IOException {
        Map<String, JsonElement> merged = new TreeMap<>();
        Path baseRoot = baseResources.resolve(relativeRoot);
        merged.putAll(readJsonTree(baseRoot, baseRoot));

        if (variantResources != null) {
            Path variantRoot = variantResources.resolve(relativeRoot);
            merged.putAll(readJsonTree(variantRoot, variantRoot));
        }

        return merged;
    }

    private static Map<String, JsonElement> readJsonTree(Path root, Path relativizeBase) throws IOException {
        if (!Files.isDirectory(root)) {
            return Map.of();
        }

        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toMap(
                    path -> relativizeBase.relativize(path).toString().replace('\\', '/'),
                    ParityTestSupport::parseJson,
                    (existing, replacement) -> replacement,
                    TreeMap::new
                ));
        }
    }

    static JsonElement parseJson(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read JSON from " + path, exception);
        }
    }

    static Optional<String> detectActiveVariant() {
        String property = System.getProperty("stonecutter.active");
        if (property != null && !property.isBlank()) {
            return Optional.of(property.trim());
        }

        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path parent = current.getParent();
            if (parent != null && parent.getFileName() != null && parent.getFileName().toString().equals("versions")) {
                return Optional.of(current.getFileName().toString());
            }
            current = parent;
        }

        return Optional.empty();
    }

    static Properties loadVariantProperties(String variant) throws IOException {
        Path propertiesFile = VERSIONS_DIR.resolve(variant).resolve("gradle.properties");
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(propertiesFile)) {
            properties.load(stream);
        }
        return properties;
    }

    static Path locateProjectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to resolve project root from working directory");
    }
}
