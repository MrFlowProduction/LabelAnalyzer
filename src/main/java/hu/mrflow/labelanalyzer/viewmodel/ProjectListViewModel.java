package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.model.AnalysisProject;
import hu.mrflow.labelanalyzer.service.ProjectRepository;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ProjectListViewModel extends BaseViewModel {

    private final ObservableList<AnalysisProject> projects =
            FXCollections.observableArrayList();

    private final ObjectProperty<AnalysisProject> selectedProject =
            new SimpleObjectProperty<>();

    private final ProjectRepository repository = ProjectRepository.getInstance();

    public ProjectListViewModel() {
        // Mentett projektek betöltése
        List<AnalysisProject> saved = repository.loadAll();
        if (saved.isEmpty()) {
            addNewProject();
        } else {
            projects.addAll(saved);
            selectedProject.set(projects.get(0));
        }
    }

    // ── Műveletek ─────────────────────────────────────────────────────────────

    public void addNewProject() {
        AnalysisProject p = new AnalysisProject("Projekt " + (projects.size() + 1));
        projects.add(p);
        selectedProject.set(p);
        repository.save(p);
    }

    public void removeProject(AnalysisProject project) {
        if (project == null) return;
        int idx = projects.indexOf(project);
        projects.remove(project);
        repository.delete(project);
        if (!projects.isEmpty()) {
            selectedProject.set(projects.get(Math.min(idx, projects.size() - 1)));
        } else {
            selectedProject.set(null);
        }
    }

    public void removeSelected() {
        removeProject(selectedProject.get());
    }

    public void renameProject(AnalysisProject project, String newName) {
        if (project == null || newName == null || newName.isBlank()) return;
        project.setName(newName);
        repository.save(project);
        // ListView frissítése: replace trick
        int idx = projects.indexOf(project);
        if (idx >= 0) projects.set(idx, project);
    }

    /** Projektet mentjük (fájlváltozás, elemzés vége után hívandó) */
    public void saveProject(AnalysisProject project) {
        if (project != null) repository.save(project);
    }

    // ── Property accessors ────────────────────────────────────────────────────

    public ObservableList<AnalysisProject> getProjects() { return projects; }

    public ObjectProperty<AnalysisProject> selectedProjectProperty() { return selectedProject; }
    public AnalysisProject getSelectedProject() { return selectedProject.get(); }
}