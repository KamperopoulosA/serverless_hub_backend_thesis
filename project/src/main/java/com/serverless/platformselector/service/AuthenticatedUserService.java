package com.serverless.platformselector.service;

import com.serverless.platformselector.entity.OurUsers;
import com.serverless.platformselector.exception.ResourceNotFoundException;
import com.serverless.platformselector.repository.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final UsersRepo usersRepo;

    public AuthenticatedUserService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    public OurUsers requireCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        return usersRepo.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
    }
}
