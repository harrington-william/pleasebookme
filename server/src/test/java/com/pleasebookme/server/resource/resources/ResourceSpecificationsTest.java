package com.pleasebookme.server.resource.resources;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.specification.ResourceSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ResourceSpecificationsTest {
    private static final BigInteger ORGANIZATION_ID = BigInteger.valueOf(1);

    /**
     * Spring Data JPA 4.x asserts non-null inside {@code Specification.and}, so a
     * factory returning {@code null} for an absent filter fails the whole
     * composition. That surfaced as an empty-bodied 401 rather than a 500, which
     * the client read as an expired session.
     */
    @Test
    void allOf_composesWhenEveryOptionalFilterIsAbsent() {
        assertThatCode(() -> Specification.allOf(
            ResourceSpecifications.hasOrganization(ORGANIZATION_ID),
            ResourceSpecifications.hasResourceType(null),
            ResourceSpecifications.hasStatus(null),
            ResourceSpecifications.matchesText(null)
        )).doesNotThrowAnyException();
    }

    @Test
    void allOf_composesWithEveryFilterSupplied() {
        assertThatCode(() -> Specification.allOf(
            ResourceSpecifications.hasOrganization(ORGANIZATION_ID),
            ResourceSpecifications.hasResourceType(BigInteger.valueOf(8)),
            ResourceSpecifications.hasStatus(ResourceStatus.ACTIVE),
            ResourceSpecifications.matchesText("room")
        )).doesNotThrowAnyException();
    }

    @Test
    void optionalFactories_neverReturnNull() {
        assertThat(ResourceSpecifications.hasResourceType(null)).isNotNull();
        assertThat(ResourceSpecifications.hasStatus(null)).isNotNull();
        assertThat(ResourceSpecifications.matchesText(null)).isNotNull();
        assertThat(ResourceSpecifications.matchesText("   ")).isNotNull();
    }

    @Test
    void allOf_composesWhenTextIsBlank() {
        Specification<ResourceEntity> specification = Specification.allOf(
            ResourceSpecifications.hasOrganization(ORGANIZATION_ID),
            ResourceSpecifications.matchesText("   ")
        );

        assertThat(specification).isNotNull();
    }
}
