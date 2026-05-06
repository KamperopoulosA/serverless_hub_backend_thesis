package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.ProviderCredentialsRequestDTO;
import com.serverless.platformselector.entity.OurUsers;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.service.AuthenticatedUserService;
import com.serverless.platformselector.service.ProviderCredentialsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/credentials")
public class CredentialsController {

    private final ProviderCredentialsService providerCredentialsService;
    private final AuthenticatedUserService authenticatedUserService;

    public CredentialsController(
            ProviderCredentialsService providerCredentialsService,
            AuthenticatedUserService authenticatedUserService) {
        this.providerCredentialsService = providerCredentialsService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PutMapping("/{provider}")
    public ResponseEntity<Map<String, String>> saveCredentials(
            @PathVariable CloudProvider provider,
            @Valid @RequestBody ProviderCredentialsRequestDTO credentialDTO,
            Authentication authentication) {
        OurUsers owner = authenticatedUserService.requireCurrentUser(authentication);
        providerCredentialsService.saveCredentials(owner.getId(), provider, credentialDTO.getEntries());
        return ResponseEntity.ok(Map.of("message", "Credentials saved successfully"));
    }

    @GetMapping("/{provider}")
    public ResponseEntity<ProviderCredentialsRequestDTO> getCredentials(
            @PathVariable CloudProvider provider,
            Authentication authentication) {
        OurUsers owner = authenticatedUserService.requireCurrentUser(authentication);
        ProviderCredentialsRequestDTO response = new ProviderCredentialsRequestDTO();
        response.setEntries(providerCredentialsService.getCredentials(owner.getId(), provider));
        return ResponseEntity.ok(response);
    }
}
