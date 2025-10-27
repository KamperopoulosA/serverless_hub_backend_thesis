package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.*;
import com.serverless.platformselector.entity.Platform;
import com.serverless.platformselector.service.PlatformService;
import com.serverless.platformselector.util.PerformanceCriterion;
import com.serverless.platformselector.util.PlatformType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/platforms")
@Tag(name = "Platform Management", description = "APIs for managing serverless platforms")
public class PlatformController {

    private static final Logger logger = LoggerFactory.getLogger(PlatformController.class);

    @Autowired
    private PlatformService platformService;

    // ✅ Everyone can view
    @GetMapping
    @Operation(summary = "Get all platforms")
    public ResponseEntity<List<PlatformDTO>> getAllPlatforms() {
        logger.info("GET /api/platforms");
        List<PlatformDTO> platforms = platformService.getAllPlatforms();
        return ResponseEntity.ok(platforms);
    }

    // ✅ Everyone can view by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get platform by ID")
    public ResponseEntity<PlatformDTO> getPlatformById(@PathVariable UUID id) {
        logger.info("GET /api/platforms/{}", id);
        PlatformDTO platform = platformService.getPlatformById(id);
        return ResponseEntity.ok(platform);
    }

    // ✅ Only ADMIN can create
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    @Operation(summary = "Create new platform")
    public ResponseEntity<PlatformDTO> createPlatform(@Valid @RequestBody PlatformCreateDTO createDTO) {
        logger.info("POST /api/platforms - Creating {}", createDTO.getName());
        PlatformDTO createdPlatform = platformService.createPlatform(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlatform);
    }

    // ✅ Only ADMIN can update
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update platform")
    public ResponseEntity<PlatformDTO> updatePlatform(@PathVariable UUID id, @Valid @RequestBody PlatformUpdateDTO updateDTO) {
        logger.info("PUT /api/platforms/{}", id);
        PlatformDTO updatedPlatform = platformService.updatePlatform(id, updateDTO);
        return ResponseEntity.ok(updatedPlatform);
    }

    // ✅ Only ADMIN can delete
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete platform")
    public ResponseEntity<Void> deletePlatform(@PathVariable UUID id) {
        logger.info("DELETE /api/platforms/{}", id);
        platformService.deletePlatform(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Search and ranking endpoints open for all
    @GetMapping("/search")
    public ResponseEntity<Page<PlatformDTO>> searchPlatformsGet(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        FilterRequestDTO filterRequest = new FilterRequestDTO();
        filterRequest.setName(name);
        filterRequest.setCategory(category);
        filterRequest.setDescription(description);
        filterRequest.setSortBy(sortBy);
        filterRequest.setSortDirection(sortDirection);
        filterRequest.setPage(page);
        filterRequest.setSize(size);

        Page<PlatformDTO> platforms = platformService.searchPlatforms(filterRequest);
        return ResponseEntity.ok(platforms);
    }

    @PostMapping("/search")
    public Page<PlatformDTO> searchPlatforms(@RequestBody FilterRequestDTO request) {
        return platformService.searchPlatforms(request);
    }

    @PostMapping("/search/fulltext")
    public ResponseEntity<Page<Platform>> searchPlatformsFullText(@RequestBody FilterRequestDTO filterRequest) {
        Page<Platform> page = platformService.searchPlatformsFullText(filterRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocompleteName(@RequestParam String prefix) {
        List<String> suggestions = platformService.getNameSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/rank")
    public ResponseEntity<List<PlatformRankingResult>> rankPlatformsMultiple(@RequestBody RankingRequest rankingRequest) {
        List<Platform> platforms = platformService.getAllPlatformsEntities();
        List<PlatformRankingResult> results = platformService.rankPlatformsGroupedByType(platforms, rankingRequest.getCriteria());
        return ResponseEntity.ok(results);
    }
}



