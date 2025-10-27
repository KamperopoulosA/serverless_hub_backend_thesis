package com.serverless.platformselector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CriterionRequestDTO {
    private String name;
    private String type;
    private Double minValue;
    private Double maxValue;
    private List<String> categoricalValues;
    private String direction;
    private String userValue; // only for requests
}

