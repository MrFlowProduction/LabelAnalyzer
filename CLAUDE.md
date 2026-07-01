gye# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

LabelAnalyzer is a Hungarian-language JavaFX desktop app for plant-protection-product (növényvédő szer) regulatory analysts. It compares an old and a new emergency authorization decision ("határozat", PDF/DOCX) against the current product label (DOCX), sends the extracted text to an LLM, and turns the response into a structured list of required label edits.

## Build & run

Requires JDK 23 and Maven (no wrapper checked in — use a system `mvn`).

```bash
mvn compile                # compile
mvn javafx:run             # run the app via the JavaFX plugin (uses hu.mrflow.labelanalyzer.MainApp)
mvn package                # build the shaded (fat) jar via maven-shade-plugin
```

There is no `src/test` directory and no test framework configured — there are currently no automated tests to run.

The JavaFX dependencies are declared twice per module (plain + `mac-aarch64` classifier). When adding a new JavaFX module or bumping `javafx.version` in `pom.xml`, keep both entries in sync or the fat jar built by `maven-shade-plugin` will be missing native libs on macOS ARM.

## Architecture

MVVM, wired together through FXML `fx:include` composition — not a DI framework.

- **model** (`model/`) — plain data holders. `AnalysisProject` is the user-facing project (name, the three input `File`s, status, last run, an `AnalysisResult`). `AnalysisResult` is the Jackson-mapped tree of the AI's JSON output (`decision_differences`, `label_changes`, `unchanged_sections`, `warnings`) — its field names/nesting mirror the JSON schema embedded in `AnalysisService.SYSTEM_PROMPT` verbatim, so if the prompt schema changes, `AnalysisResult` must change with it.
- **service** (`service/`) — business logic, no JavaFX types.
  - `AnalysisService` orchestrates a run: extract text from the 3 files → call the configured AI provider → strip markdown fences → parse JSON into `AnalysisResult` → update the project. The full system prompt (Hungarian, defines the JSON schema and rules the AI must follow) lives here as a constant.
  - `AiProviderService` is the interface each LLM backend implements (`analyze(systemPrompt, oldDecision, newDecision, labelText) -> raw JSON string`). `OpenAiProviderService`, `AnthropicProviderService`, `GeminiProviderService` each hand-build the provider-specific HTTP request via `java.net.http.HttpClient` (no SDK) and pull the completion text out of the provider's own response shape. Adding a provider means implementing this interface and adding a case in `AnalysisService.createProvider()` plus an `AppConfig.AiProvider` enum entry.
  - `FileExtractorService` extracts plain text from PDF (PDFBox) and DOCX (Apache POI); dispatches on file extension.
  - `ProjectRepository` is a singleton, file-based store. Each project is persisted as two separate JSON files under `~/.labelanalyzer/projects/`: `<uuid>.json` (metadata: name, file paths, status) and `<uuid>.result.json` (the potentially-large `AnalysisResult`), loaded lazily so the project list stays fast to open.
- **config** (`config/`) — `AppConfig` is a singleton wrapping a `Properties` file at `~/.labelanalyzer/config.properties`: selected provider, and per-provider API key/endpoint/model.
- **viewmodel** (`viewmodel/`) — JavaFX `Property`/`ObservableList`-based state, no FXML/UI references. `MainViewModel` is the root; it constructs and wires the child view models (`ProjectListViewModel`, `ProjectDetailViewModel`, `AnalysisResultViewModel`, `AppSettingsViewModel`) and contains the cross-VM glue (e.g. selecting a project in the list loads it into the detail VM; a run finishing updates the list item's status for redisplay). `AnalysisResultViewModel` is shared between the detail view and the result tabs. Long-running work (`ProjectDetailViewModel.runAnalysis()`) runs on a background `javafx.concurrent.Task` off a daemon thread, reporting progress back via `Platform.runLater`.
- **view** (`view/`) — FXML controllers. Controllers only bind Properties to UI controls and delegate actions to the ViewModel; no business logic here. Composition mirrors the FXML `fx:include` tree: `MainView` includes `ProjectListView` and `ProjectDetailView`; `ProjectDetailView` includes `ResultTabsView`. Child controllers are injected into parents via the `<fx:id>Controller` field-naming convention (e.g. `fx:id="projectDetail"` → field `projectDetailController`), and `MainController.initialize()` explicitly passes each child controller its ViewModel(s) — there's no automatic DI, so a new nested view needs a matching field + explicit `setViewModel(...)` call in the parent.

## Notes

- UI strings, code comments, log messages, and the AI system prompt are in Hungarian; keep new user-facing text and comments consistent with that unless told otherwise.
- API keys are stored in plaintext in `~/.labelanalyzer/config.properties`.
- `AnalysisService.stripMarkdownFences` is a defensive workaround for providers that wrap JSON in ```` ```json ```` fences despite the prompt saying not to — keep it when touching that code path.