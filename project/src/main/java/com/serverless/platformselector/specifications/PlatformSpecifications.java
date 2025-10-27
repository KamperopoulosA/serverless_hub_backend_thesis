package com.serverless.platformselector.specifications;

import com.serverless.platformselector.entity.Platform;
import org.springframework.data.jpa.domain.Specification;

public class PlatformSpecifications {

    public static Specification<Platform> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Platform> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (category == null || category.trim().isEmpty()) return null;
            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Platform> descriptionContains(String description) {
        return (root, query, cb) -> {
            if (description == null || description.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    // Αν θες άλλα πεδία, πρόσθεσέ τα με παρόμοιο τρόπο
}

