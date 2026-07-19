-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 12 - Indexes - Notification
-- =====================================================

CREATE INDEX idx_notifications_tenant_id ON notification.notifications(tenant_id);
CREATE INDEX idx_notifications_recipient_uid ON notification.notifications(recipient_uid);
CREATE INDEX idx_notifications_status ON notification.notifications(status);
CREATE INDEX idx_notifications_scheduled_at ON notification.notifications(scheduled_at);
CREATE INDEX idx_notifications_created_at ON notification.notifications(created_at);
