package com.serverless.platformselector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.dto.PlatformCreateDTO;
import com.serverless.platformselector.dto.PlatformDTO;
import com.serverless.platformselector.entity.Platform;
import com.serverless.platformselector.exception.ResourceNotFoundException;
import com.serverless.platformselector.repository.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {
    
    @Mock
    private PlatformRepository platformRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private PlatformService platformService;
    
    private Platform testPlatform;
    private UUID testId;
    
    @BeforeEach
    void setUp() throws Exception {
        testId = UUID.randomUUID();
        testPlatform = new Platform();
        testPlatform.setId(testId);
        testPlatform.setName("AWS Lambda");
        testPlatform.setDescription("Amazon Web Services Lambda");
        testPlatform.setCategory("FaaS");
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode featuresJson = mapper.readTree("{\"maxMemory\": 3008, \"timeout\": 900}");
        testPlatform.setFeaturesJson(featuresJson);
    }
    
    @Test
    void getAllPlatforms_ShouldReturnAllPlatforms() {
        // Given
        List<Platform> platforms = Arrays.asList(testPlatform);
        when(platformRepository.findAll()).thenReturn(platforms);
        
        // When
        List<PlatformDTO> result = platformService.getAllPlatforms();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AWS Lambda", result.get(0).getName());
        verify(platformRepository).findAll();
    }
    
    @Test
    void getPlatformById_WhenPlatformExists_ShouldReturnPlatform() {
        // Given
        when(platformRepository.findById(testId)).thenReturn(Optional.of(testPlatform));
        
        // When
        PlatformDTO result = platformService.getPlatformById(testId);
        
        // Then
        assertNotNull(result);
        assertEquals("AWS Lambda", result.getName());
        assertEquals(testId, result.getId());
        verify(platformRepository).findById(testId);
    }
    
    @Test
    void getPlatformById_WhenPlatformNotExists_ShouldThrowException() {
        // Given
        when(platformRepository.findById(testId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            platformService.getPlatformById(testId);
        });
        verify(platformRepository).findById(testId);
    }
    
    @Test
    void createPlatform_ShouldCreateAndReturnPlatform() throws Exception {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode featuresJson = mapper.readTree("{\"maxMemory\": 3008}");
        
        PlatformCreateDTO createDTO = new PlatformCreateDTO(
            "Google Cloud Functions",
            "Google Cloud Functions",
            "FaaS",
            featuresJson
        );
        
        Platform savedPlatform = new Platform();
        savedPlatform.setId(UUID.randomUUID());
        savedPlatform.setName(createDTO.getName());
        savedPlatform.setDescription(createDTO.getDescription());
        savedPlatform.setCategory(createDTO.getCategory());
        savedPlatform.setFeaturesJson(createDTO.getFeaturesJson());
        
        when(platformRepository.save(any(Platform.class))).thenReturn(savedPlatform);
        
        // When
        PlatformDTO result = platformService.createPlatform(createDTO);
        
        // Then
        assertNotNull(result);
        assertEquals("Google Cloud Functions", result.getName());
        assertEquals("FaaS", result.getCategory());
        verify(platformRepository).save(any(Platform.class));
    }
    
    @Test
    void deletePlatform_WhenPlatformExists_ShouldDeletePlatform() {
        // Given
        when(platformRepository.existsById(testId)).thenReturn(true);
        
        // When
        platformService.deletePlatform(testId);
        
        // Then
        verify(platformRepository).existsById(testId);
        verify(platformRepository).deleteById(testId);
    }
    
    @Test
    void deletePlatform_WhenPlatformNotExists_ShouldThrowException() {
        // Given
        when(platformRepository.existsById(testId)).thenReturn(false);
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            platformService.deletePlatform(testId);
        });
        verify(platformRepository).existsById(testId);
        verify(platformRepository, never()).deleteById(testId);
    }
}