package com.pleasebookme.server.widget.widgets;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import com.pleasebookme.server.widget.widgets.specification.WidgetSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class WidgetSpecificationsTest {
    private static final BigInteger TENANT_ID = BigInteger.ONE;

    @Test
    void allOf_composesWhenEveryOptionalFilterIsAbsent() {
        assertThatCode(() -> Specification.allOf(
            WidgetSpecifications.hasTenant(TENANT_ID),
            WidgetSpecifications.isNotRevoked(),
            WidgetSpecifications.hasType(null),
            WidgetSpecifications.hasStatus(null)
        )).doesNotThrowAnyException();
    }

    @Test
    void allOf_composesWithEveryFilterSupplied() {
        assertThatCode(() -> Specification.allOf(
            WidgetSpecifications.hasTenant(TENANT_ID),
            WidgetSpecifications.isNotRevoked(),
            WidgetSpecifications.hasType(WidgetType.INLINE),
            WidgetSpecifications.hasStatus(WidgetStatus.ACTIVE)
        )).doesNotThrowAnyException();
    }

    @Test
    void optionalFactories_neverReturnNull() {
        assertThat(WidgetSpecifications.hasType(null)).isNotNull();
        assertThat(WidgetSpecifications.hasStatus(null)).isNotNull();
    }
}
