package hu.mrflow.labelanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import hu.mrflow.labelanalyzer.model.AnalysisProject;

import java.io.File;
import java.nio.file.*;
import java.util.*;

/**
 * Projekteket ment és tölt be JSON formátumban.
 * Helye: ~/.labelanalyzer/projects/
 * Minden projekt egy külön fájl: <uuid>.json
 */
public class ProjectRepository {

    private static final Path PROJECTS_DIR =
            Path.of(System.getProperty("user.home"), ".labelanalyzer", "projects");

    private static ProjectRepository instance;
    private final ObjectMapper mapper;

    private ProjectRepository() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Files.createDirectories(PROJECTS_DIR);
        } catch (Exception e) {
            System.err.println("Cannot create projects dir: " + e.getMessage());
        }
    }

    public static ProjectRepository getInstance() {
        if (instance == null) instance = new ProjectRepository();
        return instance;
    }

    // ── DTO a szerializációhoz ────────────────────────────────────────────────
    // Az AnalysisProject-et nem akarjuk Jackson-nal teljesen szerializálni
    // (result lehet nagyon nagy), ezért egy egyszerű DTO-t használunk.

    public static class ProjectDto {
        public String id;
        public String name;
        public String oldDecisionPath;
        public String newDecisionPath;
        public String labelPath;
    }

    // ── Mentés ───────────────────────────────────────────────────────────────

    public void save(AnalysisProject project) {
        try {
            ProjectDto dto = toDto(project);
            Path file = PROJECTS_DIR.resolve(dto.id + ".json");
            mapper.writeValue(file.toFile(), dto);
        } catch (Exception e) {
            System.err.println("Failed to save project: " + e.getMessage());
        }
    }

    public void saveAll(List<AnalysisProject> projects) {
        // Töröljük a régi fájlokat
        try {
            Files.list(PROJECTS_DIR)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> p.toFile().delete());
        } catch (Exception ignored) {}

        projects.forEach(this::save);
    }

    // ── Betöltés ─────────────────────────────────────────────────────────────

    public List<AnalysisProject> loadAll() {
        List<AnalysisProject> result = new ArrayList<>();
        try {
            if (!Files.exists(PROJECTS_DIR)) return result;
            Files.list(PROJECTS_DIR)
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .forEach(p -> {
                        try {
                            ProjectDto dto = mapper.readValue(p.toFile(), ProjectDto.class);
                            result.add(fromDto(dto));
                        } catch (Exception e) {
                            System.err.println("Failed to load project file: " + p + " – " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.err.println("Failed to list projects: " + e.getMessage());
        }
        return result;
    }

    public void delete(AnalysisProject project) {
        try {
            Path file = PROJECTS_DIR.resolve(project.getId() + ".json");
            Files.deleteIfExists(file);
        } catch (Exception e) {
            System.err.println("Failed to delete project: " + e.getMessage());
        }
    }

    // ── Konverzió ─────────────────────────────────────────────────────────────

    private ProjectDto toDto(AnalysisProject p) {
        ProjectDto dto = new ProjectDto();
        dto.id   = p.getId();
        dto.name = p.getName();
        dto.oldDecisionPath = p.getOldDecisionFile() != null ? p.getOldDecisionFile().getAbsolutePath() : null;
        dto.newDecisionPath = p.getNewDecisionFile() != null ? p.getNewDecisionFile().getAbsolutePath() : null;
        dto.labelPath       = p.getLabelFile()       != null ? p.getLabelFile().getAbsolutePath()       : null;
        return dto;
    }

    private AnalysisProject fromDto(ProjectDto dto) {
        AnalysisProject p = new AnalysisProject(dto.name, dto.id);
        if (dto.oldDecisionPath != null) { File f = new File(dto.oldDecisionPath); if (f.exists()) p.setOldDecisionFile(f); }
        if (dto.newDecisionPath != null) { File f = new File(dto.newDecisionPath); if (f.exists()) p.setNewDecisionFile(f); }
        if (dto.labelPath       != null) { File f = new File(dto.labelPath);       if (f.exists()) p.setLabelFile(f); }
        return p;
    }
}