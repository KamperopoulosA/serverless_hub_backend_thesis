package com.serverless.platformselector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.entity.PlatformFeatures;
import org.springframework.stereotype.Component;

@Component
public class PlatformFeaturesConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

/*    public PlatformFeatures convertJsonToFeatures(JsonNode featuresJson) {
        PlatformFeatures features = new PlatformFeatures();
        if (featuresJson == null) return features;

        featuresJson.fields().forEachRemaining(entry -> {
            features.put(entry.getKey(), entry.getValue().asText());
        });

        return features;
    }*/
}
