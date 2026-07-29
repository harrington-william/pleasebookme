ALTER TABLE auth.refresh_tokens
ALTER COLUMN owner TYPE auth.refresh_owner
USING (owner::auth.refresh_owner);