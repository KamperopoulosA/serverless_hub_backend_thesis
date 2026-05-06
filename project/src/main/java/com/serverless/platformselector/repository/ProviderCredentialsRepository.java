package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.ProviderCredentials;
import com.serverless.platformselector.enums.CloudProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProviderCredentialsRepository extends JpaRepository<ProviderCredentials, UUID> {

    List<ProviderCredentials> findByOwnerUserIdAndProvider(Integer ownerUserId, CloudProvider provider);

    void deleteByOwnerUserIdAndProvider(Integer ownerUserId, CloudProvider provider);
}
