package hu.mrflow.labelanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.mrflow.labelanalyzer.config.AppConfig;
import hu.mrflow.labelanalyzer.model.AnalysisProject;
import hu.mrflow.labelanalyzer.model.AnalysisResult;

import java.time.LocalDateTime;

/**
 * Orchestrates a full analysis run:
 * 1. Extract text from the three files
 * 2. Call the selected AI provider
 * 3. Parse the JSON result
 * 4. Update the project with the result
 */
public class AnalysisService {

    // The system prompt from the prompt document
    private static final String SYSTEM_PROMPT = """
        # Szerepkör

        Te egy növényvédős regulatory szakértő vagy.

        Feladatod három dokumentum összehasonlítása:

        - Régi szükséghelyzeti engedély
        - Új szükséghelyzeti engedély
        - Jelenlegi címkeszöveg

        A cél nem egyszerű összefoglaló készítése.

        A cél egy strukturált módosítási lista előállítása, amely alapján a címke automatikusan módosítható.

        # Feladat

        1.
        Hasonlítsd össze a két határozatot.
        Határozd meg az összes érdemi különbséget.
        Csak a tartalmi különbségeket vizsgáld.
        Ne sorold fel azokat a részeket, amelyek kizárólag dátumban vagy iktatószámban különböznek, kivéve ha azok címkét érintenek.

        2.
        Vizsgáld meg a jelenlegi címkeszöveget.
        Határozd meg, hogy a határozatváltozások miatt
        - mit kell törölni,
        - mit kell cserélni,
        - mit kell beszúrni,
        - mit kell módosítani.
        Minden módosítás konkrét szöveggel történjen (old_text / new_text formátumban).

        3.
        Az eredmény KIZÁRÓLAG JSON lehet.
        Ne írj magyarázatot. Ne használj markdown-t. Ne írj bevezetőt.

        # JSON séma

        {
          "schema_version": "1.0",
          "project": {
            "product_name": "",
            "country": "",
            "language": "",
            "old_authorization_id": "",
            "new_authorization_id": ""
          },
          "decision_differences": [
            {
              "id": "",
              "category": "",
              "field": "",
              "change_type": "",
              "old_value": "",
              "new_value": "",
              "impact_summary": "",
              "label_relevance": "",
              "source_old": { "page": 0, "quote": "" },
              "source_new": { "page": 0, "quote": "" }
            }
          ],
          "label_changes": [
            {
              "id": "",
              "operation": "",
              "status": "",
              "label_location": {
                "page": 0,
                "section": "",
                "paragraph_anchor": "",
                "table_name": "",
                "row_identifier": ""
              },
              "old_text": "",
              "new_text": "",
              "change_reason": "",
              "linked_decision_difference_ids": [],
              "source_new_decision": { "page": 0, "quote": "" },
              "confidence": "",
              "review_note": ""
            }
          ],
          "unchanged_sections": [
            { "section": "", "reason": "" }
          ],
          "warnings": [
            { "severity": "", "message": "", "affected_changes": [] }
          ]
        }

        Megengedett operation értékek: replace, delete, insert_before, insert_after, update_table_row, add_table_row, delete_table_row
        Megengedett status értékek: required, recommended, check
        Megengedett confidence értékek: high, medium, low
        Megengedett category értékek: scope, period, technology_instruction, authorized_crop, dose, application_method, waiting_period, safety_phrase, classification, administrative, packaging, manufacturer, other

        Fontos szabályok:
        - Mindig ugyanazt a JSON struktúrát használd.
        - Hiányzó érték esetén null szerepeljen.
        - Ne hagyj ki mezőket.
        - Ne találj ki információt.
        - Minden label_changes elemhez tartozzon legalább egy decision_difference.
        - Minden módosításnál idézd a megfelelő szövegrészt.
        - Ha ugyanaz a módosítás több helyen szerepel a címkében, minden előfordulás külön label_changes objektum legyen.
        - Az operation mindig a legkisebb szükséges módosítást írja le.
        - Ne generálj olyan módosítást, amely nem következik közvetlenül a két határozat különbségéből.
        """;

    private final FileExtractorService extractor = new FileExtractorService();
    private final ObjectMapper mapper = new ObjectMapper();

    public void runAnalysis(AnalysisProject project,
                            ProgressCallback onProgress) throws Exception {

        project.setStatus("Running");

        onProgress.update(10, "Szöveg kinyerése a régi határozatból...");
        String oldDecision = extractor.extract(project.getOldDecisionFile());

        onProgress.update(25, "Szöveg kinyerése az új határozatból...");
        String newDecision = extractor.extract(project.getNewDecisionFile());

        onProgress.update(40, "Szöveg kinyerése a címkéből...");
        String labelText = extractor.extract(project.getLabelFile());

        onProgress.update(55, "AI elemzés indítása (" + AppConfig.getInstance().getProvider().displayName + ")...");
        AiProviderService aiService = createProvider();
        String rawJson = aiService.analyze(SYSTEM_PROMPT, oldDecision, newDecision, labelText);

        onProgress.update(85, "JSON feldolgozása...");
        // Strip potential markdown fences if AI ignores the instruction
        rawJson = stripMarkdownFences(rawJson);
        AnalysisResult result = mapper.readValue(rawJson, AnalysisResult.class);

        project.setResult(result);
        project.setLastRun(LocalDateTime.now());
        project.setStatus("Done");

        onProgress.update(100, "Kész.");
    }

    private AiProviderService createProvider() {
        return switch (AppConfig.getInstance().getProvider()) {
            case OPENAI    -> new OpenAiProviderService();
            case ANTHROPIC -> new AnthropicProviderService();
            case GEMINI    -> new GeminiProviderService();
        };
    }

    private String stripMarkdownFences(String text) {
        text = text.strip();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline >= 0) text = text.substring(firstNewline + 1);
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3).strip();
        }
        return text;
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void update(int percent, String message);
    }
}