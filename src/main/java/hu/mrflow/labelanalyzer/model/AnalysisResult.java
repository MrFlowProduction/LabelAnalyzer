package hu.mrflow.labelanalyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Root model for the AI-generated JSON analysis result.
 * Field names match the JSON schema defined in the system prompt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResult {

    @JsonProperty("schema_version")
    public String schemaVersion;

    @JsonProperty("project")
    public ProjectInfo project;

    @JsonProperty("decision_differences")
    public List<DecisionDifference> decisionDifferences;

    @JsonProperty("label_changes")
    public List<LabelChange> labelChanges;

    @JsonProperty("unchanged_sections")
    public List<UnchangedSection> unchangedSections;

    @JsonProperty("warnings")
    public List<Warning> warnings;

    // ── Nested types ──────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectInfo {
        @JsonProperty("product_name")      public String productName;
        @JsonProperty("country")           public String country;
        @JsonProperty("language")          public String language;
        @JsonProperty("old_authorization_id") public String oldAuthorizationId;
        @JsonProperty("new_authorization_id") public String newAuthorizationId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DecisionDifference {
        @JsonProperty("id")             public String id;
        @JsonProperty("category")       public String category;
        @JsonProperty("field")          public String field;
        @JsonProperty("change_type")    public String changeType;
        @JsonProperty("old_value")      public String oldValue;
        @JsonProperty("new_value")      public String newValue;
        @JsonProperty("impact_summary") public String impactSummary;
        @JsonProperty("label_relevance") public String labelRelevance;
        @JsonProperty("source_old")     public SourceRef sourceOld;
        @JsonProperty("source_new")     public SourceRef sourceNew;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LabelChange {
        @JsonProperty("id")             public String id;
        @JsonProperty("operation")      public String operation;
        @JsonProperty("status")         public String status;
        @JsonProperty("label_location") public LabelLocation labelLocation;
        @JsonProperty("old_text")       public String oldText;
        @JsonProperty("new_text")       public String newText;
        @JsonProperty("change_reason")  public String changeReason;
        @JsonProperty("linked_decision_difference_ids") public List<String> linkedIds;
        @JsonProperty("source_new_decision") public SourceRef sourceNewDecision;
        @JsonProperty("confidence")     public String confidence;
        @JsonProperty("review_note")    public String reviewNote;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LabelLocation {
        @JsonProperty("page")               public Integer page;
        @JsonProperty("section")            public String section;
        @JsonProperty("paragraph_anchor")   public String paragraphAnchor;
        @JsonProperty("table_name")         public String tableName;
        @JsonProperty("row_identifier")     public String rowIdentifier;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceRef {
        @JsonProperty("page")  public Integer page;
        @JsonProperty("quote") public String quote;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnchangedSection {
        @JsonProperty("section") public String section;
        @JsonProperty("reason")  public String reason;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Warning {
        @JsonProperty("severity")         public String severity;
        @JsonProperty("message")          public String message;
        @JsonProperty("affected_changes") public List<String> affectedChanges;
    }
}
