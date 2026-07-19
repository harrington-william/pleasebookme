CREATE INDEX idx_schedules_user_id ON core.schedules(user_id);

CREATE INDEX idx_availabilities_user_id ON core.availabilities(user_id);
CREATE INDEX idx_availabilities_schedule_id ON core.availabilities(schedule_id);

CREATE INDEX idx_services_user_id ON core.services(user_id);
CREATE INDEX idx_services_profile_id ON core.services(profile_id);
CREATE INDEX idx_services_schedule_id ON core.services(schedule_id);

CREATE INDEX idx_bookings_service_id ON core.bookings(service_id);
CREATE INDEX idx_bookings_user_id ON core.bookings(user_id);
CREATE INDEX idx_bookings_destination_calendar_id ON core.bookings(destination_calendar_id);
CREATE INDEX idx_bookings_status ON core.bookings(status);
CREATE INDEX idx_bookings_start_end_status ON core.bookings(start_time, end_time, status);
CREATE INDEX idx_bookings_user_end_time ON core.bookings(user_id, end_time);
CREATE INDEX idx_bookings_user_status_start ON core.bookings(user_id, status, start_time);
CREATE INDEX idx_bookings_service_status ON core.bookings(service_id, status);
CREATE INDEX idx_bookings_user_created_at ON core.bookings(user_id, created_at);

CREATE INDEX idx_attendees_email ON core.attendees(email);
CREATE INDEX idx_attendees_phone ON core.attendees(phone);
CREATE INDEX idx_attendees_booking_id ON core.attendees(booking_id);
CREATE INDEX idx_attendees_email_booking ON core.attendees(email, booking_id);
CREATE INDEX idx_attendees_phone_booking ON core.attendees(phone, booking_id);

CREATE INDEX idx_out_of_office_user_id ON core.out_of_office(user_id);
CREATE INDEX idx_out_of_office_to_user_id ON core.out_of_office(to_user_id);
CREATE INDEX idx_out_of_office_start_end ON core.out_of_office(start_time, end_time);
