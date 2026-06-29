package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.model.AnalysisProject;

/**
 * Gyökér ViewModel.
 *
 * Létrehozza és összeköti a gyermek ViewModel-eket:
 *   - ProjectListViewModel   → projektek listája + kiválasztás
 *   - ProjectDetailViewModel → fájlok, futtatás, progress
 *   - AnalysisResultViewModel → elemzés eredménye (megosztott a Detail és a Result nézet közt)
 *   - AppSettingsViewModel   → API beállítások
 *
 * A MainController ezt az egy objektumot kapja meg és elosztja
 * a gyermek Controller-eknek.
 */
public class MainViewModel extends BaseViewModel {

    private final AnalysisResultViewModel resultViewModel  = new AnalysisResultViewModel();
    private final ProjectDetailViewModel  detailViewModel  = new ProjectDetailViewModel(resultViewModel);
    private final ProjectListViewModel    listViewModel    = new ProjectListViewModel();
    private final AppSettingsViewModel    settingsViewModel = new AppSettingsViewModel();

    public MainViewModel() {
        // Kiválasztás változásakor töltsd be a projektet a detail VM-be
        listViewModel.selectedProjectProperty().addListener(
                (obs, old, selected) -> detailViewModel.loadProject(selected));

        // Frissítsd a lista cella megjelenítését ha a státusz változik
        // (ObservableList nem figyeli a belső mezőket automatikusan,
        //  ezért a detail VM running property-jét figyeljük)
        detailViewModel.runningProperty().addListener((obs, wasRunning, isRunning) -> {
            AnalysisProject p = listViewModel.getSelectedProject();
            if (p == null) return;
            if (isRunning) {
                p.setStatus("Running");
            } else if (!detailViewModel.errorMessageProperty().get().isBlank()) {
                p.setStatus("Error");
            } else if (p.getResult() != null) {
                p.setStatus("Done");
            }
            // ListView frissítése: replace trick
            int idx = listViewModel.getProjects().indexOf(p);
            if (idx >= 0) {
                listViewModel.getProjects().set(idx, p);
            }
        });
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ProjectListViewModel    getListViewModel()     { return listViewModel; }
    public ProjectDetailViewModel  getDetailViewModel()   { return detailViewModel; }
    public AnalysisResultViewModel getResultViewModel()   { return resultViewModel; }
    public AppSettingsViewModel    getSettingsViewModel() { return settingsViewModel; }

    @Override
    public void dispose() {
        listViewModel.dispose();
        detailViewModel.dispose();
        resultViewModel.dispose();
        settingsViewModel.dispose();
    }
}
