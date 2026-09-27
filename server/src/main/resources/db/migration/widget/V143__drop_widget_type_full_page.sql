UPDATE widget.widgets SET type = 'EMBEDDED' WHERE type = 'FULL_PAGE';

CREATE TYPE widget.widget_type_new AS ENUM (
    'INLINE',
    'POPUP',
    'EMBEDDED'
);

ALTER TABLE widget.widgets ALTER COLUMN type DROP DEFAULT;

ALTER TABLE widget.widgets
ALTER COLUMN type TYPE widget.widget_type_new
USING type::text::widget.widget_type_new;

ALTER TABLE widget.widgets ALTER COLUMN type SET DEFAULT 'EMBEDDED';

DROP TYPE widget.widget_type;

ALTER TYPE widget.widget_type_new RENAME TO widget_type;
