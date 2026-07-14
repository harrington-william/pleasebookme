CREATE TABLE organization.organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    logo_url TEXT,
    banner_url TEXT,
    bio TEXT,
    is_private BOOLEAN NOT NULL DEFAULT false,
    metadata JSONB,
    timezone VARCHAR(100) NOT NULL DEFAULT 'Australia/Sydney',
    week_start public.week_start NOT NULL DEFAULT 'MONDAY',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_organizations_slug UNIQUE (slug)
);
