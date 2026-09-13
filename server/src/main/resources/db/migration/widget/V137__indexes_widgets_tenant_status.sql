CREATE INDEX idx_widgets_tenant_status ON widget.widgets(tenant_id, status);

DROP INDEX widget.idx_widgets_tenant_id;
