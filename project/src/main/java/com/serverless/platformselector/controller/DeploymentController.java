package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.DeploymentRequestDTO;
import com.serverless.platformselector.dto.DeploymentResultDTO;
import com.serverless.platformselector.service.DeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/deployments")
@Tag(name = "Deployment Management", description = "APIs for deploying serverless functions")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping
    @Operation(summary = "Queue a serverless deployment job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deployment job queued successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid deployment request"),
        @ApiResponse(responseCode = "404", description = "Platform not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DeploymentResultDTO> deployFunction(
            @Valid @RequestBody DeploymentRequestDTO deploymentRequest,
            Authentication authentication) {
        return ResponseEntity.ok(deploymentService.queueDeployment(deploymentRequest, authentication));
    }

    @GetMapping("/{deploymentId}")
    @Operation(summary = "Get deployment status by id")
    public ResponseEntity<DeploymentResultDTO> getDeployment(
            @PathVariable UUID deploymentId,
            Authentication authentication) {
        return ResponseEntity.ok(deploymentService.getDeployment(deploymentId, authentication));
    }
}
