package hu.mrflow.labelanalyzer.view;


import hu.mrflow.labelanalyzer.model.AnalysisProject;
import hu.mrflow.labelanalyzer.viewmodel.ProjectListViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Controller a ProjectListView.fxml-hez.
 * Minimális kód: binding + cella gyár + event delegation.
 */
public class ProjectListController {

    @FXML private VBox     root;
    @FXML private ListView<AnalysisProject> projectListView;
    @FXML private Button   addProjectButton;
    @FXML private Button   removeProjectButton;

    private ProjectListViewModel viewModel;

    public void setViewModel(ProjectListViewModel vm) {
        this.viewModel = vm;

        // Lista adatkötés
        projectListView.setItems(vm.getProjects());
        projectListView.setCellFactory(lv -> new ProjectCell());

        // Kiválasztás kétirányú kötés
        projectListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> vm.selectedProjectProperty().set(sel));

        vm.selectedProjectProperty().addListener(
                (obs, old, sel) -> {
                    if (sel != projectListView.getSelectionModel().getSelectedItem()) {
                        projectListView.getSelectionModel().select(sel);
                    }
                });

        // Törlés gomb csak ha van kiválasztás
        removeProjectButton.disableProperty().bind(
                vm.selectedProjectProperty().isNull());
    }

    @FXML
    private void onAddProject() {
        viewModel.addNewProject();
    }

    @FXML
    private void onRemoveProject() {
        viewModel.removeSelected();
    }

    public VBox getRoot() { return root; }

    // ── Cella ─────────────────────────────────────────────────────────────────

    private static class ProjectCell extends ListCell<AnalysisProject> {
        @Override
        protected void updateItem(AnalysisProject item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }

            Label name   = new Label(item.getName());
            name.getStyleClass().add("project-name");

            Label status = new Label(statusIcon(item.getStatus()) + " " + item.getStatus());
            status.getStyleClass().add("project-status");

            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(2, name, status);
            setGraphic(box);
            setText(null);
        }

        private String statusIcon(String s) {
            return switch (s != null ? s : "") {
                case "Done"    -> "✔";
                case "Running" -> "⏳";
                case "Error"   -> "✖";
                default        -> "○";
            };
        }
    }
}
