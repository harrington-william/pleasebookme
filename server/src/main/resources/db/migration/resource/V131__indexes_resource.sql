CREATE INDEX idx_resources_service_id ON resource.resources(service_id);
CREATE INDEX idx_resources_resource_type_id ON resource.resources(resource_type_id);
CREATE INDEX idx_resources_organization_status ON resource.resources(organization_id, status);

CREATE INDEX idx_resource_services_service_id ON resource.resource_services(service_id);

CREATE INDEX idx_resource_pricing_resource_id ON resource.resource_pricing(resource_id);
CREATE INDEX idx_resource_pricing_resource_effective ON resource.resource_pricing(resource_id, effective_from, effective_until);

CREATE INDEX idx_resource_assignments_resource_id ON resource.resource_assignments(resource_id);
CREATE INDEX idx_resource_assignments_membership_id ON resource.resource_assignments(membership_id);
CREATE INDEX idx_resource_assignments_assigned_by ON resource.resource_assignments(assigned_by);
CREATE INDEX idx_resource_assignments_released_by ON resource.resource_assignments(released_by);

CREATE INDEX idx_resource_calendars_resource_id ON resource.resource_calendars(resource_id);
CREATE INDEX idx_resource_calendars_schedule_id ON resource.resource_calendars(schedule_id);

CREATE INDEX idx_resource_maintenance_resource_id ON resource.resource_maintenance(resource_id);
CREATE INDEX idx_resource_maintenance_start_end ON resource.resource_maintenance(start_time, end_time);

CREATE INDEX idx_resource_overrides_resource_id ON resource.resource_overrides(resource_id);
CREATE INDEX idx_resource_overrides_start_end ON resource.resource_overrides(start_time, end_time);
