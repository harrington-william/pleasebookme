# `core`

> Owns the reservation engine — scheduling, availability, booking policies, bookings, attendees, and slot holds. Never reservable assets, customer relationships, or authentication.

## Tables

| Table               | Documentation                     |
| -------------------- | ---------------------------------------- |
| `schedules`           | [[Table Schedules]]           |
| `availabilities`       | [[Table Availabilities]]       |
| `services`             | [[Table Services]]             |
| `booking_policies`     | [[Table Booking Policies]]     |
| `bookings`             | [[Table Bookings]]             |
| `attendees`            | [[Table Attendees]]            |
| `selected_slots`       | [[Table Selected Slots]]       |
| `out_of_office`        | [[Table Out Of Office]]        |

## Related Schemas

- [[Organization Schema]]
- [[Resource Schema]]
- [[Auth Schema]]
- [[Integration Schema]]
- [[Widget Schema]]
