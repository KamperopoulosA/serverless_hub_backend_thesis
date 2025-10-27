package com.serverless.platformselector.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for updating an existing platform")
public class PlatformUpdateDTO {
    
    @Schema(description = "Platform name", example = "AWS Lambda")
    private String name;
    
    @Schema(description = "Platform description")
    private String description;
    
    @Schema(description = "Platform category", example = "FaaS")
    private String category;
    
    @Schema(description = "Platform features and criteria in JSON format")
    private JsonNode featuresJson;
    
    // Constructors
    public PlatformUpdateDTO() {}
    
    public PlatformUpdateDTO(String name, String description, String category, JsonNode featuresJson) {
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