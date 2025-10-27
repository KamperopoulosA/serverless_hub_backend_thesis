package com.serverless.platformselector.util;

import com.serverless.platformselector.dto.CriterionRequestDTO;
import com.serverless.platformselector.entity.Criterion;
import com.serverless.platformselector.entity.PlatformFeatures;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PlatformRankingUtil {

    public static double calculateScore(
                                        List<CriterionRequestDTO> criteria,
                                        Map<String, Double> weights) {

        double totalScore = 0.0;

        for (CriterionRequestDTO criterion : criteria) {
            String name = criterion.getName().toLowerCase();
            double weight = weights.getOrDefault(name, 0.0);
            if (weight == 0) continue;

            double scoreForCriterion = computeScoreForCriterion(criterion);
            totalScore += scoreForCriterion * weight;

            System.out.println("Criterion: " + name +
                    ", Score: " + scoreForCriterion +
                    ", Weight: " + weight);
        }

        return totalScore;
    }

    public static double computeScoreForCriterion(CriterionRequestDTO criterion) {
        String type = criterion.getType();
        String direction = criterion.getDirection();
        String userValue = criterion.getUserValue();

        double score = 0.0;

        if ("numeric".equalsIgnoreCase(type) && userValue != null && !userValue.isEmpty()) {
            try {
                double value = Double.parseDouble(userValue);
                double min = criterion.getMinValue() != null ? criterion.getMinValue() : value;
                double max = criterion.getMaxValue() != null ? criterion.getMaxValue() : value;

                if (max > min) {
                    score = (value - min) / (max - min);
                    score = Math.max(0.0, Math.min(1.0, score)); // clamp to [0,1]
                }

                if ("negative".equalsIgnoreCase(direction)) {
                    score = 1.0 - score;
                }
            } catch (NumberFormatException e) {
                score = 0.0;
            }
        }
        else if ("categorical".equalsIgnoreCase(type) && userValue != null) {
            if (criterion.getCategoricalValues() != null && criterion.getCategoricalValues().contains(userValue)) {
                score = 1.0; // exact match
            } else {
                score = 0.0;
            }
        }
        else if ("categorical_multiple_all".equalsIgnoreCase(type) && userValue != null) {
            if (criterion.getCategoricalValues() != null && criterion.getCategoricalValues().contains(userValue)) {
                score = 1.0;
            } else {
                score = 0.0;
            }
        }

        return score;
    }


    private static double parseFlexibleNumber(String raw, String field) {
        raw = raw.trim().toLowerCase();
        if (field.contains("memory")) {
            if (raw.endsWith("gb")) return Double.parseDouble(raw.replace("gb", "")) * 1024;
            if (raw.endsWith("mb")) return Double.parseDouble(raw.replace("mb", ""));
            if (raw.endsWith("kb")) return Double.parseDouble(raw.replace("kb", "")) / 1024;
        }
        if (field.contains("timeout") || field.contains("latency")) {
            if (raw.endsWith("ms")) return Double.parseDouble(raw.replace("ms", "")) / 1000;
            if (raw.endsWith("s")) return Double.parseDouble(raw.replace("s", ""));
            if (raw.endsWith("m")) return Double.parseDouble(raw.replace("m", "")) * 60;
        }
        return Double.parseDouble(raw.replaceAll("[^0-9.]", ""));
    }
}
