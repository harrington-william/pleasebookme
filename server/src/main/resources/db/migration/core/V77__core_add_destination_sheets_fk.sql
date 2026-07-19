ALTER TABLE core.services ADD COLUMN destination_sheets_id BIGINT;

ALTER TABLE core.services ADD CONSTRAINT fk_service_destination_sheets
    FOREIGN KEY (destination_sheets_id)
    REFERENCES integration.destination_sheets(id)
    ON DELETE SET NULL;