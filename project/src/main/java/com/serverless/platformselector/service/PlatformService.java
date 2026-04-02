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
import com.serverless.platformselector.util.PlatformRankingUtil;
import com.serverless.platformselector.util.PlatformType;
import com.serverless.platformselector.util.PlatformWeightsConfig;
import io.micrometer.tracing.annotation.SpanTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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
               @@ plainto_tsquery('english', :keyword)
               OR similarity(p.name, :keyword) > 0.25)
               
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



    public List<PlatformDTO> rankPlatformsByPerformanceCriteria(
            List<Platform> platforms,
            List<CriterionRequestDTO> criteria) {

        return platforms.stream()
                .map(platform -> {
                    // Μετατροπή JSON → PlatformFeatures (normalized)
                    PlatformFeatures features = normalizeFeatures(
                            convertJsonToFeatures(platform.getFeaturesJson())
                    );
                    platform.setFeatures(features);

                    double score = PlatformRankingUtil.calculateScore(platform, platforms, criteria);

                    PlatformDTO dto = convertToDTO(platform);
                    dto.setTotalScore(score);
                    return dto;
                })
                .sorted(Comparator.comparingDouble(PlatformDTO::getTotalScore).reversed())
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

            // Φιλτράρουμε τις πλατφόρμες ανά TYPΟ / CATEGORY, ΟΧΙ με βάση το όνομα
            List<Platform> filteredPlatforms = platforms.stream()
                    .filter(p -> p.getName() != null &&
                            p.getName().equalsIgnoreCase(platformType.name()))
                    .collect(Collectors.toList());

            if (filteredPlatforms.isEmpty()) {
                continue; // αν δεν έχουμε καμία πλατφόρμα αυτού του τύπου, προχώρα στον επόμενο
            }

            // Κάνουμε ranking με βάση τα criteria & τα weights που δίνει ο χρήστης
            List<PlatformDTO> rankedPlatforms =
                    rankPlatformsByPerformanceCriteria(filteredPlatforms, criteria);

            PlatformRankingResult result = new PlatformRankingResult();
            result.setPlatformType(platformType);
            result.setRankedPlatforms(rankedPlatforms);

            results.add(result);
        }

        return results;
    }




    private PlatformFeatures convertJsonToFeatures(JsonNode json) {

        PlatformFeatures features = new PlatformFeatures();

        if (json == null || json.isNull()) return features;

        json.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode valueNode = entry.getValue();

            Object value;

            if (valueNode.isNumber()) {
                value = valueNode.numberValue();
            }
            else if (valueNode.isArray()) {
                List<String> list = new ArrayList<>();
                valueNode.forEach(v -> list.add(v.asText()));
                value = list;
            }
            else {
                value = valueNode.asText();
            }

            features.addFeature(key, value);
        });

        return features;
    }


    private PlatformFeatures normalizeFeatures(PlatformFeatures raw) {

        PlatformFeatures normalized = new PlatformFeatures();

        if (raw == null || raw.toMap().isEmpty()) {
            return normalized;
        }

        raw.toMap().forEach((key, value) -> {

            if (value instanceof String) {
                normalized.addFeature(key.toLowerCase(), ((String) value).trim());
            }
            else if (value instanceof Number) {
                normalized.addFeature(key.toLowerCase(), value);
            }
            else if (value instanceof List) {
                normalized.addFeature(key.toLowerCase(), value);
            }
            else {
                normalized.addFeature(key.toLowerCase(), value.toString());
            }

        });

        return normalized;
    }



}