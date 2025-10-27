package com.serverless.platformselector.entity;

import com.serverless.platformselector.enums.DeploymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_records")
public class DeploymentRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;
    
    @Column(name = "function_name", nullable = false)
    private String functionName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "deployment_status", nullable = false)
    private DeploymentStatus deploymentStatus;
    
    @Column(name = "endpoint_url")
    private String endpointUrl;
    
    @Column(columnDefinition = "TEXT")
    private String log;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public DeploymentRecord() {}
    
    public DeploymentRecord(UUID userId, Platform platform, String functionName, DeploymentStatus deploymentStatus) {
        this.userId = userId;
        this.platform = platform;
        this.functionName = functionName;
        this.deploymentStatus = deploymentStatus;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    
    public DeploymentStatus getDeploymentStatus() { return deploymentStatus; }
    public void setDeploymentStatus(DeploymentStatus deploymentStatus) { this.deploymentStatus = deploymentStatus; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getLog() { return log; }
    public void setLog(String log) { this.log = log; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}