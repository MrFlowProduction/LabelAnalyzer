package hu.mrflow.labelanalyzer.view;

import hu.mrflow.labelanalyzer.viewmodel.AnalysisResultViewModel;
import hu.mrflow.labelanalyzer.viewmodel.ProjectDetailViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller a ProjectDetailView.fxml-hez.
 *
 * Minden állapot a ViewModel-ben van; ez a class csak:
 *   - bekötöttségeket (bind) végez az initialize-ban
 *   - fájlböngészőt nyit és a kiválasztott fájlt átadja a VM-nek
 *   - az Elemzés futtatása gombot delegálja a VM-nek
 */
public class ProjectDetailController {

    @FXML private Label       projectNameLabel;
    @FXML private Label       oldDecisionLabel;
    @FXML private Label       newDecisionLabel;
    @FXML private Label       labelFileLabel;
    @FXML private Button      runButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label       statusLabel;

    // A beágyazott ResultTabsController-t az FXML <fx:include> adja be
    @FXML private ResultTabsController resultTabsController;

    private ProjectDetailViewModel detailViewModel;
    private AnalysisResultViewModel resultViewModel;

    public void setViewModels(ProjectDetailViewModel detailVm, AnalysisResultViewModel resultVm) {
        this.detailViewModel = detailVm;
        this.resultViewModel = resultVm;

        // ── Egyirányú bindingok (ViewModel → UI) ──────────────────────────
        projectNameLabel.textProperty().bind(detailVm.projectNameProperty());

        // Dupla kattintás a fejléc nevére → inline átnevezés
        projectNameLabel.setTooltip(new Tooltip("Dupla kattintás az átnevezéshez"));
        projectNameLabel.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                onRenameProject();
            }
        });

        // Fájl label: fájlnév vagy üzenet
        oldDecisionLabel.textProperty().bind(
                fileNameBinding(detailVm.oldDecisionFileProperty()));
        newDecisionLabel.textProperty().bind(
                fileNameBinding(detailVm.newDecisionFileProperty()));
        labelFileLabel.textProperty().bind(
                fileNameBinding(detailVm.labelFileProperty()));

        // Futtatás gomb
        runButton.disableProperty().bind(detailVm.canRunProperty().not());

        // Progress
        progressBar.progressProperty().bind(detailVm.progressProperty());
        progressBar.visibleProperty().bind(detailVm.runningProperty());

        statusLabel.textProperty().bind(detailVm.statusMessageProperty());

        // Eredmény tabek frissítése
        resultTabsController.setViewModel(resultVm);
    }

    // ── Fájlböngészők ─────────────────────────────────────────────────────────

    @FXML
    private void onBrowseOldDecision() {
        File f = browse("Régi határozat kiválasztása");
        if (f != null) detailViewModel.selectOldDecisionFile(f);
    }

    @FXML
    private void onBrowseNewDecision() {
        File f = browse("Új határozat kiválasztása");
        if (f != null) detailViewModel.selectNewDecisionFile(f);
    }

    @FXML
    private void onBrowseLabel() {
        File f = browse("Jelenlegi címke kiválasztása");
        if (f != null) detailViewModel.selectLabelFile(f);
    }

    @FXML
    private void onRunAnalysis() {
        detailViewModel.runAnalysis();
    }

    private void onRenameProject() {
        TextInputDialog dialog = new TextInputDialog(detailViewModel.projectNameProperty().get());
        dialog.setTitle("Átnevezés");
        dialog.setHeaderText(null);
        dialog.setContentText("Projekt neve:");
        dialog.showAndWait().ifPresent(detailViewModel::renameCurrentProject);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private File browse(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Dokumentumok", "*.pdf", "*.docx", "*.doc"),
                new FileChooser.ExtensionFilter("Minden fájl", "*.*")
        );
        return chooser.showOpenDialog(runButton.getScene().getWindow());
    }

    /**
     * Létrehoz egy binding-ot, ami egy File property-ből a fájlnevet,
     * null esetén "Nincs fájl kiválasztva" szöveget ad vissza.
     */
    private javafx.beans.binding.StringBinding fileNameBinding(
            javafx.beans.property.ObjectProperty<File> prop) {
        return new javafx.beans.binding.StringBinding() {
            { bind(prop); }
            @Override
            protected String computeValue() {
                File f = prop.get();
                return f != null ? f.getName() : "Nincs fájl kiválasztva";
            }
        };
    }
}
