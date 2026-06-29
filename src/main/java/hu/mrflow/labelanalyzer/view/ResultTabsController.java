package hu.mrflow.labelanalyzer.view;

import hu.mrflow.labelanalyzer.model.AnalysisResult;
import hu.mrflow.labelanalyzer.viewmodel.AnalysisResultViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Controller a ResultTabsView.fxml-hez.
 *
 * Az 5 fül összes widgetjét az AnalysisResultViewModel Property-ihez és
 * ObservableList-jeihez köti. Dinamikus tartalom (pl. figyelmeztetés TitledPane-ek)
 * itt épül fel, mert az FXML statikus – ez a "kompromisszum" a FXML + MVVM között.
 */
public class ResultTabsController {

    // ── Tab 1: Összefoglaló ───────────────────────────────────────────────────
    @FXML private Label productNameLabel;
    @FXML private Label countryLabel;
    @FXML private Label languageLabel;
    @FXML private Label oldAuthLabel;
    @FXML private Label newAuthLabel;
    @FXML private Label diffCountLabel;
    @FXML private Label changeCountLabel;
    @FXML private Label warnCountLabel;

    // ── Tab 2: Különbségek ────────────────────────────────────────────────────
    @FXML private TableView<AnalysisResult.DecisionDifference> differencesTable;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffIdCol;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffCategoryCol;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffFieldCol;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffTypeCol;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffOldCol;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffNewCol;
    @FXML private TableColumn<AnalysisResult.DecisionDifference, String> diffImpactCol;
    @FXML private TextArea differenceDetailArea;

    // ── Tab 3: Módosítások ────────────────────────────────────────────────────
    @FXML private TableView<AnalysisResult.LabelChange> changesTable;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgStatusCol;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgIdCol;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgOperationCol;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgConfidenceCol;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgSectionCol;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgPageCol;
    @FXML private TableColumn<AnalysisResult.LabelChange, String> chgReasonCol;
    @FXML private TextArea oldTextArea;
    @FXML private TextArea newTextArea;
    @FXML private Label    reviewNoteLabel;

    // ── Tab 4: Figyelmeztetések ───────────────────────────────────────────────
    @FXML private VBox warningsBox;

    // ── Tab 5: Változatlan fejezetek ──────────────────────────────────────────
    @FXML private TableView<AnalysisResult.UnchangedSection> unchangedTable;
    @FXML private TableColumn<AnalysisResult.UnchangedSection, String> unchangedSectionCol;
    @FXML private TableColumn<AnalysisResult.UnchangedSection, String> unchangedReasonCol;

    @FXML
    private void initialize() {
        differencesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        changesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        unchangedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private AnalysisResultViewModel viewModel;

    public void setViewModel(AnalysisResultViewModel vm) {
        this.viewModel = vm;

        bindSummaryTab(vm);
        bindDifferencesTab(vm);
        bindChangesTab(vm);
        bindWarningsTab(vm);
        bindUnchangedTab(vm);
    }

    // ── Tab 1 binding ─────────────────────────────────────────────────────────

    private void bindSummaryTab(AnalysisResultViewModel vm) {
        productNameLabel.textProperty().bind(vm.productNameProperty());
        countryLabel.textProperty().bind(vm.countryProperty());
        languageLabel.textProperty().bind(vm.languageProperty());
        oldAuthLabel.textProperty().bind(vm.oldAuthorizationIdProperty());
        newAuthLabel.textProperty().bind(vm.newAuthorizationIdProperty());

        diffCountLabel.textProperty().bind(vm.differenceCountProperty().asString());
        changeCountLabel.textProperty().bind(vm.labelChangeCountProperty().asString());
        warnCountLabel.textProperty().bind(vm.warningCountProperty().asString());
    }

    // ── Tab 2 binding ─────────────────────────────────────────────────────────

    private void bindDifferencesTab(AnalysisResultViewModel vm) {
        diffIdCol.setCellValueFactory(c       -> new SimpleStringProperty(nvl(c.getValue().id)));
        diffCategoryCol.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().category)));
        diffFieldCol.setCellValueFactory(c    -> new SimpleStringProperty(nvl(c.getValue().field)));
        diffTypeCol.setCellValueFactory(c     -> new SimpleStringProperty(nvl(c.getValue().changeType)));
        diffOldCol.setCellValueFactory(c      -> new SimpleStringProperty(nvl(c.getValue().oldValue)));
        diffNewCol.setCellValueFactory(c      -> new SimpleStringProperty(nvl(c.getValue().newValue)));
        diffImpactCol.setCellValueFactory(c   -> new SimpleStringProperty(nvl(c.getValue().impactSummary)));

        differencesTable.setItems(vm.getDifferences());

        // Kiválasztás → ViewModel
        differencesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> vm.selectedDifferenceProperty().set(sel));

        // ViewModel részletező szöveg → TextArea (egyirányú binding)
        differenceDetailArea.textProperty().bind(vm.differenceDetailTextProperty());
    }

    // ── Tab 3 binding ─────────────────────────────────────────────────────────

    private void bindChangesTab(AnalysisResultViewModel vm) {
        // Státusz cella: színes pont + szöveg
        chgStatusCol.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().status)));
        chgStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Circle dot = new Circle(6, statusColor(item));
                Label lbl  = new Label(" " + item);
                javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(dot, lbl);
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });

        chgIdCol.setCellValueFactory(c         -> new SimpleStringProperty(nvl(c.getValue().id)));
        chgOperationCol.setCellValueFactory(c  -> new SimpleStringProperty(nvl(c.getValue().operation)));
        chgConfidenceCol.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().confidence)));
        chgSectionCol.setCellValueFactory(c    -> new SimpleStringProperty(
                c.getValue().labelLocation != null ? nvl(c.getValue().labelLocation.section) : ""));
        chgPageCol.setCellValueFactory(c       -> new SimpleStringProperty(
                c.getValue().labelLocation != null && c.getValue().labelLocation.page != null
                        ? String.valueOf(c.getValue().labelLocation.page) : ""));
        chgReasonCol.setCellValueFactory(c     -> new SimpleStringProperty(nvl(c.getValue().changeReason)));

        changesTable.setItems(vm.getLabelChanges());

        // Kiválasztás → ViewModel
        changesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> vm.selectedLabelChangeProperty().set(sel));

        // ViewModel computed property-k → UI
        oldTextArea.textProperty().bind(vm.labelChangeOldTextProperty());
        newTextArea.textProperty().bind(vm.labelChangeNewTextProperty());
        reviewNoteLabel.textProperty().bind(vm.labelChangeReviewNoteProperty());
    }

    // ── Tab 4 binding (dinamikus) ─────────────────────────────────────────────

    private void bindWarningsTab(AnalysisResultViewModel vm) {
        // ObservableList változáskor újraépítjük a TitledPane-eket
        vm.getWarnings().addListener((javafx.collections.ListChangeListener<AnalysisResult.Warning>) change -> {
            rebuildWarnings(vm);
        });
    }

    private void rebuildWarnings(AnalysisResultViewModel vm) {
        warningsBox.getChildren().clear();
        if (vm.getWarnings().isEmpty()) {
            warningsBox.getChildren().add(new Label("Nincs figyelmeztetés."));
            return;
        }
        for (AnalysisResult.Warning w : vm.getWarnings()) {
            String affected = w.affectedChanges != null
                    ? String.join(", ", w.affectedChanges) : "–";
            TitledPane tp = new TitledPane(
                    "[" + nvl(w.severity) + "] " + nvl(w.message),
                    new Label("Érintett változások: " + affected)
            );
            tp.setCollapsible(true);
            tp.setExpanded(false);
            String styleCls = "warning-" + (w.severity != null ? w.severity.toLowerCase() : "info");
            tp.getStyleClass().add(styleCls);
            warningsBox.getChildren().add(tp);
        }
    }

    // ── Tab 5 binding ─────────────────────────────────────────────────────────

    private void bindUnchangedTab(AnalysisResultViewModel vm) {
        unchangedSectionCol.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().section)));
        unchangedReasonCol.setCellValueFactory(c  -> new SimpleStringProperty(nvl(c.getValue().reason)));
        unchangedTable.setItems(vm.getUnchangedSections());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String nvl(String s) { return s != null ? s : ""; }

    private Color statusColor(String status) {
        return switch (status != null ? status : "") {
            case "required"    -> Color.web("#e74c3c");
            case "recommended" -> Color.web("#f39c12");
            case "check"       -> Color.web("#3498db");
            default            -> Color.GRAY;
        };
    }
}
