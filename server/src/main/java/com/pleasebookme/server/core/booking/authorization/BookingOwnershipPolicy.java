package com.pleasebookme.server.core.booking.authorization;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class BookingOwnershipPolicy implements AuthorizationPolicy {

    private static final String RESTRICTED_ROLE = "STAFF";

    @Override
    public String resourceType() {
        return "BOOKING";
    }

    @Override
    public boolean supports(String action) {
        return "UPDATE".equals(action) || "CANCEL".equals(action);
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public AuthorizationDecision evaluate(AuthorizationContext context) {
        if (!(context.principal() instanceof UserPrincipal userPrincipal)) {
            return AuthorizationDecision.abstain(policyName());
        }

        if (!context.hasMembership() || !context.membership().hasRole(RESTRICTED_ROLE)) {
            return AuthorizationDecision.abstain(policyName());
        }

        BookingEntity booking = context.resourceAs(BookingEntity.class).orElse(null);

        if (booking == null) {
            return AuthorizationDecision.abstain(policyName());
        }

        if (!booking.getUser().getUserUid().equals(userPrincipal.subject())) {
            return AuthorizationDecision.deny(
                policyName(),
                "NOT_ASSIGNED_BOOKING",
                "STAFF may only " + context.action().toLowerCase() + " bookings assigned to them"
            );
        }

        return AuthorizationDecision.abstain(policyName());
    }

    private String policyName() {
        return getClass().getSimpleName();
    }
}
