package hu.mrflow.labelanalyzer.model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

public class AnalysisProject {

    private final String id;
    private String name;
    private File oldDecisionFile;
    private File newDecisionFile;
    private File labelFile;
    private AnalysisResult result;
    private LocalDateTime lastRun;
    private String status = "Pending";

    public AnalysisProject(String name) {
        this.id   = UUID.randomUUID().toString();
        this.name = name;
    }

    public AnalysisProject(String name, String id) {
        this.id   = id;
        this.name = name;
    }

    public String getId()   { return id; }

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