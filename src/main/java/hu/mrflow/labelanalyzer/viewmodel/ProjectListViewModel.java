package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.model.AnalysisProject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel a bal oldali projektlistához.
 *
 * Felelős:
 *  - a projektek ObservableList-jéért
 *  - a kiválasztott projekt Property-jéért
 *  - projekt hozzáadás / törlés műveletekért
 *
 * A kiválasztás változásakor értesíti a ProjectDetailViewModel-t
 * (közvetlen referencia helyett a MainViewModel közvetít).
 */
public class ProjectListViewModel extends BaseViewModel {

    private final ObservableList<AnalysisProject> projects =
            FXCollections.observableArrayList();

    private final ObjectProperty<AnalysisProject> selectedProject =
            new SimpleObjectProperty<>();

    public ProjectListViewModel() {
        // Induláskor egy üres projekt
        addNewProject();
    }

    // ── Műveletek ─────────────────────────────────────────────────────────────

    public void addNewProject() {
        AnalysisProject p = new AnalysisProject("Projekt " + (projects.size() + 1));
        projects.add(p);
        selectedProject.set(p);
    }

    public void removeProject(AnalysisProject project) {
        if (project == null) return;
        int idx = projects.indexOf(project);
        projects.remove(project);
        if (!projects.isEmpty()) {
            int nextIdx = Math.min(idx, projects.size() - 1);
            selectedProject.set(projects.get(nextIdx));
        } else {
            selectedProject.set(null);
        }
    }

    public void removeSelected() {
        removeProject(selectedProject.get());
    }

    // ── Property accessors ────────────────────────────────────────────────────

    public ObservableList<AnalysisProject> getProjects() { return projects; }

    public ObjectProperty<AnalysisProject> selectedProjectProperty() { return selectedProject; }
    public AnalysisProject getSelectedProject() { return selectedProject.get(); }
}
