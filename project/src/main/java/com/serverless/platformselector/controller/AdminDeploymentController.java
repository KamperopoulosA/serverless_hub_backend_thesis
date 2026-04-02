package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.service.DeploymentAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/deployments")
public class AdminDeploymentController {

    private final DeploymentAdminService deploymentAdminService;

    public AdminDeploymentController(DeploymentAdminService deploymentAdminService) {
        this.deploymentAdminService = deploymentAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DeploymentRecordDTO>> getAllDeployments() {
        List<DeploymentRecordDTO> deployments = deploymentAdminService.getAllDeployments();
        return ResponseEntity.ok(deployments);
    }
}
