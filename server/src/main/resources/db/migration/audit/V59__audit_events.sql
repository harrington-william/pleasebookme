CREATE TABLE audit.audit_events (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    correlation_id VARCHAR(255),
    request_id VARCHAR(255),
    trace_id VARCHAR(255),
    tenant_id BIGINT,
    organization_id BIGINT,
    actor_id BIGINT NOT NULL,
    event_domain audit.event_domain NOT NULL,
    event_type audit.event_type NOT NULL,
    action audit.audit_action NOT NULL,
    severity audit.severity NOT NULL,
    status audit.audit_status NOT NULL,
    context JSONB,
    event_version INTEGER NOT NULL DEFAULT 1,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_audit_events_uid UNIQUE (uid),

    CONSTRAINT fk_audit_event_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant.tenants(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_audit_event_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization.organizations(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_audit_event_actor
        FOREIGN KEY (actor_id)
        REFERENCES audit.audit_actors(id)
        ON DELETE CASCADE
);
