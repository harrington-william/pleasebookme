package com.pleasebookme.server.resource.resources.specification;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;

/**
 * Query predicates for the organization-scoped resource list.
 *
 * <p>Every optional predicate returns {@link Specification#unrestricted()} when
 * its filter is absent, so callers can compose the full filter set
 * unconditionally instead of branching per parameter.
 *
 * <p>Returning {@code null} here instead would be the Spring Data JPA 3.x idiom
 * and throws on 4.x: {@code Specification.and} asserts its argument is non-null,
 * so a single absent filter fails the whole query.
 */
public final class ResourceSpecifications {
    private ResourceSpecifications() {}

    public static Specification<ResourceEntity> hasOrganization(BigInteger organizationId) {
        return (root, query, builder) -> builder.equal(
            root.get("organization").get("organizationId"),
            organizationId
        );
    }

    public static Specification<ResourceEntity> hasResourceType(BigInteger resourceTypeId) {
        if (resourceTypeId == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.equal(
            root.get("resourceType").get("resourceTypeId"),
            resourceTypeId
        );
    }

    public static Specification<ResourceEntity> hasStatus(ResourceStatus status) {
        if (status == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    /**
     * Case-insensitive contains-match across name and slug.
     *
     * <p>Wildcards in the caller's text are escaped so a search for {@code "100%"}
     * matches literally instead of degrading into a match-everything pattern.
     */
    public static Specification<ResourceEntity> matchesText(String text) {
        if (text == null || text.isBlank()) {
            return Specification.unrestricted();
        }

        String pattern = "%" + escapeLikeWildcards(text.trim().toLowerCase()) + "%";

        return (root, query, builder) -> builder.or(
            builder.like(builder.lower(root.get("name")), pattern, '\\'),
            builder.like(builder.lower(root.get("slug")), pattern, '\\')
        );
    }

    private static String escapeLikeWildcards(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}
