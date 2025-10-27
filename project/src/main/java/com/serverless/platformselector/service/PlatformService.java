package com.serverless.platformselector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.dto.*;
import com.serverless.platformselector.entity.Criterion;
import com.serverless.platformselector.entity.Platform;
import com.serverless.platformselector.entity.PlatformFeatures;
import com.serverless.platformselector.exception.ResourceNotFoundException;
import com.serverless.platformselector.mapper.PlatformWithRankRowMapper;
import com.serverless.platformselector.repository.PlatformRepository;
import com.serverless.platformselector.specifications.PlatformSpecifications;
import com.serverless.platformselector.util.PlatformRankingUtil;
import com.serverless.platformselector.util.PlatformType;
import com.serverless.platformselector.util.PlatformWeightsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlatformService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private PlatformRepository platformRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public PlatformService(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
    
    public List<PlatformDTO> getAllPlatforms() {
        logger.info("Retrieving all platforms");
        return platformRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public PlatformDTO getPlatformById(UUID id) {
        logger.info("Retrieving platform with id: {}", id);
        Platform platform = platformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + id));
        return convertToDTO(platform);
    }
    
    public PlatformDTO createPlatform(PlatformCreateDTO createDTO) {
        logger.info("Creating new platform: {}", createDTO.getName());
        
        Platform platform = new Platform();
        platform.setName(createDTO.getName());
        platform.setDescription(createDTO.getDescription());
        platform.setCategory(createDTO.getCategory());
        platform.setFeaturesJson(createDTO.getFeaturesJson());
        
        Platform savedPlatform = platformRepository.save(platform);
        logger.info("Platform created successfully with id: {}", savedPlatform.getId());
        
        return convertToDTO(savedPlatform);
    }
    
    public PlatformDTO updatePlatform(UUID id, PlatformUpdateDTO updateDTO) {
        logger.info("Updating platform with id: {}", id);
        
        Platform platform = platformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + id));
        
        if (updateDTO.getName() != null) {
            platform.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            platform.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getCategory() != null) {
            platform.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getFeaturesJson() != null) {
            platform.setFeaturesJson(updateDTO.getFeaturesJson());
        }
        
        Platform updatedPlatform = platformRepository.save(platform);
        logger.info("Platform updated successfully with id: {}", updatedPlatform.getId());
        
        return convertToDTO(updatedPlatform);
    }
    
    public void deletePlatform(UUID id) {
        logger.info("Deleting platform with id: {}", id);
        
        if (!platformRepository.existsById(id)) {
            throw new ResourceNotFoundException("Platform not found with id: " + id);
        }
        
        platformRepository.deleteById(id);
        logger.info("Platform deleted successfully with id: {}", id);
    }

    public Page<PlatformDTO> searchPlatforms(FilterRequestDTO request) {
        Specification<Platform> spec = Specification.where(null);

        // ✅ Full-text search (keyword σε όλα τα πεδία)
        if (hasText(request.getKeyword())) {
            String keyword = "%" + request.getKeyword().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("category")), keyword),
                    cb.like(cb.lower(root.get("description")), keyword)
            ));
        }

        // ✅ Optional name filter
        if (hasText(request.getName())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
        }

        // ✅ Optional category filter
        if (hasText(request.getCategory())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("category")), "%" + request.getCategory().toLowerCase() + "%"));
        }

        // ✅ Optional description filter
        if (hasText(request.getDescription())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("description")), "%" + request.getDescription().toLowerCase() + "%"));
        }

        // Sorting
        Sort sort = request.getSortDirection().equalsIgnoreCase("DESC") ?
                Sort.by(request.getSortBy()).descending() :
                Sort.by(request.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Platform> platforms = platformRepository.findAll(spec, pageable);
         
        return platforms.map(this::convertToDTO);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private PlatformDTO convertToDTO(Platform platform) {
        return new PlatformDTO(
            platform.getId(),
            platform.getName(),
            platform.getDescription(),
            platform.getCategory(),
            platform.getFeaturesJson(),
            platform.getCreatedAt(),
            platform.getUpdatedAt()
        );
    }

    public Page<Platform> searchPlatformsFullText(FilterRequestDTO filterRequest) {
        System.out.println("=== FULLTEXT SEARCH CALLED ===");
        System.out.println("Incoming FilterRequestDTO: " + filterRequest);

        // Set defaults if frontend sends minimal data
        String keyword = filterRequest.getKeyword() != null ? filterRequest.getKeyword() : "";
        List<String> categories = filterRequest.getCategoricalFilters() != null
                ? filterRequest.getCategoricalFilters()
                : Collections.emptyList();

        int page = filterRequest.getPage();   // primitives are never null
        int size = filterRequest.getSize();
        int offset = page * size;

        System.out.println("Keyword: '" + keyword + "', Categories: " + categories + ", Page: " + page + ", Size: " + size);

        // Build base SQL
        StringBuilder sql = new StringBuilder("""
        SELECT p.*,
            ts_rank_cd(
                setweight(to_tsvector('english', coalesce(p.name, '')), 'A') ||
                setweight(to_tsvector('english', coalesce(p.description, '')), 'B') ||
                setweight(to_tsvector('english', coalesce(p.category, '')), 'C'),
                plainto_tsquery('english', :keyword)
            ) AS rank
        FROM platforms p
        WHERE (:keyword = '' OR
               to_tsvector('english', coalesce(p.name, '') || ' ' || coalesce(p.description, '') || ' ' || coalesce(p.category, ''))
               @@ plainto_tsquery('english', :keyword))
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("keyword", keyword);
        params.addValue("size", size);
        params.addValue("offset", offset);

        // Only add category filter if not empty
        if (!categories.isEmpty()) {
            sql.append(" AND p.category = ANY(:categories) ");
            params.addValue("categories", categories.toArray(new String[0]));
        }

        sql.append(" LIMIT :size OFFSET :offset");

        System.out.println("SQL Parameters: " + params.getValues());

        List<PlatformWithRank> platformWithRankList = namedParameterJdbcTemplate.query(
                sql.toString(), params, new PlatformWithRankRowMapper()
        );

        System.out.println("Query returned " + platformWithRankList.size() + " results.");
        platformWithRankList.forEach(p -> System.out.println("  → " + p));

        // Sort results based on request
        Comparator<PlatformWithRank> comparator = getPlatformWithRankComparator(filterRequest);
        platformWithRankList.sort(comparator);

        List<Platform> platforms = platformWithRankList.stream()
                .map(PlatformWithRank::getPlatform)
                .collect(Collectors.toList());

        System.out.println("Returning " + platforms.size() + " platforms to the frontend.");
        return new PageImpl<>(platforms, PageRequest.of(page, size), platforms.size());
    }





    private static Comparator<PlatformWithRank> getPlatformWithRankComparator(FilterRequestDTO filterRequest) {
        Comparator<PlatformWithRank> comparator;

        if ("rank".equalsIgnoreCase(filterRequest.getSortBy())) {
            comparator = Comparator.comparingDouble(PlatformWithRank::getRank);
        } else if ("name".equalsIgnoreCase(filterRequest.getSortBy())) {
            comparator = Comparator.comparing(pwr -> pwr.getPlatform().getName(), String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(pwr -> pwr.getPlatform().getName());
        }

        if ("DESC".equalsIgnoreCase(filterRequest.getSortDirection())) {
            comparator = comparator.reversed();
        }

        return comparator;
    }



    public List<String> getNameSuggestions(String prefix) {
        return platformRepository.findNameSuggestions(prefix);
    }

    private double computeCriterionScore(JsonNode featuresJson, Criterion criterion) {
        String critName = criterion.getName();
        String type = criterion.getType();
        String direction = criterion.getDirection();

        JsonNode valueNode = featuresJson.get(critName);
        if (valueNode == null) {
            return 0.0; // ή άλλη λογική
        }

        if ("numeric".equalsIgnoreCase(type)) {
            double value = valueNode.asDouble();
            double min = criterion.getMinValue() != null ? criterion.getMinValue() : 0.0;
            double max = criterion.getMaxValue() != null ? criterion.getMaxValue() : 1.0;
            if ("positive".equalsIgnoreCase(direction)) {
                if (max == min) return 1.0;
                return (value - min) / (max - min);
            } else if ("negative".equalsIgnoreCase(direction)) {
                if (max == min) return 1.0;
                return (max - value) / (max - min);
            }
        } else if ("categorical".equalsIgnoreCase(type)) {
            // Υποθέτω ordered list κατηγορικών τιμών
            int index = criterion.getCategoricalValues().indexOf(valueNode.asText());
            if (index < 0) return 0.0;
            int maxIndex = criterion.getCategoricalValues().size() - 1;
            return ((double) index + 1) / (maxIndex + 1);
        }
        // Μπορείς να επεκτείνεις με άλλους τύπους...

        return 0.0;
    }

    public List<PlatformDTO> rankPlatformsByPerformanceCriteria(
            List<Platform> platforms,
            List<CriterionRequestDTO> criteria,
            PlatformType platformType) {

        Map<String, Double> weights = PlatformWeightsConfig.getWeightsForPlatform(platformType);

        return platforms.stream()
                .map(platform -> {
                    platform.setFeatures(convertJsonToFeatures(platform.getFeaturesJson()));
                    double score = PlatformRankingUtil.calculateScore(criteria, weights);
                    PlatformDTO dto = convertToDTO(platform);
                    dto.setTotalScore(score);
                    return dto;
                })
                .sorted((d1, d2) -> Double.compare(d2.getTotalScore(), d1.getTotalScore()))
                .collect(Collectors.toList());
    }




    public List<Platform> getAllPlatformsEntities() {
        logger.info("Retrieving all platform entities");
        return platformRepository.findAll();
    }

    public List<PlatformRankingResult> rankPlatformsGroupedByType(
            List<Platform> platforms,
            List<CriterionRequestDTO> criteria) {

        List<PlatformRankingResult> results = new ArrayList<>();

        for (PlatformType platformType : PlatformType.values()) {
            // Φιλτράρω τις πλατφόρμες για το συγκεκριμένο τύπο
            List<Platform> filteredPlatforms = platforms.stream()
                    .filter(p -> p.getName().equals(platformType.name()))
                    .collect(Collectors.toList());

            // Παίρνω τα weights για το συγκεκριμένο platformType
            Map<String, Double> weights = PlatformWeightsConfig.getWeightsForPlatform(platformType);

            // Κάνω το ranking με βάση τα κριτήρια και τα βάρη
            List<PlatformDTO> rankedPlatforms = rankPlatformsByPerformanceCriteria(filteredPlatforms, criteria, platformType);

            // Δημιουργώ ένα DTO που κρατά το platformType + rankedPlatforms
            PlatformRankingResult result = new PlatformRankingResult();
            result.setPlatformType(platformType);
            result.setRankedPlatforms(rankedPlatforms);

            results.add(result);
        }

        return results;
    }


    private PlatformFeatures convertJsonToFeatures(JsonNode featuresJson) {
        PlatformFeatures features = new PlatformFeatures();
        if (featuresJson == null) return features;

        featuresJson.fields().forEachRemaining(entry -> {
            features.put(entry.getKey(), entry.getValue().asText());
        });

        return features;
    }


}