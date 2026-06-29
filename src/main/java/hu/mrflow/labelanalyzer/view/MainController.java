package hu.mrflow.labelanalyzer.view;

import hu.mrflow.labelanalyzer.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Controller a MainView.fxml-hez.
 *
 * Felelőssége:
 *  - MainViewModel létrehozása és szétosztása a gyermek controllerek közt
 *  - Menüesemények kezelése (new project, settings, exit, about)
 *
 * Semmilyen UI logikát NEM tartalmaz – az a ViewModel-ben él.
 */
public class MainController {

    // A gyermek controllereket az FXML fx:include injekcióval adja át,
    // a mező neve: <fx:id>Controller konvenció szerint.
    @FXML private ProjectListController   projectListController;
    @FXML private ProjectDetailController projectDetailController;

    private MainViewModel mainViewModel;

    @FXML
    private void initialize() {
        mainViewModel = new MainViewModel();
        projectListController.setViewModel(mainViewModel.getListViewModel());
        projectDetailController.setViewModels(
                mainViewModel.getDetailViewModel(),
                mainViewModel.getResultViewModel()
        );
    }

    // ── Menüesemények ─────────────────────────────────────────────────────────

    @FXML
    private void onNewProject() {
        mainViewModel.getListViewModel().addNewProject();
    }

    @FXML
    private void onSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("SettingsView.fxml"));
            DialogPane pane = loader.load();
            SettingsController ctrl = loader.getController();
            ctrl.setViewModel(mainViewModel.getSettingsViewModel());

            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Beállítások");
            dialog.initOwner(getWindow());
            dialog.setResultConverter(bt -> {
                if (bt == javafx.scene.control.ButtonType.OK) ctrl.save();
                return null;
            });
            dialog.showAndWait();
        } catch (IOException e) {
            showError("Nem sikerült megnyitni a beállítások ablakot:\n" + e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        mainViewModel.dispose();
        getWindow().hide();
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Névjegy");
        alert.setHeaderText("LabelAnalyzer v2.0");
        alert.setContentText("""
            Növényvédő szer engedélyhatározat-összehasonlító
            és címkemódosítás-elemző rendszer.

            Architektúra: JavaFX · FXML · MVVM

            Támogatott AI szolgáltatók:
              • OpenAI (ChatGPT)
              • Anthropic (Claude)
              • Google (Gemini)
            """);
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Window getWindow() {
        // Bármely injektált controller scene-jéből előkerítjük az ablakot
        return projectListController.getRoot().getScene().getWindow();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText("Hiba");
        a.showAndWait();
    }
}
