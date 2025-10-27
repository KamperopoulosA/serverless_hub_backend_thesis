package com.serverless.platformselector.util;

import java.util.HashMap;
import java.util.Map;

public class PlatformWeightsConfig {

    private static final Map<PlatformType, Map<String, Double>> weightsMap = new HashMap<>();

    static {
// AWS weights
        Map<String, Double> awsWeights = new HashMap<>();
        awsWeights.put("memory", 0.4);      // memory importance
        awsWeights.put("maxTimeout", 0.3);  // maxTimeout importance
        awsWeights.put("runtime", 0.2);     // runtime importance
        awsWeights.put("region", 0.1);      // region importance

// GCP weights
        Map<String, Double> gcpWeights = new HashMap<>();
        gcpWeights.put("memory", 0.3);
        gcpWeights.put("maxTimeout", 0.3);
        gcpWeights.put("runtime", 0.2);
        gcpWeights.put("region", 0.2);


        weightsMap.put(PlatformType.AWS, awsWeights);
        weightsMap.put(PlatformType.GCP, gcpWeights);
    }

    public static Map<String, Double> getWeightsForPlatform(PlatformType platformType) {
        return weightsMap.getOrDefault(platformType, new HashMap<>());
    }
}
