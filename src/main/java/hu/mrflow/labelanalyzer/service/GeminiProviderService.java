package hu.mrflow.labelanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.mrflow.labelanalyzer.config.AppConfig;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * AI provider implementation for Google Gemini.
 */
public class GeminiProviderService implements AiProviderService {

    private final AppConfig config = AppConfig.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String analyze(String systemPrompt,
                          String oldDecision,
                          String newDecision,
                          String labelText) throws Exception {

        AppConfig.AiProvider provider = AppConfig.AiProvider.GEMINI;
        String apiKey   = config.getApiKey(provider);
        String endpoint = config.getEndpoint(provider);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String userContent = buildUserContent(systemPrompt, oldDecision, newDecision, labelText);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", userContent)))
        ));
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0
        ));

        String json = mapper.writeValueAsString(requestBody);

        // Gemini uses API key as query param
        String url = endpoint + "?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error " + response.statusCode() + ": " + response.body());
        }

        var responseJson = mapper.readTree(response.body());
        return responseJson
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();
    }

    private String buildUserContent(String systemPrompt,
                                    String oldDecision,
                                    String newDecision,
                                    String labelText) {
        // Gemini doesn't have a separate system field in basic API, so we prepend it
        return systemPrompt + "\n\n" +
                """
                === RÉGI HATÁROZAT ===
                %s
                
                === ÚJ HATÁROZAT ===
                %s
                
                === JELENLEGI CÍMKESZÖVEG ===
                %s
                """.formatted(oldDecision, newDecision, labelText);
    }
}
