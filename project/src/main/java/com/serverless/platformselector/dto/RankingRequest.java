package com.serverless.platformselector.dto;

import com.serverless.platformselector.entity.Criterion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingRequest {
    private List<CriterionRequestDTO> criteria;
}

