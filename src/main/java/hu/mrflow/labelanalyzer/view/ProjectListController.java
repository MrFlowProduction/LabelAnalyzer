package hu.mrflow.labelanalyzer.view;

import hu.mrflow.labelanalyzer.model.AnalysisProject;
import hu.mrflow.labelanalyzer.viewmodel.ProjectListViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class ProjectListController {

    @FXML private VBox     root;
    @FXML private ListView<AnalysisProject> projectListView;
    @FXML private Button   addProjectButton;
    @FXML private Button   removeProjectButton;

    private ProjectListViewModel viewModel;

    public void setViewModel(ProjectListViewModel vm) {
        this.viewModel = vm;

        projectListView.setItems(vm.getProjects());
        projectListView.setCellFactory(lv -> new ProjectCell(vm));

        // Lista → ViewModel kiválasztás
        projectListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> vm.selectedProjectProperty().set(sel));

        // ViewModel → lista kiválasztás
        vm.selectedProjectProperty().addListener((obs, old, sel) -> {
            if (sel != projectListView.getSelectionModel().getSelectedItem()) {
                projectListView.getSelectionModel().select(sel);
            }
        });

        removeProjectButton.disableProperty().bind(vm.selectedProjectProperty().isNull());
    }

    @FXML private void onAddProject()    { viewModel.addNewProject(); }
    @FXML private void onRemoveProject() { viewModel.removeSelected(); }

    public VBox getRoot() { return root; }

    // ── Cella dupla kattintásra névszerkesztéssel ─────────────────────────────

    private static class ProjectCell extends ListCell<AnalysisProject> {

        private final ProjectListViewModel vm;

        ProjectCell(ProjectListViewModel vm) {
            this.vm = vm;

            // Dupla kattintás → inline névszerkesztés
            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY
                        && e.getClickCount() == 2
                        && getItem() != null) {
                    startRename(getItem());
                }
            });
        }

        @Override
        protected void updateItem(AnalysisProject item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }

            Label name   = new Label(item.getName());
            name.getStyleClass().add("project-name");

            Label status = new Label(statusIcon(item.getStatus()) + " " + item.getStatus());
            status.getStyleClass().add("project-status");

            VBox box = new VBox(2, name, status);
            setGraphic(box);
            setText(null);
        }

        private void startRename(AnalysisProject project) {
            TextInputDialog dialog = new TextInputDialog(project.getName());
            dialog.setTitle("Átnevezés");
            dialog.setHeaderText(null);
            dialog.setContentText("Projekt neve:");
            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.isBlank()) {
                    vm.renameProject(project, newName.trim());
                }
            });
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