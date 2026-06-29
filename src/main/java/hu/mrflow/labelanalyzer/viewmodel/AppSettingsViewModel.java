package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.config.AppConfig;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppSettingsViewModel extends BaseViewModel {

    private final AppConfig config = AppConfig.getInstance();

    private final ObjectProperty<AppConfig.AiProvider> selectedProvider =
            new SimpleObjectProperty<>(config.getProvider());

    // OpenAI
    private final StringProperty openAiKey      = new SimpleStringProperty(config.getApiKey(AppConfig.AiProvider.OPENAI));
    private final StringProperty openAiEndpoint = new SimpleStringProperty(config.getEndpoint(AppConfig.AiProvider.OPENAI));
    private final StringProperty openAiModel    = new SimpleStringProperty(config.getModel(AppConfig.AiProvider.OPENAI));
    private final ObservableList<String> openAiModels =
            FXCollections.observableArrayList(AppConfig.AiProvider.OPENAI.availableModels);

    // Anthropic
    private final StringProperty anthropicKey      = new SimpleStringProperty(config.getApiKey(AppConfig.AiProvider.ANTHROPIC));
    private final StringProperty anthropicEndpoint = new SimpleStringProperty(config.getEndpoint(AppConfig.AiProvider.ANTHROPIC));
    private final StringProperty anthropicModel    = new SimpleStringProperty(config.getModel(AppConfig.AiProvider.ANTHROPIC));
    private final ObservableList<String> anthropicModels =
            FXCollections.observableArrayList(AppConfig.AiProvider.ANTHROPIC.availableModels);

    // Gemini
    private final StringProperty geminiKey      = new SimpleStringProperty(config.getApiKey(AppConfig.AiProvider.GEMINI));
    private final StringProperty geminiEndpoint = new SimpleStringProperty(config.getEndpoint(AppConfig.AiProvider.GEMINI));
    private final StringProperty geminiModel    = new SimpleStringProperty(config.getModel(AppConfig.AiProvider.GEMINI));
    private final ObservableList<String> geminiModels =
            FXCollections.observableArrayList(AppConfig.AiProvider.GEMINI.availableModels);

    public void save() {
        config.setProvider(selectedProvider.get());

        config.setApiKey(AppConfig.AiProvider.OPENAI,    openAiKey.get());
        config.setEndpoint(AppConfig.AiProvider.OPENAI,  openAiEndpoint.get());
        config.setModel(AppConfig.AiProvider.OPENAI,     openAiModel.get());

        config.setApiKey(AppConfig.AiProvider.ANTHROPIC,    anthropicKey.get());
        config.setEndpoint(AppConfig.AiProvider.ANTHROPIC,  anthropicEndpoint.get());
        config.setModel(AppConfig.AiProvider.ANTHROPIC,     anthropicModel.get());

        config.setApiKey(AppConfig.AiProvider.GEMINI,    geminiKey.get());
        config.setEndpoint(AppConfig.AiProvider.GEMINI,  geminiEndpoint.get());
        config.setModel(AppConfig.AiProvider.GEMINI,     geminiModel.get());

        config.save();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ObjectProperty<AppConfig.AiProvider> selectedProviderProperty() { return selectedProvider; }

    public StringProperty openAiKeyProperty()         { return openAiKey; }
    public StringProperty openAiEndpointProperty()    { return openAiEndpoint; }
    public StringProperty openAiModelProperty()       { return openAiModel; }
    public ObservableList<String> getOpenAiModels()   { return openAiModels; }

    public StringProperty anthropicKeyProperty()         { return anthropicKey; }
    public StringProperty anthropicEndpointProperty()    { return anthropicEndpoint; }
    public StringProperty anthropicModelProperty()       { return anthropicModel; }
    public ObservableList<String> getAnthropicModels()   { return anthropicModels; }

    public StringProperty geminiKeyProperty()         { return geminiKey; }
    public StringProperty geminiEndpointProperty()    { return geminiEndpoint; }
    public StringProperty geminiModelProperty()       { return geminiModel; }
    public ObservableList<String> getGeminiModels()   { return geminiModels; }
}