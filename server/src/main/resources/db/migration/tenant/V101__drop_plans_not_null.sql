ALTER TABLE tenant.plans ALTER COLUMN max_users DROP NOT NULL;
ALTER TABLE tenant.plans ALTER COLUMN max_services DROP NOT NULL;
ALTER TABLE tenant.plans ALTER COLUMN max_widgets DROP NOT NULL;
ALTER TABLE tenant.plans ALTER COLUMN max_resources DROP NOT NULL;
ALTER TABLE tenant.plans ALTER COLUMN max_api_keys DROP NOT NULL;