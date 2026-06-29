package hu.mrflow.labelanalyzer.viewmodel;

import hu.mrflow.labelanalyzer.config.AppConfig;
import javafx.beans.property.*;

/**
 * ViewModel a beállítások dialógushoz.
 * Minden provider API kulcsát, végpontját és modelljét külön Property tárolja,
 * hogy a Controller kétirányú bindingot kössön rájuk.
 */
public class AppSettingsViewModel extends BaseViewModel {

    private final AppConfig config = AppConfig.getInstance();

    // Aktív provider
    private final ObjectProperty<AppConfig.AiProvider> selectedProvider =
            new SimpleObjectProperty<>(config.getProvider());

    // OpenAI
    private final StringProperty openAiKey      = new SimpleStringProperty(config.getApiKey(AppConfig.AiProvider.OPENAI));
    private final StringProperty openAiEndpoint = new SimpleStringProperty(config.getEndpoint(AppConfig.AiProvider.OPENAI));
    private final StringProperty openAiModel    = new SimpleStringProperty(config.getModel(AppConfig.AiProvider.OPENAI));

    // Anthropic
    private final StringProperty anthropicKey      = new SimpleStringProperty(config.getApiKey(AppConfig.AiProvider.ANTHROPIC));
    private final StringProperty anthropicEndpoint = new SimpleStringProperty(config.getEndpoint(AppConfig.AiProvider.ANTHROPIC));
    private final StringProperty anthropicModel    = new SimpleStringProperty(config.getModel(AppConfig.AiProvider.ANTHROPIC));

    // Gemini
    private final StringProperty geminiKey      = new SimpleStringProperty(config.getApiKey(AppConfig.AiProvider.GEMINI));
    private final StringProperty geminiEndpoint = new SimpleStringProperty(config.getEndpoint(AppConfig.AiProvider.GEMINI));
    private final StringProperty geminiModel    = new SimpleStringProperty(config.getModel(AppConfig.AiProvider.GEMINI));

    // ── Mentés ───────────────────────────────────────────────────────────────

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

    // ── Property accessors ────────────────────────────────────────────────────

    public ObjectProperty<AppConfig.AiProvider> selectedProviderProperty() { return selectedProvider; }

    public StringProperty openAiKeyProperty()         { return openAiKey; }
    public StringProperty openAiEndpointProperty()    { return openAiEndpoint; }
    public StringProperty openAiModelProperty()       { return openAiModel; }

    public StringProperty anthropicKeyProperty()      { return anthropicKey; }
    public StringProperty anthropicEndpointProperty() { return anthropicEndpoint; }
    public StringProperty anthropicModelProperty()    { return anthropicModel; }

    public StringProperty geminiKeyProperty()         { return geminiKey; }
    public StringProperty geminiEndpointProperty()    { return geminiEndpoint; }
    public StringProperty geminiModelProperty()       { return geminiModel; }
}
