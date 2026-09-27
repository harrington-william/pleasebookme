# PleaseBookMe widget library

This folder **is** the product we hand to a client's site. Everything under
`pleasebookme/` is copied into the client's Next.js repo as source; nothing
outside it is.

```text
pleasebookme/
  core/                 ecosystem-agnostic: transport, session, date math, primitives, tokens
  ecosystems/
    barbershop/
      logic/            headless — types, steps, zod schema, the slots hook, the state machine
      variants/
        kinetic/        presentation only — the components, and nothing else
    registry.ts         ecosystem code -> widget component
```

**Logic is per ecosystem, presentation is per variant.** A second barbershop
look is a second folder under `variants/`. A hotel is a second folder under
`ecosystems/` with its own `logic/`, because a stay is not an appointment.

---

## Installing it in a client site

### 1. Create the widget and copy the key pair

In the PleaseBookMe dashboard, create a widget for the client's organization.
The response shows `publicKey` and `secretKey` **once**. If the secret is
lost, rotate the widget's credentials — there is no way to read it back.

### 2. Put the key pair in the client site's own `.env.local`

```bash
NEXT_PUBLIC_PBM_API_URL=https://api.pleasebookme.app
NEXT_PUBLIC_PBM_PUBLIC_KEY=<publicKey>
NEXT_PUBLIC_PBM_SECRET_KEY=<secretKey>
```

That file, in the client's own repository and never committed, is the **only**
place the key pair lives outside the dashboard and the server's hashed copy.
PleaseBookMe stores no second copy, offers no vault, and has no "download my
keys again" screen.

Both values are inlined into the browser bundle at build time, so treat them
as public identifiers rather than secrets: what protects the widget is that
the server pins it to one tenant, that CORS pins it to allowed origins, and
(when an origin is registered on the widget) that bootstrap checks it. See
`SECURITY.md` → "Embedded widget channel".

### 3. Copy the library in

Copy `pleasebookme/core/` and `pleasebookme/ecosystems/barbershop/` anywhere
in the client repo. The folder is self-contained: every import inside it is
relative, so no `tsconfig` path alias is needed and no file outside it is
referenced.

### 4. Install the peer dependencies

```
react  react-dom  axios  zod  react-hook-form  @hookform/resolvers
lucide-react  clsx  tailwind-merge  tailwindcss@4
```

### 5. Import the tokens

In the site's Tailwind entry CSS:

```css
@import "tailwindcss";
@import "<path>/pleasebookme/core/styles/tokens.css";
```

If the copied folder sits outside Tailwind's auto-detected sources, also add
`@source "<path>/pleasebookme";` so its classes are scanned.

### 6. Render it

```tsx
"use client";

import { PleaseBookMeProvider } from "<path>/pleasebookme/core/auth/PleaseBookMeProvider";
import { BarbershopBookingWidget } from "<path>/pleasebookme/ecosystems/barbershop/variants/kinetic/BarbershopBookingWidget";

export default function BookingPage() {
  return (
    <PleaseBookMeProvider
      apiUrl={process.env.NEXT_PUBLIC_PBM_API_URL!}
      publicKey={process.env.NEXT_PUBLIC_PBM_PUBLIC_KEY!}
      secretKey={process.env.NEXT_PUBLIC_PBM_SECRET_KEY!}
    >
      <BarbershopBookingWidget />
    </PleaseBookMeProvider>
  );
}
```

The provider takes plain string props and never reads `process.env` itself —
the site decides how it sources configuration. `widget/app/barbershop/kinetic/page.tsx`
in this repo is the same six lines and is the reference implementation.

### 7. Allow the site's origin on the platform (ops step)

Add the client site's origin to `app.cors.allowed-origins` on the server and
redeploy. A browser cannot call the API from an origin that is not on that
list, so this step is not optional — and because onboarding is manual today,
it is a deliberate configuration change rather than an open CORS policy.

If the widget was created with a registered origin, that origin must also
equal the site's origin exactly: `https://www.barbershop.com` — scheme, host,
non-default port only, no path, no trailing slash.

The server also needs `X-Correlation-Id` in `app.cors.allowed-headers` and
`Retry-After` in `exposed-headers`. Those are global, not per-client, so they
are already configured — but if a deployment ever drops them, every widget
call fails its preflight and the widget shows "This booking page isn't
available" with no other symptom.

---

## The two rules that keep this portable

1. **Every import inside `pleasebookme/` is relative.** No `@/`, no `next/*`,
   no file from `widget/components`, `widget/lib` or `widget/app`. One `@/`
   breaks every future paste, and nothing in CI will tell you.
2. **Every colour, size and spacing value comes from `core/styles/tokens.css`.**
   Colours are declared as `--pbm-*` on `.pbm-widget` and mapped through
   `@theme inline`, so the widget is themed in its own scope and the host
   site's own `--primary` is neither read nor overwritten. Never hard-code a
   hex value in a component.

## How it talks to the server

```text
session.ts    POST /api/v1/auth/widget/bootstrap   { publicKey, secretKey, origin }  ->  { accessToken }
client.ts     Authorization: Bearer <accessToken>  on every call; one silent
              re-bootstrap and replay if a 401 comes back
widget-api.ts GET  /api/v1/widget/organization
              GET  /api/v1/widget/services/{slug}
              GET  /api/v1/widget/services/{slug}/slots?date=YYYY-MM-DD
              POST /api/v1/widget/services/{slug}/bookings
```

The access token lives for 15 minutes, in memory only — never in
`localStorage`, `sessionStorage` or a cookie. There is no refresh token for a
widget; re-bootstrapping is a single request and costs nothing. Concurrent
calls share one in-flight bootstrap rather than minting a token each.

No endpoint takes a tenant, organization or widget id: the token says which
tenant, and the server derives everything else from it.

## Where this is going

- **Next:** publish `pleasebookme/` as `@pleasebookme/widgets` with one
  `exports` entry per variant. The folder is already shaped for it — nothing
  reaches outside itself.
- **After that:** a loader script plus an iframe, so a non-Next.js site can
  embed the same variants. Note for then: the loader must bootstrap from the
  **host page**, not from inside the iframe, or every tenant's request carries
  the iframe's origin and origin validation becomes meaningless.
