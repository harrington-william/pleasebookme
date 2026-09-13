-- Some assets need neither free-text detail nor a meaningful numeric capacity.
ALTER TABLE resource.resources ALTER COLUMN description DROP NOT NULL;

ALTER TABLE resource.resources ALTER COLUMN capacity DROP NOT NULL;
