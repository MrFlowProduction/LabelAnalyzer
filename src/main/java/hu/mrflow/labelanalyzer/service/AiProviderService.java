package hu.mrflow.labelanalyzer.service;

/**
 * Common interface for all AI provider implementations.
 * Adding a new provider = implementing this interface.
 */
public interface AiProviderService {

    /**
     * Send the prompt with document contents to the AI and return raw JSON string.
     *
     * @param systemPrompt  the regulatory expert system prompt
     * @param oldDecision   extracted text of the old határozat
     * @param newDecision   extracted text of the new határozat
     * @param labelText     extracted text of the current label (docx)
     * @return raw JSON string as returned by the AI
     * @throws Exception on network or API error
     */
    String analyze(String systemPrompt,
                   String oldDecision,
                   String newDecision,
                   String labelText) throws Exception;
}