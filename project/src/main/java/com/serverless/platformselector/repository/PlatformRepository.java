package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, UUID>, JpaSpecificationExecutor<Platform> {
    @Query(value = """
    SELECT p.*, 
      ts_rank_cd(
        setweight(to_tsvector('english', coalesce(p.name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(p.description, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(p.category, '')), 'C'),
        plainto_tsquery('english', :keyword)
      ) AS rank
    FROM platforms p
    WHERE (:keyword IS NULL OR 
           to_tsvector('english', coalesce(p.name, '') || ' ' || coalesce(p.description, '') || ' ' || coalesce(p.category, ''))
           @@ plainto_tsquery('english', :keyword))
      AND (:categories IS NULL OR p.category = ANY(:categories))
    LIMIT :size OFFSET :offset
    """,
            nativeQuery = true)
    List<Platform> searchFullTextWithFilters(
            @Param("keyword") String keyword,
            @Param("categories") List<String> categories,
            @Param("size") int size,
            @Param("offset") int offset);



    // Auto-completion για ονόματα (name)
    @Query(value = "SELECT DISTINCT name FROM platforms WHERE name ILIKE %:prefix% LIMIT 10", nativeQuery = true)
    List<String> findNameSuggestions(@Param("prefix") String prefix);
    Optional<Platform> findByName(String name);
    
    Page<Platform> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT p FROM Platform p WHERE p.name LIKE %:name%")
    Page<Platform> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Platform p WHERE p.category = :category AND p.name LIKE %:name%")
    Page<Platform> findByCategoryAndNameContaining(@Param("category") String category, 
                                                  @Param("name") String name, 
                                                  Pageable pageable);
    
    @Query(value = "SELECT * FROM platforms p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:name IS NULL OR p.name ILIKE %:name%)", 
           nativeQuery = true)
    Page<Platform> findWithFilters(@Param("category") String category,
                                  @Param("name") String name,
                                  Pageable pageable);
}