CREATE TABLE auth.user_passwords (
    user_id BIGINT PRIMARY KEY,
    raw TEXT NOT NULL,
    hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_password_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);
