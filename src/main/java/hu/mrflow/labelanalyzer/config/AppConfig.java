package hu.mrflow.labelanalyzer.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Persistent configuration for API keys and provider settings.
 * Stored in user home directory: ~/.labelanalyzer/config.properties
 */
public class AppConfig {

    public enum AiProvider {
        OPENAI("OpenAI (ChatGPT)", "https://api.openai.com/v1/chat/completions", "gpt-4o"),
        ANTHROPIC("Anthropic (Claude)", "https://api.anthropic.com/v1/messages", "claude-opus-4-6"),
        GEMINI("Google (Gemini)", "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent", "gemini-1.5-pro");

        public final String displayName;
        public final String defaultEndpoint;
        public final String defaultModel;

        AiProvider(String displayName, String defaultEndpoint, String defaultModel) {
            this.displayName = displayName;
            this.defaultEndpoint = defaultEndpoint;
            this.defaultModel = defaultModel;
        }
    }

    private static final Path CONFIG_DIR  = Path.of(System.getProperty("user.home"), ".labelanalyzer");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");

    private static AppConfig instance;
    private final Properties props = new Properties();

    private AppConfig() {
        load();
    }

    public static AppConfig getInstance() {
        if (instance == null) instance = new AppConfig();
        return instance;
    }

    // ── Provider ──────────────────────────────────────────────────────────────

    public AiProvider getProvider() {
        String name = props.getProperty("provider", AiProvider.OPENAI.name());
        try { return AiProvider.valueOf(name); }
        catch (IllegalArgumentException e) { return AiProvider.OPENAI; }
    }

    public void setProvider(AiProvider provider) {
        props.setProperty("provider", provider.name());
    }

    // ── Per-provider API keys ─────────────────────────────────────────────────

    public String getApiKey(AiProvider provider) {
        return props.getProperty("apikey." + provider.name(), "");
    }

    public void setApiKey(AiProvider provider, String key) {
        props.setProperty("apikey." + provider.name(), key);
    }

    /** Convenience: key for the currently selected provider */
    public String getCurrentApiKey() {
        return getApiKey(getProvider());
    }

    // ── Per-provider endpoints & models ──────────────────────────────────────

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

    // ── Persist ───────────────────────────────────────────────────────────────

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

