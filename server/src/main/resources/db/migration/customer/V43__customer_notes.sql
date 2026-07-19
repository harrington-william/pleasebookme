CREATE TABLE customer.customer_notes (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    author_user_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_customer_note_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer.customers(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_customer_note_author
        FOREIGN KEY (author_user_id)
        REFERENCES auth.users(id)
        ON DELETE SET NULL
);
