package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.model.AnalysisProject;
import hu.mrflow.labelanalyzer.service.AnalysisService;
import hu.mrflow.labelanalyzer.service.ProjectRepository;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ProjectDetailViewModel extends BaseViewModel {

    private final StringProperty projectName  = new SimpleStringProperty("");
    private final ObjectProperty<File> oldDecisionFile = new SimpleObjectProperty<>();
    private final ObjectProperty<File> newDecisionFile = new SimpleObjectProperty<>();
    private final ObjectProperty<File> labelFile       = new SimpleObjectProperty<>();

    private final BooleanProperty running       = new SimpleBooleanProperty(false);
    private final DoubleProperty  progress      = new SimpleDoubleProperty(0.0);
    private final StringProperty  statusMessage = new SimpleStringProperty("Kész az elemzés elindítására.");
    private final StringProperty  errorMessage  = new SimpleStringProperty("");

    private final BooleanBinding canRun;

    private final AnalysisResultViewModel resultViewModel;
    private AnalysisProject currentProject;

    private final AnalysisService   analysisService = new AnalysisService();
    private final ProjectRepository repository      = ProjectRepository.getInstance();

    // Callback a listanézet frissítéséhez mentés után
    private Runnable onProjectSaved;

    private static final Logger log = LoggerFactory.getLogger(ProjectDetailViewModel.class);

    public ProjectDetailViewModel(AnalysisResultViewModel resultViewModel) {
        this.resultViewModel = resultViewModel;
        canRun = oldDecisionFile.isNotNull()
                .and(newDecisionFile.isNotNull())
                .and(labelFile.isNotNull())
                .and(running.not());
    }

    public void setOnProjectSaved(Runnable r) { this.onProjectSaved = r; }

    // ── Projekt betöltése ─────────────────────────────────────────────────────

    public void loadProject(AnalysisProject project) {
        this.currentProject = project;
        if (project == null) {
            projectName.set("");
            oldDecisionFile.set(null);
            newDecisionFile.set(null);
            labelFile.set(null);
            resultViewModel.clear();
            return;
        }
        projectName.set(project.getName());
        oldDecisionFile.set(project.getOldDecisionFile());
        newDecisionFile.set(project.getNewDecisionFile());
        labelFile.set(project.getLabelFile());

        if (project.getResult() != null) {
            resultViewModel.loadResult(project.getResult());
        } else {
            resultViewModel.clear();
        }
    }

    // ── Fájl kiválasztás ──────────────────────────────────────────────────────

    public void selectOldDecisionFile(File f) {
        oldDecisionFile.set(f);
        if (currentProject != null) {
            currentProject.setOldDecisionFile(f);
            saveCurrentProject();
        }
    }

    public void selectNewDecisionFile(File f) {
        newDecisionFile.set(f);
        if (currentProject != null) {
            currentProject.setNewDecisionFile(f);
            saveCurrentProject();
        }
    }

    public void selectLabelFile(File f) {
        labelFile.set(f);
        if (currentProject != null) {
            currentProject.setLabelFile(f);
            saveCurrentProject();
        }
    }

    private void saveCurrentProject() {
        repository.save(currentProject);
        if (onProjectSaved != null) onProjectSaved.run();
    }

    // ── Elemzés futtatása ─────────────────────────────────────────────────────

    public void runAnalysis() {
        if (currentProject == null || !canRun.get()) return;

        running.set(true);
        progress.set(0.0);
        errorMessage.set("");
        statusMessage.set("Elemzés folyamatban…");
        currentProject.setStatus("Running");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                analysisService.runAnalysis(currentProject, (pct, msg) ->
                        Platform.runLater(() -> {
                            progress.set(pct / 100.0);
                            statusMessage.set(msg);
                        })
                );
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            running.set(false);
            progress.set(1.0);
            resultViewModel.loadResult(currentProject.getResult());
            saveCurrentProject();
        });

        task.setOnFailed(e -> {
            running.set(false);
            if(currentProject != null) currentProject.setStatus("Error");
            String msg = task.getException() != null
                    ? task.getException().getMessage() : "Ismeretlen hiba";
            errorMessage.set(msg);
            statusMessage.set("Hiba: " + msg);
            log.error(msg);

        });

        Thread t = new Thread(task, "analysis-thread");
        t.setDaemon(true);
        t.start();
    }

    // ── Property accessors ────────────────────────────────────────────────────

    public StringProperty  projectNameProperty()          { return projectName; }
    public ObjectProperty<File> oldDecisionFileProperty() { return oldDecisionFile; }
    public ObjectProperty<File> newDecisionFileProperty() { return newDecisionFile; }
    public ObjectProperty<File> labelFileProperty()       { return labelFile; }
    public BooleanProperty  runningProperty()             { return running; }
    public DoubleProperty   progressProperty()            { return progress; }
    public StringProperty   statusMessageProperty()       { return statusMessage; }
    public StringProperty   errorMessageProperty()        { return errorMessage; }
    public BooleanBinding   canRunProperty()              { return canRun; }
}