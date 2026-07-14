CREATE INDEX idx_accounts_user_id ON auth.accounts(user_id);
CREATE INDEX idx_accounts_type ON auth.accounts(type);

CREATE INDEX idx_api_keys_tenant_id ON auth.api_keys(tenant_id);
CREATE INDEX idx_api_keys_owner_user_id ON auth.api_keys(owner_user_id);
CREATE INDEX idx_api_keys_status ON auth.api_keys(status);
CREATE INDEX idx_api_keys_expires_at ON auth.api_keys(expires_at);
