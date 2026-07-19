ALTER TABLE auth.refresh_tokens ADD CONSTRAINT fk_refresh_token_widget
    FOREIGN KEY (widget_id)
    REFERENCES widget.widgets(id)
    ON DELETE SET NULL;
