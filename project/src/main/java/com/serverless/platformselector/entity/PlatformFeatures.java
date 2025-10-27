package com.serverless.platformselector.entity;

import java.util.HashMap;
import java.util.Map;

public class PlatformFeatures extends HashMap<String, String> {
    private final Map<String, Object> features = new HashMap<>();

    public void put(String key, Object value) {
        features.put(key.toLowerCase(), value);
    }

    public Object get(String key) {
        return features.get(key.toLowerCase());
    }

    public boolean has(String key) {
        return features.containsKey(key.toLowerCase());
    }
}
