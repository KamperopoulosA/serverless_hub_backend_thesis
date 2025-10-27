package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.UserCredentialDTO;
import com.serverless.platformselector.entity.Platform;
import com.serverless.platformselector.entity.UserCredentials;
import com.serverless.platformselector.exception.ResourceNotFoundException;
import com.serverless.platformselector.repository.PlatformRepository;
import com.serverless.platformselector.repository.UserCredentialsRepository;
import com.serverless.platformselector.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserCredentialsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCredentialsService.class);
    
    @Autowired
    private UserCredentialsRepository userCredentialsRepository;
    
    @Autowired
    private PlatformRepository platformRepository;
    
    @Autowired
    private EncryptionUtil encryptionUtil;
    
    public void saveOrUpdateCredentials(UserCredentialDTO credentialDTO) {
        logger.info("Saving/updating credentials for user: {} and platform: {}", 
                   credentialDTO.getUserId(), credentialDTO.getPlatformId());
        
        Platform platform = platformRepository.findById(credentialDTO.getPlatformId())
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + credentialDTO.getPlatformId()));
        
        Optional<UserCredentials> existingCredential = userCredentialsRepository
                .findByUserIdAndPlatformIdAndCredentialKey(
                    credentialDTO.getUserId(),
                    credentialDTO.getPlatformId(),
                    credentialDTO.getCredentialKey()
                );
        
        UserCredentials credentials;
        if (existingCredential.isPresent()) {
            credentials = existingCredential.get();
            logger.info("Updating existing credential");
        } else {
            credentials = new UserCredentials();
            credentials.setUserId(credentialDTO.getUserId());
            credentials.setPlatform(platform);
            credentials.setCredentialKey(credentialDTO.getCredentialKey());
            logger.info("Creating new credential");
        }
        
        // Encrypt the credential value before storing
        String encryptedValue = encryptionUtil.encrypt(credentialDTO.getCredentialValue());
        credentials.setCredentialValue(encryptedValue);
        
        userCredentialsRepository.save(credentials);
        logger.info("Credentials saved successfully");
    }
    
    public String getDecryptedCredentialValue(UUID userId, UUID platformId, String credentialKey) {
        logger.info("Retrieving credential for user: {}, platform: {}, key: {}", 
                   userId, platformId, credentialKey);
        
        UserCredentials credentials = userCredentialsRepository
                .findByUserIdAndPlatformIdAndCredentialKey(userId, platformId, credentialKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Credential not found for user: " + userId + ", platform: " + platformId + ", key: " + credentialKey));
        
        return encryptionUtil.decrypt(credentials.getCredentialValue());
    }
    
    public UserCredentials getCredentialsById(UUID credentialsId) {
        return userCredentialsRepository.findById(credentialsId)
                .orElseThrow(() -> new ResourceNotFoundException("Credentials not found with id: " + credentialsId));
    }

    public Map<String, String> getAllUserCredentialsMap(UUID userId, UUID platformId) {
        logger.info("Fetching all credentials for user: {} and platform: {}", userId, platformId);

        List<UserCredentials> credentialsList = userCredentialsRepository.findByUserIdAndPlatformId(userId, platformId);

        Map<String, String> credentialsMap = new HashMap<>();
        for (UserCredentials cred : credentialsList) {
            String decryptedValue = encryptionUtil.decrypt(cred.getCredentialValue());
            credentialsMap.put(cred.getCredentialKey(), decryptedValue);
        }

        return credentialsMap;
    }
}