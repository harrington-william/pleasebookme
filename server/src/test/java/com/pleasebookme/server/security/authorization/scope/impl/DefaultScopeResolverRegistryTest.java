package com.pleasebookme.server.security.authorization.scope.impl;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.authorization.scope.ScopeResolver;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultScopeResolverRegistryTest {

    private static class BookingEntityProxy extends BookingEntity {
    }

    private <T> ScopeResolver<T> fakeResolver(Class<T> type, ResourceScope scope) {
        return new ScopeResolver<T>() {
            @Override
            public Class<T> resourceType() {
                return type;
            }

            @Override
            public ResourceScope resolve(T resource) {
                return scope;
            }
        };
    }

    @Test
    void resolve_returnsUnscoped_whenResourceIsNull() {
        DefaultScopeResolverRegistry registry = new DefaultScopeResolverRegistry(List.of());
        registry.validateNoDuplicateResourceTypes();

        assertThat(registry.resolve(null)).isEqualTo(ResourceScope.unscoped());
    }

    @Test
    void resolve_returnsUnscoped_whenNoResolverMatchesTheResourceType() {
        DefaultScopeResolverRegistry registry = new DefaultScopeResolverRegistry(List.of());
        registry.validateNoDuplicateResourceTypes();

        assertThat(registry.resolve("a string with no registered resolver"))
            .isEqualTo(ResourceScope.unscoped());
    }

    @Test
    void resolve_dispatchesToTheResolverMatchingTheResourcesExactType() {
        ResourceScope expected = ResourceScope.ofOrganization(BigInteger.TEN);
        ScopeResolver<BookingEntity> bookingResolver = fakeResolver(BookingEntity.class, expected);

        DefaultScopeResolverRegistry registry = new DefaultScopeResolverRegistry(List.of(bookingResolver));
        registry.validateNoDuplicateResourceTypes();

        assertThat(registry.resolve(new BookingEntity())).isEqualTo(expected);
    }

    @Test
    void resolve_matchesAHibernateProxyStyleSubclass_viaIsInstanceNotExactClassEquality() {
        ResourceScope expected = ResourceScope.ofOrganization(BigInteger.TEN);
        ScopeResolver<BookingEntity> bookingResolver = fakeResolver(BookingEntity.class, expected);

        DefaultScopeResolverRegistry registry = new DefaultScopeResolverRegistry(List.of(bookingResolver));
        registry.validateNoDuplicateResourceTypes();

        // Simulates a Hibernate-generated proxy: a runtime subclass of the
        // entity, not the exact declared class. A Map<Class<?>, ...> lookup
        // keyed by exact class would miss this and silently fall through to
        // unscoped(); isInstance() must not.
        assertThat(registry.resolve(new BookingEntityProxy())).isEqualTo(expected);
    }

    @Test
    void validateNoDuplicateResourceTypes_throws_whenTwoResolversTargetTheSameResourceType() {
        ScopeResolver<BookingEntity> first = fakeResolver(BookingEntity.class, ResourceScope.unscoped());
        ScopeResolver<BookingEntity> second = fakeResolver(BookingEntity.class, ResourceScope.unscoped());

        DefaultScopeResolverRegistry registry =
            new DefaultScopeResolverRegistry(List.of(first, second));

        assertThatThrownBy(registry::validateNoDuplicateResourceTypes)
            .isInstanceOf(IllegalStateException.class);
    }
}
