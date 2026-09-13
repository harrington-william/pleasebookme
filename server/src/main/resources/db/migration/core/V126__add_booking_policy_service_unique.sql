ALTER TABLE core.booking_policies
ADD CONSTRAINT uq_booking_policies_service UNIQUE (service_id);