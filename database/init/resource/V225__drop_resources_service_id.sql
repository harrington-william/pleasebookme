-- Service assignments now live exclusively in resource.resource_services so a
-- resource can participate in more than one bookable service.
ALTER TABLE resource.resources DROP COLUMN service_id;
