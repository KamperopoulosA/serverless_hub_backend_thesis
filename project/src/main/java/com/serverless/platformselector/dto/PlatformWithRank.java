package com.serverless.platformselector.dto;

import com.serverless.platformselector.entity.Platform;

public class PlatformWithRank {

    private Platform platform;
    private Double rank;

    public PlatformWithRank(Platform platform, Double rank) {
        this.platform = platform;
        this.rank = rank;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Double getRank() {
        return rank;
    }
}
