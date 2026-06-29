package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.model.AnalysisResult;
import javafx.beans.property.*;
import javafx.collections.*;

/**
 * ViewModel az elemzési eredmény megjelenítéséhez.
 *
 * Az AI-tól kapott AnalysisResult-ot Observable listákká és Property-kké
 * alakítja, amelyekre a Controller-ek és az FXML bindingok közvetlenül köthetők.
 *
 * Szűrési logika (pl. csak "required" módosítások) is ide kerülhet.
 */
public class AnalysisResultViewModel extends BaseViewModel {

    // Projekt meta
    private final StringProperty productName        = new SimpleStringProperty("");
    private final StringProperty country            = new SimpleStringProperty("");
    private final StringProperty language           = new SimpleStringProperty("");
    private final StringProperty oldAuthorizationId = new SimpleStringProperty("");
    private final StringProperty newAuthorizationId = new SimpleStringProperty("");

    // Számlálók (summary tab)
    private final IntegerProperty differenceCount  = new SimpleIntegerProperty(0);
    private final IntegerProperty labelChangeCount = new SimpleIntegerProperty(0);
    private final IntegerProperty warningCount     = new SimpleIntegerProperty(0);

    // Listák
    private final ObservableList<AnalysisResult.DecisionDifference> differences =
            FXCollections.observableArrayList();

    private final ObservableList<AnalysisResult.LabelChange> labelChanges =
            FXCollections.observableArrayList();

    private final ObservableList<AnalysisResult.Warning> warnings =
            FXCollections.observableArrayList();

    private final ObservableList<AnalysisResult.UnchangedSection> unchangedSections =
            FXCollections.observableArrayList();

    // Kiválasztott elemek részletező panelekhez
    private final ObjectProperty<AnalysisResult.DecisionDifference> selectedDifference =
            new SimpleObjectProperty<>();

    private final ObjectProperty<AnalysisResult.LabelChange> selectedLabelChange =
            new SimpleObjectProperty<>();

    // Részletező szövegek (computed a kiválasztásból)
    private final StringProperty differenceDetailText  = new SimpleStringProperty("");
    private final StringProperty labelChangeOldText    = new SimpleStringProperty("");
    private final StringProperty labelChangeNewText    = new SimpleStringProperty("");
    private final StringProperty labelChangeReviewNote = new SimpleStringProperty("");

    // Van-e betöltött eredmény
    private final BooleanProperty hasResult = new SimpleBooleanProperty(false);

    public AnalysisResultViewModel() {
        // Kiválasztott határozat-különbség → részletező szöveg
        selectedDifference.addListener((obs, old, sel) -> {
            if (sel == null) {
                differenceDetailText.set("");
                return;
            }
            differenceDetailText.set("""
                Kategória: %s
                Mező: %s
                Változás típusa: %s

                Régi érték:
                %s

                Új érték:
                %s

                Hatás összefoglalója:
                %s

                Forráshivatkozás (régi, %d. oldal): %s
                Forráshivatkozás (új, %d. oldal):  %s
                """.formatted(
                    nvl(sel.category), nvl(sel.field), nvl(sel.changeType),
                    nvl(sel.oldValue), nvl(sel.newValue), nvl(sel.impactSummary),
                    sel.sourceOld != null && sel.sourceOld.page != null ? sel.sourceOld.page : 0,
                    sel.sourceOld != null ? nvl(sel.sourceOld.quote) : "",
                    sel.sourceNew != null && sel.sourceNew.page != null ? sel.sourceNew.page : 0,
                    sel.sourceNew != null ? nvl(sel.sourceNew.quote) : ""
            ));
        });

        // Kiválasztott címkemódosítás → régi/új szöveg + megjegyzés
        selectedLabelChange.addListener((obs, old, sel) -> {
            if (sel == null) {
                labelChangeOldText.set("");
                labelChangeNewText.set("");
                labelChangeReviewNote.set("");
                return;
            }
            labelChangeOldText.set(sel.oldText != null ? sel.oldText : "(nincs régi szöveg)");
            labelChangeNewText.set(sel.newText != null ? sel.newText : "(nincs új szöveg)");
            labelChangeReviewNote.set(sel.reviewNote != null ? "⚠ " + sel.reviewNote : "");
        });
    }

    // ── Adatbetöltés ─────────────────────────────────────────────────────────

    public void loadResult(AnalysisResult result) {
        if (result == null) { clear(); return; }

        if (result.project != null) {
            productName.set(nvl(result.project.productName));
            country.set(nvl(result.project.country));
            language.set(nvl(result.project.language));
            oldAuthorizationId.set(nvl(result.project.oldAuthorizationId));
            newAuthorizationId.set(nvl(result.project.newAuthorizationId));
        }

        differences.setAll(result.decisionDifferences != null
                ? result.decisionDifferences : java.util.List.of());

        labelChanges.setAll(result.labelChanges != null
                ? result.labelChanges : java.util.List.of());

        warnings.setAll(result.warnings != null
                ? result.warnings : java.util.List.of());

        unchangedSections.setAll(result.unchangedSections != null
                ? result.unchangedSections : java.util.List.of());

        differenceCount.set(differences.size());
        labelChangeCount.set(labelChanges.size());
        warningCount.set(warnings.size());

        hasResult.set(true);
    }

    public void clear() {
        productName.set(""); country.set(""); language.set("");
        oldAuthorizationId.set(""); newAuthorizationId.set("");
        differences.clear(); labelChanges.clear();
        warnings.clear(); unchangedSections.clear();
        differenceCount.set(0); labelChangeCount.set(0); warningCount.set(0);
        selectedDifference.set(null); selectedLabelChange.set(null);
        hasResult.set(false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String nvl(String s) { return s != null ? s : ""; }

    // ── Property accessors ────────────────────────────────────────────────────

    public StringProperty productNameProperty()        { return productName; }
    public StringProperty countryProperty()            { return country; }
    public StringProperty languageProperty()           { return language; }
    public StringProperty oldAuthorizationIdProperty() { return oldAuthorizationId; }
    public StringProperty newAuthorizationIdProperty() { return newAuthorizationId; }

    public IntegerProperty differenceCountProperty()   { return differenceCount; }
    public IntegerProperty labelChangeCountProperty()  { return labelChangeCount; }
    public IntegerProperty warningCountProperty()      { return warningCount; }

    public ObservableList<AnalysisResult.DecisionDifference> getDifferences()    { return differences; }
    public ObservableList<AnalysisResult.LabelChange>        getLabelChanges()    { return labelChanges; }
    public ObservableList<AnalysisResult.Warning>            getWarnings()        { return warnings; }
    public ObservableList<AnalysisResult.UnchangedSection>   getUnchangedSections(){ return unchangedSections; }

    public ObjectProperty<AnalysisResult.DecisionDifference> selectedDifferenceProperty() { return selectedDifference; }
    public ObjectProperty<AnalysisResult.LabelChange>        selectedLabelChangeProperty() { return selectedLabelChange; }

    public StringProperty differenceDetailTextProperty()  { return differenceDetailText; }
    public StringProperty labelChangeOldTextProperty()    { return labelChangeOldText; }
    public StringProperty labelChangeNewTextProperty()    { return labelChangeNewText; }
    public StringProperty labelChangeReviewNoteProperty() { return labelChangeReviewNote; }

    public BooleanProperty hasResultProperty() { return hasResult; }
}
