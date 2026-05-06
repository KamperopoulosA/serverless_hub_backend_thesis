package com.serverless.platformselector.dto;

import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ProviderCredentialsRequestDTO {

    @NotNull(message = "Credential entries are required")
    private Map<String, String> entries = new HashMap<>();

    public Map<String, String> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, String> entries) {
        this.entries = entries;
    }
}
