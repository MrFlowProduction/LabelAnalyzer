package hu.mrflow.labelanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.mrflow.labelanalyzer.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * AI provider implementation for OpenAI (ChatGPT).
 */
public class OpenAiProviderService implements AiProviderService {

    private final AppConfig config = AppConfig.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(OpenAiProviderService.class);


    @Override
    public String analyze(String systemPrompt,
                          String oldDecision,
                          String newDecision,
                          String labelText) throws Exception {

        log.info("Start Analyze by OpenAI");

        AppConfig.AiProvider provider = AppConfig.AiProvider.OPENAI;
        String apiKey   = config.getApiKey(provider);
        String endpoint = config.getEndpoint(provider);
        String model    = config.getModel(provider);

        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI API key is not configured.");
            throw new IllegalStateException("OpenAI API key is not configured.");
        }

        String userContent = buildUserContent(oldDecision, newDecision, labelText);

        // Build request body
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        //requestBody.put("temperature", 0);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userContent)
        ));

        String json = mapper.writeValueAsString(requestBody);
        log.debug("Request JSON for OPEN AI: " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("OpenAI API error " + response.statusCode() + ": " + response.body());
            throw new RuntimeException("OpenAI API error " + response.statusCode() + ": " + response.body());
        }

        // Extract content from response
        var responseJson = mapper.readTree(response.body());
        log.debug("Response JSON from OpenAI: " + responseJson);
        return responseJson
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();
    }

    private String buildUserContent(String oldDecision, String newDecision, String labelText) {
        return """
            === RÉGI HATÁROZAT ===
            %s
            
            === ÚJ HATÁROZAT ===
            %s
            
            === JELENLEGI CÍMKESZÖVEG ===
            %s
            """.formatted(oldDecision, newDecision, labelText);
    }
}
