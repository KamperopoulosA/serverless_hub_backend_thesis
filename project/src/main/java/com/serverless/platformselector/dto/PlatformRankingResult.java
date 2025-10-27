package com.serverless.platformselector.dto;

import com.serverless.platformselector.util.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformRankingResult {
    private PlatformType platformType;
    private List<PlatformDTO> rankedPlatforms;
}
