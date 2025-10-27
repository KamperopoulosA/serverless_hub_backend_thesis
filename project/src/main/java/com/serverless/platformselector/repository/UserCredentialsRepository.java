package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {
    
    List<UserCredentials> findByUserId(UUID userId);
    
    List<UserCredentials> findByUserIdAndPlatformId(UUID userId, UUID platformId);
    
    Optional<UserCredentials> findByUserIdAndPlatformIdAndCredentialKey(UUID userId, UUID platformId, String credentialKey);
    
    void deleteByUserIdAndPlatformId(UUID userId, UUID platformId);
}