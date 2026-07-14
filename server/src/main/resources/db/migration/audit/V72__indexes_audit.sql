CREATE INDEX idx_audit_events_actor_id ON audit.audit_events(actor_id);
CREATE INDEX idx_audit_events_organization_id ON audit.audit_events(organization_id);
CREATE INDEX idx_audit_events_occurred_at ON audit.audit_events(occurred_at);
CREATE INDEX idx_audit_events_event_domain ON audit.audit_events(event_domain);
CREATE INDEX idx_audit_events_event_type ON audit.audit_events(event_type);
CREATE INDEX idx_audit_events_status ON audit.audit_events(status);
CREATE INDEX idx_audit_events_severity ON audit.audit_events(severity);
CREATE INDEX idx_audit_events_correlation_id ON audit.audit_events(correlation_id);
CREATE INDEX idx_audit_events_trace_id ON audit.audit_events(trace_id);

CREATE INDEX idx_audit_resources_event_id ON audit.audit_resources(event_id);
CREATE INDEX idx_audit_resources_resource_type ON audit.audit_resources(resource_type);
CREATE INDEX idx_audit_resources_resource_uid ON audit.audit_resources(resource_uid);

CREATE INDEX idx_audit_changes_resource_id ON audit.audit_changes(resource_id);
CREATE INDEX idx_audit_changes_field_name ON audit.audit_changes(field_name);
CREATE INDEX idx_audit_changes_occurred_at ON audit.audit_changes(occurred_at);
