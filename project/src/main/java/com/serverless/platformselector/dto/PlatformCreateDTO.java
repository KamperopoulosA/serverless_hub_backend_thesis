package com.serverless.platformselector.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO for creating a new platform")
public class PlatformCreateDTO {
    
    @NotBlank(message = "Platform name is required")
    @Schema(description = "Platform name", example = "AWS Lambda", required = true)
    private String name;
    
    @Schema(description = "Platform description")
    private String description;
    
    @NotBlank(message = "Platform category is required")
    @Schema(description = "Platform category", example = "FaaS", required = true)
    private String category;
    
    @NotNull(message = "Features JSON is required")
    @Schema(description = "Platform features and criteria in JSON format", required = true)
    private JsonNode featuresJson;
    
    // Constructors
    public PlatformCreateDTO() {}
    
    public PlatformCreateDTO(String name, String description, String category, JsonNode featuresJson) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.featuresJson = featuresJson;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public JsonNode getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(JsonNode featuresJson) { this.featuresJson = featuresJson; }
}