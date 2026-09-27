- Refactor availability ruleset list and build config ruleset page - Done
- Refactor create new service page and build config service page - Done
- Refactor resource page and build config resource page
- Build dashboard page - Done
- Widget page - Done
- Widget
- Debounced feature for search bar


Write documentation for the new service page implementation

Server — new endpoints

GET  /api/v1/public/organizations/{slug}

GET  /api/v1/public/organizations/{slug}/services/{serviceSlug}/slots?date=YYYY-MM-DD

POST /api/v1/public/organizations/{slug}/services/{serviceSlug}/bookings

---

# Widget Task

Explain the business model and end-to-end widget setup flow. In the first stage, we build a landing-page site for clients and we manually copy-paste the widget NextJS component from our repo to their repo. Like selling source code, because widgets use the exactly same logic to communicate with the Spring server. For ecosystems, we just simply build many variants of the widget. For example, we could have different barber variants and styles, also we could have different hotel variants and styles. The key is continuously reuse them. Then, the next step is evolve to JavaScript iframe and custom NextJS library if possible.

Ask for recommended project structure to organize by ecosystems and variants effectively.

- [x] Drop `PublicBookingController` and rename the public booking to widget — done 2026-09-22: `service/publicbooking` → `service/widget/fullpage`, `PublicBooking*` → `FullPageWidget*`/`Widget*`, the empty `core/booking/api/v1/open/PublicBookingController` stub removed
- [x] Add `/booking` prefix to the hosted page — done 2026-09-22: the client route is now `pleasebookme.app/booking/{org}/{service}`; the server path `/api/v1/public/**` is unchanged, and `ReservedOrganizationSlugs` no longer gates reads
- [ ] Consider to move `BarbershopService` to a new `booking` directory in orchestration service
- [x]  Review the widget code on the frontend