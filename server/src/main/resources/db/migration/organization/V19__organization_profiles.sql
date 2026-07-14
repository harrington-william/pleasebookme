CREATE TABLE organization.profiles (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_profiles_uid UNIQUE (uid),
    CONSTRAINT uq_profiles_user_organization UNIQUE (user_id, organization_id),
    CONSTRAINT uq_profiles_username_organization UNIQUE (username, organization_id),

    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_organization FOREIGN KEY (organization_id) REFERENCES organization.organizations(id) ON DELETE CASCADE
);
