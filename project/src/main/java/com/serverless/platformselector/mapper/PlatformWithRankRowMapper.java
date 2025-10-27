package com.serverless.platformselector.mapper;

import com.serverless.platformselector.dto.PlatformWithRank;
import com.serverless.platformselector.entity.Platform;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class PlatformWithRankRowMapper implements RowMapper<PlatformWithRank> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PlatformWithRank mapRow(ResultSet rs, int rowNum) throws SQLException {
        Platform p = new Platform();

        // UUID: μετατροπή από string σε UUID
        String idStr = rs.getString("id");
        if (idStr != null) {
            p.setId(UUID.fromString(idStr));
        }

        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategory(rs.getString("category"));

        // features_json τύπος JSONB: διαβάζουμε ως string και κάνουμε parse με Jackson
        String featuresJsonStr = rs.getString("features_json");
        if (featuresJsonStr != null) {
            try {
                JsonNode featuresJson = objectMapper.readTree(featuresJsonStr);
                p.setFeaturesJson(featuresJson);
            } catch (Exception e) {
                // αν αποτύχει το parsing, απλά βάζουμε null ή κάνουμε logging
                p.setFeaturesJson(null);
            }
        }

        // LocalDateTime από timestamp
        java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            p.setCreatedAt(createdAtTs.toLocalDateTime());
        }

        java.sql.Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            p.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }

        // Βγάζουμε το rank από το query
        Double rank = rs.getDouble("rank");

        return new PlatformWithRank(p, rank);
    }
}
