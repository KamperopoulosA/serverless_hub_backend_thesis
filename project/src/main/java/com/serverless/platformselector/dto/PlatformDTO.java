package com.serverless.platformselector.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(description = "Platform data transfer object")
public class PlatformDTO {
    
    @Schema(description = "Platform unique identifier")
    private UUID id;
    
    @Schema(description = "Platform name", example = "AWS Lambda")
    private String name;
    
    @Schema(description = "Platform description")
    private String description;
    
    @Schema(description = "Platform category", example = "FaaS")
    private String category;
    
    @Schema(description = "Platform features and criteria in JSON format")
    private JsonNode featuresJson;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    private double totalScore;

    // Constructors
    public PlatformDTO() {}
    
    public PlatformDTO(UUID id, String name, String description, String category, 
                      JsonNode featuresJson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.featuresJson = featuresJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}