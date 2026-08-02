ALTER TABLE integration.destination_calendars ADD COLUMN oauth_connection_id BIGINT;

ALTER TABLE integration.destination_calendars ADD CONSTRAINT fk_destination_calendar_oauth_connection
    FOREIGN KEY (oauth_connection_id)
    REFERENCES integration.oauth_connections(id)
    ON DELETE CASCADE;

ALTER TABLE integration.destination_sheets ADD COLUMN oauth_connection_id BIGINT;

ALTER TABLE integration.destination_sheets ADD CONSTRAINT fk_destination_sheet_oauth_connection
    FOREIGN KEY (oauth_connection_id)
    REFERENCES integration.oauth_connections(id)
    ON DELETE CASCADE;
