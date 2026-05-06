package com.serverless.platformselector.dto;

import com.serverless.platformselector.enums.CloudProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformRankingResult {
    private CloudProvider platformType;
    private List<PlatformDTO> rankedPlatforms;
}
