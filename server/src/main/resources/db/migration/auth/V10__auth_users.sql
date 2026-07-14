CREATE TABLE auth.users (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    bio TEXT,
    avatar_url TEXT,
    locale public.locale NOT NULL DEFAULT 'en',
    timezone VARCHAR(100) NOT NULL DEFAULT 'Australia/Sydney',
    theme public.theme NOT NULL DEFAULT 'DARK',
    week_start public.week_start NOT NULL DEFAULT 'MONDAY',
    account_status auth.account_status NOT NULL DEFAULT 'ACTIVE',
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_uid UNIQUE (uid),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone)
);
