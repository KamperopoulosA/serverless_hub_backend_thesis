package com.serverless.platformselector.util;

import com.serverless.platformselector.dto.CriterionRequestDTO;
import com.serverless.platformselector.entity.Platform;

import java.util.List;

public class PlatformRankingUtil {

    // ===========================================================
    // FINAL SCORE
    // ===========================================================
    public static double calculateScore(Platform platform,
                                        List<Platform> allPlatforms,
                                        List<CriterionRequestDTO> criteria) {

        double totalScore = 0.0;

        for (CriterionRequestDTO c : criteria) {

            double weight = c.getWeight() != null ? c.getWeight() : 0.0;
            if (weight == 0) continue;

            double criterionScore = computeCriterionScore(platform, allPlatforms, c);
            totalScore += weight * criterionScore;
        }

        return totalScore;
    }

    // ===========================================================
    // MAIN SWITCH
    // ===========================================================
    private static double computeCriterionScore(Platform platform,
                                                List<Platform> allPlatforms,
                                                CriterionRequestDTO c) {

        Object rawValue = platform.getFeatures().get(c.getName());
        if (rawValue == null) return 0.0;

        switch (c.getType().toLowerCase()) {

            case "numeric":
                return computeNumericMinMaxScore(allPlatforms, c, rawValue);

            case "categorical_multiple":
                return computeSetMatchingScore(c, rawValue);

            case "categorical":
                return computeCategoricalScore(c, rawValue);

            default:
                return 0.0;
        }
    }

    // ===========================================================
    // 🔴 NUMERIC → GLOBAL MIN-MAX (NO USER VALUE)
    // ===========================================================
    private static double computeNumericMinMaxScore(List<Platform> allPlatforms,
                                                    CriterionRequestDTO c,
                                                    Object rawValue) {

        double value = extractNumeric(rawValue.toString());

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Platform p : allPlatforms) {

            Object v = p.getFeatures().get(c.getName());
            if (v == null) continue;

            double num = extractNumeric(v.toString());

            if (num < min) min = num;
            if (num > max) max = num;
        }

        if (max == min) return 1.0;

        double score = (value - min) / (max - min);

        // για latency κτλ
        if ("negative".equalsIgnoreCase(c.getDirection())) {
            score = 1.0 - score;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    // ===========================================================
    // 🔴 SET MATCHING (LANGUAGES / RUNTIME)
    // ===========================================================
    private static double computeSetMatchingScore(CriterionRequestDTO c,
                                                  Object rawValue) {

        if (!(rawValue instanceof List<?> platformValues)) {
            return 0.0;
        }

        List<String> userValues = c.getCategoricalValues();
        if (userValues == null || userValues.isEmpty()) return 0.0;

        long matches = platformValues.stream()
                .map(Object::toString)
                .filter(userValues::contains)
                .count();

        return (double) matches / (double) userValues.size();
    }

    // ===========================================================
    // (OPTIONAL) ORDINAL
    // ===========================================================
    private static double computeCategoricalScore(CriterionRequestDTO c,
                                                  Object rawValue) {

        if (c.getCategoricalValues() == null || c.getCategoricalValues().isEmpty())
            return 0.0;

        List<String> list = c.getCategoricalValues();

        int index = list.indexOf(rawValue.toString());
        if (index == -1) return 0.0;

        int maxIndex = list.size() - 1;

        return 1.0 - ((double) index / (double) maxIndex);
    }

    // ===========================================================
    // UTILITY
    // ===========================================================
    public static double extractNumeric(String raw) {

        if (raw == null) return 0.0;

        String cleaned = raw.replaceAll("[^0-9.]", "");

        if (cleaned.isEmpty()) return 0.0;

        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
