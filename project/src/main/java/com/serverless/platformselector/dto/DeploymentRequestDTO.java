package com.serverless.platformselector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "DTO for deployment request")
public class DeploymentRequestDTO {

    @NotNull(message = "Platform ID is required")
    @Schema(description = "Platform identifier", required = true)
    private UUID platformId;

    @NotBlank(message = "Function name is required")
    @Schema(description = "Name of the function to deploy", example = "my-serverless-function", required = true)
    private String functionName;

    @NotBlank(message = "Function package is required")
    @Schema(description = "Base64 encoded function package (zip file)", required = true)
    private String functionPackageBase64;

    @Schema(description = "Function runtime", example = "nodejs18.x")
    private String runtime;

    @Schema(description = "Function handler", example = "index.handler")
    private String handler;

    @Schema(description = "Deployment region", example = "us-east-1")
    private String region;

    public UUID getPlatformId() {
        return platformId;
    }

    public void setPlatformId(UUID platformId) {
        this.platformId = platformId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionPackageBase64() {
        return functionPackageBase64;
    }

    public void setFunctionPackageBase64(String functionPackageBase64) {
        this.functionPackageBase64 = functionPackageBase64;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
