package com.pleasebookme.server.widget.widgets.specification;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;

/** Query predicates for the tenant-scoped, non-revoked widget list. */
public final class WidgetSpecifications {
    private WidgetSpecifications() {}

    public static Specification<WidgetEntity> hasTenant(BigInteger tenantId) {
        return (root, query, builder) -> builder.equal(
            root.get("tenant").get("tenantId"),
            tenantId
        );
    }

    public static Specification<WidgetEntity> isNotRevoked() {
        return (root, query, builder) -> builder.notEqual(root.get("status"), WidgetStatus.REVOKED);
    }

    public static Specification<WidgetEntity> hasType(WidgetType type) {
        if (type == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.equal(root.get("type"), type);
    }

    public static Specification<WidgetEntity> hasStatus(WidgetStatus status) {
        if (status == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }
}
