-- Mirrored to Flyway as V144__tenant_seed_barbershop_ecosystem.sql.
--
-- THIS MIGRATION IS A NO-OP ON EVERY EXISTING DATABASE, and that was not known
-- when it was written. V100__seed_ecosystems.sql already inserts BARBERSHOP
-- (name 'Barbershops'), so the ON CONFLICT clause below swallows this insert and
-- the name/description here never take effect. TASK-0012 asked for the seed
-- without checking for an earlier one.
--
-- It is kept rather than deleted because V144 has already been applied: removing
-- a migration below the schema high-water mark makes Flyway's validate fail at
-- boot with "Detected applied migration not resolved locally", which surfaces as
-- an opaque entityManagerFactory bean-creation error. A harmless no-op costs
-- less than that. Do not edit the Flyway copy either — its checksum is recorded.
--
-- If the BARBERSHOP row should carry the fuller description below, that is a new
-- migration performing an UPDATE, not an edit to this one.
--
-- The widget registry selects presentation by this code; provisioning still uses
-- GENERAL. icon is required by the ecosystem schema.
INSERT INTO tenant.ecosystems (code, name, description, icon, status)
VALUES ('BARBERSHOP', 'Barbershop', 'Barbers, hairdressers and salons booking chairs by appointment', '💈', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;
