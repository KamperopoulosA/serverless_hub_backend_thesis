package com.serverless.platformselector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.serverless.platformselector.enums.FeatureKey;

public class PlatformFeatureValidator {

    public static void validateFeatures(JsonNode json) {
        json.fields().forEachRemaining(entry -> {
            String key = entry.getKey().toUpperCase();

            try {
                FeatureKey.valueOf(key);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Το χαρακτηριστικό '" + entry.getKey() + "' δεν είναι αποδεκτό. " +
                                "Επιτρεπόμενα keys: runtime, region, scaling, maxTimeout, memory."
                );
            }
        });
    }
}
