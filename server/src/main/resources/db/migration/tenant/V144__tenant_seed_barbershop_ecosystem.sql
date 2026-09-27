INSERT INTO tenant.ecosystems (code, name, description, icon, status)
VALUES ('BARBERSHOP', 'Barbershop', 'Barbers, hairdressers and salons booking chairs by appointment', '💈', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;
