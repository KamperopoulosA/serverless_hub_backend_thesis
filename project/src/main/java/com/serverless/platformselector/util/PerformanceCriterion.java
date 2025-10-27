package com.serverless.platformselector.util;

import lombok.Data;
import lombok.Setter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum PerformanceCriterion {
    COLD_START_LATENCY("ColdStartLatency", 0.25),
    WARM_INVOCATION_LATENCY("WarmInvocationLatency", 0.15),
    THROUGHPUT("Throughput", 0.15),
    SCALABILITY_SPEED("ScalabilitySpeed", 0.10),
    RESOURCE_ALLOCATION_EFFICIENCY("ResourceAllocationEfficiency", 0.08),
    EXECUTION_TIME_VARIABILITY("ExecutionTimeVariability", 0.08),
    CONCURRENCY_LIMITS("ConcurrencyLimits", 0.07),
    NETWORK_IO_LATENCY("NetworkIOLatency", 0.07),
    DATA_TRANSFER_THROUGHPUT("DataTransferThroughput", 0.05);

    private final String criterionName;
    private final double weight;

    PerformanceCriterion(String criterionName, double weight) {
        this.criterionName = criterionName;
        this.weight = weight;
    }

    public String getCriterionName() {
        return criterionName;
    }

    public double getWeight() {
        return weight;
    }

    // Βοηθητική μέθοδος να φτιάξεις Map<String, Double> από το enum
    public static Map<String, Double> getWeightsMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(PerformanceCriterion::getCriterionName, PerformanceCriterion::getWeight));
    }
}
