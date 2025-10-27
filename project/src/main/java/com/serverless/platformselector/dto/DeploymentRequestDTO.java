package com.serverless.platformselector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "DTO for deployment request")
public class DeploymentRequestDTO {
    
    @NotNull(message = "Platform ID is required")
    @Schema(description = "Platform identifier", required = true)
    private UUID platformId;
    
    @NotNull(message = "User ID is required")
    @Schema(description = "User identifier", required = true)
    private UUID userId;

    @NotBlank(message = "Function name is required")
    @Schema(description = "Name of the function to deploy", example = "my-serverless-function", required = true)
    private String functionName;
    
    @NotBlank(message = "Function package is required")
    @Schema(description = "Base64 encoded function package (zip file)", required = true)
    private String functionPackageBase64;
    
    @NotNull(message = "Credentials ID is required")
    @Schema(description = "User credentials identifier for the platform", required = true)
    private UUID credentialsId;


    private String runtime;  // π.χ. "nodejs18.x", "python3.8", "java11"
    private String handler;  // π.χ. "index.handler", "app.lambda_handler", "com.example.Handler::handleRequest"
    private String region;   // π.χ. "us-east-1", "us-central1" (προαιρετικό, μπορεί να έχει default)
}