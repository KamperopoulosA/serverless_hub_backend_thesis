package com.serverless.platformselector.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.dto.PlatformCreateDTO;
import com.serverless.platformselector.dto.PlatformDTO;
import com.serverless.platformselector.service.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlatformController.class)
class PlatformControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PlatformService platformService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PlatformDTO testPlatformDTO;
    private UUID testId;
    
    @BeforeEach
    void setUp() throws Exception {
        testId = UUID.randomUUID();
        JsonNode featuresJson = objectMapper.readTree("{\"maxMemory\": 3008, \"timeout\": 900}");
        
        testPlatformDTO = new PlatformDTO(
            testId,
            "AWS Lambda",
            "Amazon Web Services Lambda",
            "FaaS",
            featuresJson,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
    
    @Test
    void getAllPlatforms_ShouldReturnPlatformsList() throws Exception {
        // Given
        List<PlatformDTO> platforms = Arrays.asList(testPlatformDTO);
        when(platformService.getAllPlatforms()).thenReturn(platforms);
        
        // When & Then
        mockMvc.perform(get("/api/platforms"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("AWS Lambda"))
                .andExpect(jsonPath("$[0].category").value("FaaS"));
    }
    
    @Test
    void getPlatformById_ShouldReturnPlatform() throws Exception {
        // Given
        when(platformService.getPlatformById(testId)).thenReturn(testPlatformDTO);
        
        // When & Then
        mockMvc.perform(get("/api/platforms/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("AWS Lambda"))
                .andExpect(jsonPath("$.category").value("FaaS"));
    }
    
    @Test
    void createPlatform_ShouldCreateAndReturnPlatform() throws Exception {
        // Given
        JsonNode featuresJson = objectMapper.readTree("{\"maxMemory\": 3008}");
        PlatformCreateDTO createDTO = new PlatformCreateDTO(
            "Google Cloud Functions",
            "Google Cloud Functions",
            "FaaS",
            featuresJson
        );
        
        PlatformDTO createdPlatform = new PlatformDTO(
            UUID.randomUUID(),
            "Google Cloud Functions",
            "Google Cloud Functions",
            "FaaS",
            featuresJson,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        when(platformService.createPlatform(any(PlatformCreateDTO.class))).thenReturn(createdPlatform);
        
        // When & Then
        mockMvc.perform(post("/api/platforms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Google Cloud Functions"))
                .andExpect(jsonPath("$.category").value("FaaS"));
    }
    
    @Test
    void createPlatform_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid DTO with missing required fields
        PlatformCreateDTO invalidDTO = new PlatformCreateDTO();
        
        // When & Then
        mockMvc.perform(post("/api/platforms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void deletePlatform_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/platforms/{id}", testId))
                .andExpect(status().isNoContent());
    }
}