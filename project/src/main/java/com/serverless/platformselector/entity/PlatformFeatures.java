package com.serverless.platformselector.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PlatformFeatures {

    private final Map<String, Object> features = new HashMap<>();

    @JsonAnySetter
    public void addFeature(String key, Object value) {
        features.put(key.toLowerCase(), value);
    }

    // Renamed to avoid conflict with Platform.getFeatures()
    @JsonAnyGetter
    public Map<String, Object> getAll() {
        return features;
    }

    public Object get(String key) {
        return features.get(key.toLowerCase());
    }

    public boolean has(String key) {
        return features.containsKey(key.toLowerCase());
    }

    public Map<String, Object> toMap() {
        return features;
    }
}
