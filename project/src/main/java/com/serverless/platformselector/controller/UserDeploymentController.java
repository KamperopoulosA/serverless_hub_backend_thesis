package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.service.UserDeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/deployments")
public class UserDeploymentController {

    private final UserDeploymentService userDeploymentService;

    public UserDeploymentController(UserDeploymentService userDeploymentService) {
        this.userDeploymentService = userDeploymentService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<DeploymentRecordDTO>> getMyDeployments(Authentication authentication) {
        return ResponseEntity.ok(userDeploymentService.getDeploymentsForCurrentUser(authentication));
    }
}
