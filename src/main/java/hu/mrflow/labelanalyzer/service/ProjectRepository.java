package hu.mrflow.labelanalyzer.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import hu.mrflow.labelanalyzer.model.AnalysisProject;
import hu.mrflow.labelanalyzer.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Projekteket és elemzési eredményeket ment/tölt be JSON formátumban.
 *
 * Fájlstruktúra (~/.labelanalyzer/projects/):
 *   <uuid>.json         – projekt meta-adatok (név, fájlútvonalak, státusz, lastRun)
 *   <uuid>.result.json  – AnalysisResult (csak ha már futott elemzés)
 *
 * A két fájl szándékosan szét van választva:
 *   - A projekt lista gyorsan betölthető az eredmény nélkül
 *   - Nagy eredmény JSON nem lassítja az indulást
 */
public class ProjectRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectRepository.class);

    private static final Path PROJECTS_DIR =
            Path.of(System.getProperty("user.home"), ".labelanalyzer", "projects");

    private static ProjectRepository instance;
    private final ObjectMapper mapper;

    private ProjectRepository() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Files.createDirectories(PROJECTS_DIR);
            log.debug("Projects directory: {}", PROJECTS_DIR);
        } catch (Exception e) {
            log.error("Cannot create projects directory: {}", e.getMessage(), e);
        }
    }

    public static ProjectRepository getInstance() {
        if (instance == null) instance = new ProjectRepository();
        return instance;
    }

    // ── DTO ──────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectDto {
        public String id;
        public String name;
        public String oldDecisionPath;
        public String newDecisionPath;
        public String labelPath;
        public String status;
        public String lastRun;   // ISO LocalDateTime string
    }

    // ── Mentés ───────────────────────────────────────────────────────────────

    /**
     * Projekt meta-adatait menti. Ha van eredmény, azt is elmenti külön fájlba.
     */
    public void save(AnalysisProject project) {
        try {
            // 1. Meta mentése
            ProjectDto dto = toDto(project);
            Path metaFile = PROJECTS_DIR.resolve(dto.id + ".json");
            mapper.writeValue(metaFile.toFile(), dto);
            log.debug("Projekt meta mentve: {}", metaFile.getFileName());

            // 2. Eredmény mentése (ha van)
            if (project.getResult() != null) {
                saveResult(project.getId(), project.getResult());
            }
        } catch (Exception e) {
            log.error("Projekt mentési hiba [{}]: {}", project.getName(), e.getMessage(), e);
        }
    }

    /**
     * Csak az eredményt menti (elemzés lefutása után hívandó).
     */
    public void saveResult(String projectId, AnalysisResult result) {
        try {
            Path resultFile = PROJECTS_DIR.resolve(projectId + ".result.json");
            mapper.writeValue(resultFile.toFile(), result);
            log.info("Elemzési eredmény mentve: {}", resultFile.getFileName());
        } catch (Exception e) {
            log.error("Eredmény mentési hiba [projectId={}]: {}", projectId, e.getMessage(), e);
        }
    }

    // ── Betöltés ─────────────────────────────────────────────────────────────

    /**
     * Betölti az összes projektet meta-adatokkal együtt.
     * Az eredményeket lazy módon tölti be (csak ha létezik a .result.json).
     */
    public List<AnalysisProject> loadAll() {
        List<AnalysisProject> projects = new ArrayList<>();
        try {
            if (!Files.exists(PROJECTS_DIR)) return projects;

            Files.list(PROJECTS_DIR)
                    .filter(p -> p.toString().endsWith(".json")
                            && !p.toString().endsWith(".result.json"))
                    .sorted()
                    .forEach(p -> {
                        try {
                            ProjectDto dto = mapper.readValue(p.toFile(), ProjectDto.class);
                            AnalysisProject project = fromDto(dto);

                            // Eredmény betöltése ha létezik
                            AnalysisResult result = loadResult(dto.id);
                            if (result != null) {
                                project.setResult(result);
                                log.debug("Eredmény betöltve: {} – {} módosítás",
                                        project.getName(),
                                        result.labelChanges != null ? result.labelChanges.size() : 0);
                            }

                            projects.add(project);
                            log.debug("Projekt betöltve: {} [{}]", dto.name, dto.id);
                        } catch (Exception e) {
                            log.warn("Projekt fájl betöltési hiba: {} – {}", p.getFileName(), e.getMessage());
                        }
                    });

            log.info("{} projekt betöltve", projects.size());
        } catch (Exception e) {
            log.error("Projektek betöltési hiba: {}", e.getMessage(), e);
        }
        return projects;
    }

    /**
     * Egy projekt eredményét tölti be a .result.json fájlból.
     * Null-t ad vissza ha nincs mentett eredmény.
     */
    public AnalysisResult loadResult(String projectId) {
        Path resultFile = PROJECTS_DIR.resolve(projectId + ".result.json");
        if (!Files.exists(resultFile)) return null;
        try {
            return mapper.readValue(resultFile.toFile(), AnalysisResult.class);
        } catch (Exception e) {
            log.warn("Eredmény betöltési hiba [projectId={}]: {}", projectId, e.getMessage());
            return null;
        }
    }

    // ── Törlés ───────────────────────────────────────────────────────────────

    public void delete(AnalysisProject project) {
        try {
            Path metaFile   = PROJECTS_DIR.resolve(project.getId() + ".json");
            Path resultFile = PROJECTS_DIR.resolve(project.getId() + ".result.json");
            Files.deleteIfExists(metaFile);
            Files.deleteIfExists(resultFile);
            log.info("Projekt törölve: {}", project.getName());
        } catch (Exception e) {
            log.error("Projekt törlési hiba [{}]: {}", project.getName(), e.getMessage(), e);
        }
    }

    // ── Konverzió ─────────────────────────────────────────────────────────────

    private ProjectDto toDto(AnalysisProject p) {
        ProjectDto dto = new ProjectDto();
        dto.id      = p.getId();
        dto.name    = p.getName();
        dto.status  = p.getStatus();
        dto.lastRun = p.getLastRun() != null ? p.getLastRun().toString() : null;
        dto.oldDecisionPath = p.getOldDecisionFile() != null ? p.getOldDecisionFile().getAbsolutePath() : null;
        dto.newDecisionPath = p.getNewDecisionFile() != null ? p.getNewDecisionFile().getAbsolutePath() : null;
        dto.labelPath       = p.getLabelFile()       != null ? p.getLabelFile().getAbsolutePath()       : null;
        return dto;
    }

    private AnalysisProject fromDto(ProjectDto dto) {
        AnalysisProject p = new AnalysisProject(dto.name, dto.id);
        p.setStatus(dto.status != null ? dto.status : "Pending");
        if (dto.lastRun != null) {
            try { p.setLastRun(LocalDateTime.parse(dto.lastRun)); }
            catch (Exception ignored) {}
        }
        setFileIfExists(p::setOldDecisionFile, dto.oldDecisionPath);
        setFileIfExists(p::setNewDecisionFile, dto.newDecisionPath);
        setFileIfExists(p::setLabelFile,       dto.labelPath);
        return p;
    }

    private void setFileIfExists(java.util.function.Consumer<File> setter, String path) {
        if (path == null) return;
        File f = new File(path);
        if (f.exists()) setter.accept(f);
        else log.warn("Fájl nem található, kihagyva: {}", path);
    }
}