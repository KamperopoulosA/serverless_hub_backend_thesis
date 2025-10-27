package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.DeploymentRequestDTO;
import com.serverless.platformselector.dto.DeploymentResultDTO;
import com.serverless.platformselector.service.DeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deployments")
@Tag(name = "Deployment Management", description = "APIs for deploying serverless functions")
public class DeploymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);
    
    @Autowired
    private DeploymentService deploymentService;
    
    @PostMapping
    @Operation(summary = "Deploy serverless function", description = "Deploy a serverless function to the specified platform")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deployment initiated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid deployment request"),
        @ApiResponse(responseCode = "404", description = "Platform or credentials not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DeploymentResultDTO> deployFunction(@Valid @RequestBody DeploymentRequestDTO deploymentRequest) {
        logger.info("POST /api/deployments - Deploying function: {} to platform: {}", 
                   deploymentRequest.getFunctionName(), deploymentRequest.getPlatformId());
        
        DeploymentResultDTO result = deploymentService.deployFunction(deploymentRequest);
        
        return ResponseEntity.ok(result);
    }
}