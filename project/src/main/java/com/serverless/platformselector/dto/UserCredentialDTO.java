package com.serverless.platformselector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "DTO for user credentials")
public class UserCredentialDTO {
    
    @NotNull(message = "User ID is required")
    @Schema(description = "User identifier", required = true)
    private UUID userId;
    
    @NotNull(message = "Platform ID is required")
    @Schema(description = "Platform identifier", required = true)
    private UUID platformId;
    
    @NotBlank(message = "Credential key is required")
    @Schema(description = "Credential key name", example = "AWS_ACCESS_KEY_ID", required = true)
    private String credentialKey;
    
    @NotBlank(message = "Credential value is required")
    @Schema(description = "Credential value", required = true)
    private String credentialValue;
    
    // Constructors
    public UserCredentialDTO() {}
    
    public UserCredentialDTO(UUID userId, UUID platformId, String credentialKey, String credentialValue) {
        this.userId = userId;
        this.platformId = platformId;
        this.credentialKey = credentialKey;
        this.credentialValue = credentialValue;
    }
    
    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getPlatformId() { return platformId; }
    public void setPlatformId(UUID platformId) { this.platformId = platformId; }
    
    public String getCredentialKey() { return credentialKey; }
    public void setCredentialKey(String credentialKey) { this.credentialKey = credentialKey; }
    
    public String getCredentialValue() { return credentialValue; }
    public void setCredentialValue(String credentialValue) { this.credentialValue = credentialValue; }
}