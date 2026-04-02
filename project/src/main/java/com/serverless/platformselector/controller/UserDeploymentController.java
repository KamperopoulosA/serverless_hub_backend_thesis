package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.service.UserDeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/deployments")
public class UserDeploymentController {

    private final UserDeploymentService userDeploymentService;

    public UserDeploymentController(UserDeploymentService userDeploymentService) {
        this.userDeploymentService = userDeploymentService;
    }


    @GetMapping("/my")
    public ResponseEntity<List<DeploymentRecordDTO>> getMyDeployments(Authentication authentication) {

        //
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222"); //authentication.getName();


        List<DeploymentRecordDTO> deployments = userDeploymentService.getDeploymentsForUser(userId);
        return ResponseEntity.ok(deployments);
    }
}
