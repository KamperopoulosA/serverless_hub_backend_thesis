package com.serverless.platformselector.service;

import com.serverless.platformselector.entity.ProviderCredentials;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.exception.BadRequestException;
import com.serverless.platformselector.repository.ProviderCredentialsRepository;
import com.serverless.platformselector.util.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ProviderCredentialsService {

    private static final Map<CloudProvider, Set<String>> ALLOWED_KEYS = new EnumMap<>(CloudProvider.class);

    static {
        ALLOWED_KEYS.put(CloudProvider.AWS, Set.of(
            "AWS_ACCESS_KEY_ID",
            "AWS_SECRET_ACCESS_KEY",
            "AWS_REGION"
        ));
        ALLOWED_KEYS.put(CloudProvider.GCP, Set.of("GCP_SERVICE_ACCOUNT_JSON"));
    }

    private final ProviderCredentialsRepository providerCredentialsRepository;
    private final EncryptionUtil encryptionUtil;

    public ProviderCredentialsService(
            ProviderCredentialsRepository providerCredentialsRepository,
            EncryptionUtil encryptionUtil) {
        this.providerCredentialsRepository = providerCredentialsRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public void saveCredentials(Integer ownerUserId, CloudProvider provider, Map<String, String> entries) {
        validateEntries(provider, entries);
        providerCredentialsRepository.deleteByOwnerUserIdAndProvider(ownerUserId, provider);

        entries.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
            .forEach(entry -> {
                ProviderCredentials credential = new ProviderCredentials();
                credential.setOwnerUserId(ownerUserId);
                credential.setProvider(provider);
                credential.setCredentialKey(entry.getKey());
                credential.setCredentialValue(encryptionUtil.encrypt(entry.getValue()));
                providerCredentialsRepository.save(credential);
            });
    }

    @Transactional(readOnly = true)
    public Map<String, String> getCredentials(Integer ownerUserId, CloudProvider provider) {
        Map<String, String> credentials = new HashMap<>();

        providerCredentialsRepository.findByOwnerUserIdAndProvider(ownerUserId, provider)
            .forEach(entry -> credentials.put(entry.getCredentialKey(), encryptionUtil.decrypt(entry.getCredentialValue())));

        return credentials;
    }

    @Transactional(readOnly = true)
    public Map<String, String> requireCredentials(Integer ownerUserId, CloudProvider provider) {
        Map<String, String> credentials = getCredentials(ownerUserId, provider);
        if (credentials.isEmpty()) {
            throw new BadRequestException("Missing saved credentials for provider " + provider);
        }
        return credentials;
    }

    private void validateEntries(CloudProvider provider, Map<String, String> entries) {
        if (provider == null) {
            throw new BadRequestException("Provider is required");
        }

        if (entries == null || entries.isEmpty()) {
            throw new BadRequestException("Credential entries are required");
        }

        Set<String> allowedKeys = ALLOWED_KEYS.get(provider);
        if (allowedKeys == null) {
            throw new BadRequestException("Unsupported credential provider " + provider);
        }

        Set<String> unexpectedKeys = new HashSet<>(entries.keySet());
        unexpectedKeys.removeAll(allowedKeys);
        if (!unexpectedKeys.isEmpty()) {
            throw new BadRequestException("Unsupported credential keys for provider " + provider + ": " + unexpectedKeys);
        }

        boolean hasAnyValue = entries.values().stream().anyMatch(value -> value != null && !value.isBlank());
        if (!hasAnyValue) {
            throw new BadRequestException("At least one credential value is required");
        }

        if (provider == CloudProvider.AWS) {
            requireEntry(entries, "AWS_ACCESS_KEY_ID");
            requireEntry(entries, "AWS_SECRET_ACCESS_KEY");
        } else if (provider == CloudProvider.GCP) {
            requireEntry(entries, "GCP_SERVICE_ACCOUNT_JSON");
        }
    }

    private void requireEntry(Map<String, String> entries, String key) {
        String value = entries.get(key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required credential key " + key);
        }
    }
}
