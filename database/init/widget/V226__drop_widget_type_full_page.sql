-- FULL_PAGE is a hosted public page, not an embeddable widget row. Existing
-- rows are defensively converted even though none are expected in PLATFORM V1.
UPDATE widget.widgets SET type = 'EMBEDDED' WHERE type = 'FULL_PAGE';

-- PostgreSQL cannot remove an enum value, so recreate the type without it.
CREATE TYPE widget.widget_type_new AS ENUM (
    'INLINE',
    'POPUP',
    'EMBEDDED'
);

-- The default depends on the old enum and must be removed before the cast.
ALTER TABLE widget.widgets ALTER COLUMN type DROP DEFAULT;

ALTER TABLE widget.widgets
ALTER COLUMN type TYPE widget.widget_type_new
USING type::text::widget.widget_type_new;

ALTER TABLE widget.widgets ALTER COLUMN type SET DEFAULT 'EMBEDDED';

DROP TYPE widget.widget_type;

-- Preserve the established type name for application and schema consumers.
ALTER TYPE widget.widget_type_new RENAME TO widget_type;
