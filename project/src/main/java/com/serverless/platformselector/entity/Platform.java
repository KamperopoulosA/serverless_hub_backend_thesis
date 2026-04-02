package com.serverless.platformselector.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.serverless.platformselector.util.PlatformFeatureValidator;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "platforms")
public class Platform {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String category;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features_json", columnDefinition = "jsonb")
    private JsonNode featuresJson;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private PlatformFeatures features;

    // Constructors
    public Platform() {}
    
    public Platform(String name, String description, String category, JsonNode featuresJson) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.featuresJson = featuresJson;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public JsonNode getFeaturesJson() { return featuresJson; }

    public void setFeaturesJson(JsonNode featuresJson) {
        PlatformFeatureValidator.validateFeatures(featuresJson);
        this.featuresJson = featuresJson;
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public PlatformFeatures getFeatures() {
        return features;
    }

    public void setFeatures(PlatformFeatures features) {
        this.features = features;
    }

}