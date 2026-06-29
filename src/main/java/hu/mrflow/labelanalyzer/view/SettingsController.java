package hu.mrflow.labelanalyzer.view;

import hu.mrflow.labelanalyzer.config.AppConfig;
import hu.mrflow.labelanalyzer.viewmodel.AppSettingsViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller a SettingsView.fxml-hez.
 * Kétirányú binding minden mezőre – a save() delegál a ViewModel-be.
 */
public class SettingsController {

    @FXML private ComboBox<AppConfig.AiProvider> providerCombo;

    // OpenAI
    @FXML private TextField openAiKeyField;
    @FXML private TextField openAiEndpointField;
    @FXML private TextField openAiModelField;

    // Anthropic
    @FXML private TextField anthropicKeyField;
    @FXML private TextField anthropicEndpointField;
    @FXML private TextField anthropicModelField;

    // Gemini
    @FXML private TextField geminiKeyField;
    @FXML private TextField geminiEndpointField;
    @FXML private TextField geminiModelField;

    private AppSettingsViewModel viewModel;

    public void setViewModel(AppSettingsViewModel vm) {
        this.viewModel = vm;

        // Provider combo
        providerCombo.getItems().setAll(AppConfig.AiProvider.values());
        providerCombo.setButtonCell(providerCell());
        providerCombo.setCellFactory(lv -> providerCell());
        providerCombo.valueProperty().bindBidirectional(vm.selectedProviderProperty());

        // Kétirányú binding minden szövegmezőre
        openAiKeyField.textProperty().bindBidirectional(vm.openAiKeyProperty());
        openAiEndpointField.textProperty().bindBidirectional(vm.openAiEndpointProperty());
        openAiModelField.textProperty().bindBidirectional(vm.openAiModelProperty());

        anthropicKeyField.textProperty().bindBidirectional(vm.anthropicKeyProperty());
        anthropicEndpointField.textProperty().bindBidirectional(vm.anthropicEndpointProperty());
        anthropicModelField.textProperty().bindBidirectional(vm.anthropicModelProperty());

        geminiKeyField.textProperty().bindBidirectional(vm.geminiKeyProperty());
        geminiEndpointField.textProperty().bindBidirectional(vm.geminiEndpointProperty());
        geminiModelField.textProperty().bindBidirectional(vm.geminiModelProperty());
    }

    /** Meghívja a ViewModel save()-jét; a MainController hívja OK gomb után. */
    public void save() {
        if (viewModel != null) viewModel.save();
    }

    private ListCell<AppConfig.AiProvider> providerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(AppConfig.AiProvider item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName);
            }
        };
    }
}
