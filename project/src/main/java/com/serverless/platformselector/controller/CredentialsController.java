package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.UserCredentialDTO;
import com.serverless.platformselector.service.UserCredentialsService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/credentials")
@Tag(name = "Credentials Management", description = "APIs for managing user credentials")
public class CredentialsController {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialsController.class);
    
    @Autowired
    private UserCredentialsService userCredentialsService;
    
    @PostMapping
    @Operation(summary = "Save or update credentials", description = "Save or update user credentials for a platform")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credentials saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Platform not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> saveCredentials(@Valid @RequestBody UserCredentialDTO credentialDTO) {
        logger.info("POST /api/credentials - Saving credentials for user: {} and platform: {}", 
                   credentialDTO.getUserId(), credentialDTO.getPlatformId());
        
        userCredentialsService.saveOrUpdateCredentials(credentialDTO);
        
        return ResponseEntity.ok(Map.of("message", "Credentials saved successfully"));
    }
}