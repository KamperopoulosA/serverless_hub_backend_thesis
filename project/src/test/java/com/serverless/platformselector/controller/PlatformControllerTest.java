package com.serverless.platformselector.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.dto.PlatformCreateDTO;
import com.serverless.platformselector.dto.PlatformDTO;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.service.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                CloudProvider.AWS,
                featuresJson,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void getAllPlatforms_ShouldReturnPlatformsList() throws Exception {
        List<PlatformDTO> platforms = Arrays.asList(testPlatformDTO);
        when(platformService.getAllPlatforms()).thenReturn(platforms);

        mockMvc.perform(get("/api/platforms"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("AWS Lambda"))
                .andExpect(jsonPath("$[0].category").value("FaaS"));
    }

    @Test
    void getPlatformById_ShouldReturnPlatform() throws Exception {
        when(platformService.getPlatformById(testId)).thenReturn(testPlatformDTO);

        mockMvc.perform(get("/api/platforms/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("AWS Lambda"))
                .andExpect(jsonPath("$.category").value("FaaS"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createPlatform_ShouldCreateAndReturnPlatform() throws Exception {
        JsonNode featuresJson = objectMapper.readTree("{\"maxMemory\": 3008}");
        PlatformCreateDTO createDTO = new PlatformCreateDTO(
                "Google Cloud Functions",
                "Google Cloud Functions",
                "FaaS",
                CloudProvider.GCP,
                featuresJson
        );

        PlatformDTO createdPlatform = new PlatformDTO(
                UUID.randomUUID(),
                "Google Cloud Functions",
                "Google Cloud Functions",
                "FaaS",
                CloudProvider.GCP,
                featuresJson,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(platformService.createPlatform(any(PlatformCreateDTO.class))).thenReturn(createdPlatform);

        mockMvc.perform(post("/api/platforms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Google Cloud Functions"))
                .andExpect(jsonPath("$.category").value("FaaS"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createPlatform_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        PlatformCreateDTO invalidDTO = new PlatformCreateDTO();

        mockMvc.perform(post("/api/platforms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deletePlatform_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/platforms/{id}", testId))
                .andExpect(status().isNoContent());
    }
}
