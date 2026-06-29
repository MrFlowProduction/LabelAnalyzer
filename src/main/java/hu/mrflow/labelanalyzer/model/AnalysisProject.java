package hu.mrflow.labelanalyzer.model;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Represents one analysis project: three input files + optional result.
 */
public class AnalysisProject {

    private String name;
    private File oldDecisionFile;   // régi határozat (PDF or DOCX)
    private File newDecisionFile;   // új határozat (PDF or DOCX)
    private File labelFile;         // jelenlegi címkeszöveg (DOCX)
    private AnalysisResult result;
    private LocalDateTime lastRun;
    private String status = "Pending"; // Pending | Running | Done | Error

    public AnalysisProject(String name) {
        this.name = name;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public File getOldDecisionFile() { return oldDecisionFile; }
    public void setOldDecisionFile(File f) { this.oldDecisionFile = f; }

    public File getNewDecisionFile() { return newDecisionFile; }
    public void setNewDecisionFile(File f) { this.newDecisionFile = f; }

    public File getLabelFile() { return labelFile; }
    public void setLabelFile(File f) { this.labelFile = f; }

    public AnalysisResult getResult() { return result; }
    public void setResult(AnalysisResult result) { this.result = result; }

    public LocalDateTime getLastRun() { return lastRun; }
    public void setLastRun(LocalDateTime t) { this.lastRun = t; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReady() {
        return oldDecisionFile != null && newDecisionFile != null && labelFile != null;
    }

    @Override
    public String toString() { return name; }
}

