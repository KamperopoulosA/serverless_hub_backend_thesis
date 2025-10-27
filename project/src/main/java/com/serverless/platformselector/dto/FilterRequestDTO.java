package com.serverless.platformselector.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "DTO for filtering and sorting platforms")
public class FilterRequestDTO {
    
    @Schema(description = "Numeric filters with criteria and values")
    private Map<String, NumericFilter> numericFilters;
    
    @Schema(description = "Categorical filters")
    private List<String> categoricalFilters;
    
    @Schema(description = "Weights for ranking criteria")
    private Map<String, Double> weights;
    
    @Schema(description = "Page number for pagination", example = "0")
    private int page = 0;

    private String name;
    private String category;
    private String description;
    private String keyword;


    @Schema(description = "Page size for pagination", example = "10")
    private int size = 10;

    @Schema(description = "Sort field", example = "name")
    private String sortBy = "rank";
    
    @Schema(description = "Sort direction", example = "DESC")
    private String sortDirection = "DESC";

    // Inner class for numeric filters
    public static class NumericFilter {
        @Schema(description = "Minimum value")
        private Double min;
        
        @Schema(description = "Maximum value")
        private Double max;
        
        @Schema(description = "Exact value")
        private Double exact;
        
        // Constructors
        public NumericFilter() {}
        
        public NumericFilter(Double min, Double max, Double exact) {
            this.min = min;
            this.max = max;
            this.exact = exact;
        }
        
        // Getters and Setters
        public Double getMin() { return min; }
        public void setMin(Double min) { this.min = min; }
        
        public Double getMax() { return max; }
        public void setMax(Double max) { this.max = max; }
        
        public Double getExact() { return exact; }
        public void setExact(Double exact) { this.exact = exact; }
    }
    
    // Constructors
    public FilterRequestDTO() {}
    
    // Getters and Setters
    public Map<String, NumericFilter> getNumericFilters() { return numericFilters; }
    public void setNumericFilters(Map<String, NumericFilter> numericFilters) { this.numericFilters = numericFilters; }
    
    public List<String> getCategoricalFilters() { return categoricalFilters; }
    public void setCategoricalFilters(List<String> categoricalFilters) { this.categoricalFilters = categoricalFilters; }
    
    public Map<String, Double> getWeights() { return weights; }
    public void setWeights(Map<String, Double> weights) { this.weights = weights; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
