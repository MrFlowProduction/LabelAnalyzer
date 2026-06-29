package hu.mrflow.labelanalyzer.config;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AppConfig {

    public enum AiProvider {
        OPENAI("OpenAI (ChatGPT)",
                "https://api.openai.com/v1/chat/completions",
                "gpt-5-mini",
                List.of(
                        "gpt-5",
                        "gpt-5-mini",
                        "gpt-5-nano",
                        "gpt-4.1",
                        "gpt-4.1-mini",
                        "gpt-4o",
                        "gpt-4o-mini",
                        "gpt-4-turbo",
                        "gpt-4",
                        "gpt-3.5-turbo"
                )),
        ANTHROPIC("Anthropic (Claude)",
                "https://api.anthropic.com/v1/messages",
                "claude-opus-4-6",
                List.of("claude-opus-4-6", "claude-sonnet-4-6", "claude-haiku-4-5-20251001", "claude-opus-4-8", "claude-opus-4-7")),
        GEMINI("Google (Gemini)",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent",
                "gemini-1.5-pro",
                List.of("gemini-1.5-pro", "gemini-1.5-flash", "gemini-2.0-flash", "gemini-2.5-pro"));

        public final String displayName;
        public final String defaultEndpoint;
        public final String defaultModel;
        public final List<String> availableModels;

        AiProvider(String displayName, String defaultEndpoint, String defaultModel, List<String> availableModels) {
            this.displayName     = displayName;
            this.defaultEndpoint = defaultEndpoint;
            this.defaultModel    = defaultModel;
            this.availableModels = availableModels;
        }
    }

    private static final Path CONFIG_DIR  = Path.of(System.getProperty("user.home"), ".labelanalyzer");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");

    private static AppConfig instance;
    private final Properties props = new Properties();

    private AppConfig() { load(); }

    public static AppConfig getInstance() {
        if (instance == null) instance = new AppConfig();
        return instance;
    }

    public AiProvider getProvider() {
        String name = props.getProperty("provider", AiProvider.OPENAI.name());
        try { return AiProvider.valueOf(name); }
        catch (IllegalArgumentException e) { return AiProvider.OPENAI; }
    }

    public void setProvider(AiProvider provider) {
        props.setProperty("provider", provider.name());
    }

    public String getApiKey(AiProvider provider) {
        return props.getProperty("apikey." + provider.name(), "");
    }

    public void setApiKey(AiProvider provider, String key) {
        props.setProperty("apikey." + provider.name(), key);
    }

    public String getCurrentApiKey() { return getApiKey(getProvider()); }

    public String getEndpoint(AiProvider provider) {
        return props.getProperty("endpoint." + provider.name(), provider.defaultEndpoint);
    }

    public void setEndpoint(AiProvider provider, String endpoint) {
        props.setProperty("endpoint." + provider.name(), endpoint);
    }

    public String getModel(AiProvider provider) {
        return props.getProperty("model." + provider.name(), provider.defaultModel);
    }

    public void setModel(AiProvider provider, String model) {
        props.setProperty("model." + provider.name(), model);
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
                props.store(out, "LabelAnalyzer Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        }
    }
}