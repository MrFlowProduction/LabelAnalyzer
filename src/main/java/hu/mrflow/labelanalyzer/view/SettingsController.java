package hu.mrflow.labelanalyzer.view;

import hu.mrflow.labelanalyzer.config.AppConfig;
import hu.mrflow.labelanalyzer.viewmodel.AppSettingsViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private ComboBox<AppConfig.AiProvider> providerCombo;

    @FXML private TextField openAiKeyField;
    @FXML private TextField openAiEndpointField;
    @FXML private ComboBox<String> openAiModelCombo;

    @FXML private TextField anthropicKeyField;
    @FXML private TextField anthropicEndpointField;
    @FXML private ComboBox<String> anthropicModelCombo;

    @FXML private TextField geminiKeyField;
    @FXML private TextField geminiEndpointField;
    @FXML private ComboBox<String> geminiModelCombo;

    private AppSettingsViewModel viewModel;

    public void setViewModel(AppSettingsViewModel vm) {
        this.viewModel = vm;

        // Provider combo
        providerCombo.getItems().setAll(AppConfig.AiProvider.values());
        providerCombo.setButtonCell(providerCell());
        providerCombo.setCellFactory(lv -> providerCell());
        providerCombo.valueProperty().bindBidirectional(vm.selectedProviderProperty());

        // OpenAI
        openAiKeyField.textProperty().bindBidirectional(vm.openAiKeyProperty());
        openAiEndpointField.textProperty().bindBidirectional(vm.openAiEndpointProperty());
        openAiModelCombo.setItems(vm.getOpenAiModels());
        openAiModelCombo.valueProperty().bindBidirectional(vm.openAiModelProperty());

        // Anthropic
        anthropicKeyField.textProperty().bindBidirectional(vm.anthropicKeyProperty());
        anthropicEndpointField.textProperty().bindBidirectional(vm.anthropicEndpointProperty());
        anthropicModelCombo.setItems(vm.getAnthropicModels());
        anthropicModelCombo.valueProperty().bindBidirectional(vm.anthropicModelProperty());

        // Gemini
        geminiKeyField.textProperty().bindBidirectional(vm.geminiKeyProperty());
        geminiEndpointField.textProperty().bindBidirectional(vm.geminiEndpointProperty());
        geminiModelCombo.setItems(vm.getGeminiModels());
        geminiModelCombo.valueProperty().bindBidirectional(vm.geminiModelProperty());
    }

    public void save() {
        // Editable ComboBox esetén a kézzel begépelt értéket is el kell menteni
        if (openAiModelCombo.getEditor() != null) {
            vm().openAiModelProperty().set(openAiModelCombo.getEditor().getText());
        }
        if (anthropicModelCombo.getEditor() != null) {
            vm().anthropicModelProperty().set(anthropicModelCombo.getEditor().getText());
        }
        if (geminiModelCombo.getEditor() != null) {
            vm().geminiModelProperty().set(geminiModelCombo.getEditor().getText());
        }
        viewModel.save();
    }

    private AppSettingsViewModel vm() { return viewModel; }

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