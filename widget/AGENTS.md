<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

# The widget workspace

This project is **two things in one folder**, and confusing them is the
easiest way to break it.

| | `pleasebookme/` | everything else (`app/`, `components/`, `lib/`) |
|---|---|---|
| What it is | the library we sell | a dev harness to look at it |
| Ships to a client | **yes, copied as source** | never |
| May import from the other | **no** | yes |
| Imports | relative only | `@/` alias is fine |

The business model is copy-paste distribution: we build the client's
landing-page site and paste `pleasebookme/core/` plus one ecosystem folder
into their repo, like selling source. The harness exists so we can see the
result without a client site. See `pleasebookme/README.md` for the seven
installation steps a real site follows.

## The rules

1. **Nothing under `pleasebookme/` may use `@/`, `next/*`, or a file outside
   `pleasebookme/`.** A single alias import compiles fine here and breaks
   every future paste, silently. `npm run lint && npm run build` will not
   catch it; this is the grep that does:
   ```bash
   grep -rn "from \"@/\|from \"next/" pleasebookme   # must print nothing
   ```
2. **No hard-coded colour, size or spacing value in a component.** Everything
   comes from `pleasebookme/core/styles/tokens.css`. Colours are declared as
   `--pbm-*` on `.pbm-widget` and mapped with `@theme inline`, so the widget
   themes its own subtree and never touches the host site's `--primary`.
   ```bash
   grep -rnE "#[0-9a-fA-F]{6}" pleasebookme --include="*.tsx"   # must print nothing
   ```
3. **`app/globals.css` may not define a token `tokens.css` already defines.**
   If a class in a variant only renders because the harness declared
   something, the library is broken for every real client. The harness
   deliberately imports `tailwindcss` plus `tokens.css` and adds only fonts.
4. **The key pair lives in the consuming site's own `.env.local`**, and
   `PleaseBookMeProvider` never reads `process.env` — it takes plain string
   props. `app/barbershop/kinetic/page.tsx` reads the env vars and passes
   them down; a real client site does exactly the same six lines.
5. **No token in `localStorage`, `sessionStorage` or a cookie.** The 15-minute
   access token is a closure variable in `core/auth/session.ts`. Widgets get
   no refresh token; a 401 triggers one silent re-bootstrap and one replay.

## The t-shirt size collision

`tokens.css` defines a spacing scale whose keys are t-shirt sizes —
`--spacing-xs` through `--spacing-2xl`. Tailwind's width utilities read the
**container** namespace for the same names, and when a key is missing there it
falls back to the spacing scale. So on a page that imports these tokens:

```text
max-w-2xl   ->  48px   (--spacing-2xl)      NOT 42rem
max-w-3xl   ->  48rem  (--container-3xl)    fine, 3xl is not a spacing key
```

A `max-w-2xl` container silently collapses to 48 pixels wide and the text
wraps one word per line. It is not a build error and no linter catches it.

**Use an explicit value for widths — `max-w-[42rem]`, `max-w-[600px]` — or a
size that is not in the spacing scale.** The rule applies to `w-*`, `min-w-*`
and the `*-h-*` family too. The library itself uses none of these; the one
place that did was the harness's configuration notice, fixed on 2026-09-22.

The client app carries the same spacing scale and the same hazard.

## Layout

```text
pleasebookme/
  core/
    api/       client.ts (axios + bearer + 401 replay), widget-api.ts (the four calls), errors.ts
    auth/      session.ts (bootstrap, single-flight), PleaseBookMeProvider.tsx, use-pleasebookme.ts
    lib/       date-math.ts (+ test), format.ts, calendar-link.ts, cn.ts
    ui/        button, input, label, native-select, spinner, error-message
    styles/    tokens.css
  ecosystems/
    barbershop/
      logic/     types, steps, details-schema, use-available-slots, use-booking-flow
      variants/
        kinetic/ BarbershopBookingWidget.tsx + components/ + theme.css
    registry.ts  ecosystem code -> component
app/
  page.tsx                    catalog of ecosystem × variant
  barbershop/kinetic/page.tsx the harness route, port 3002
```

**Logic is per ecosystem; presentation is per variant.** `logic/` is headless
— no JSX, no styling. A variant owns no state beyond form inputs. A second
look for barbershops is a new folder under `variants/`; a hotel is a new
folder under `ecosystems/` with its own `logic/`, because a stay is not an
appointment.

## Provenance

`core/lib/date-math.ts` is a **verbatim** copy of
`client/features/public-booking/lib/date-math.ts`, comments included, and its
test came with it. Keep it that way: the hosted page will eventually consume
this library instead of its own copy, and that migration should be a delete,
not a merge. The same is true of `format.ts` and `calendar-link.ts`.

`logic/use-booking-flow.ts` is the state machine lifted out of that feature's
`booking-widget.tsx`; the variant components are its components with the
imports rewritten. Behaviour was carried, not redesigned.

## Running it

```bash
npm run dev     # port 3002 — the client owns 3000
npm run lint    # must be clean, warnings included
npm test        # vitest: date math, session single-flight, client 401 replay
npm run build
```

`.env.local` (gitignored) needs `NEXT_PUBLIC_PBM_API_URL`,
`NEXT_PUBLIC_PBM_PUBLIC_KEY` and `NEXT_PUBLIC_PBM_SECRET_KEY` from a widget
created in the dashboard. Without them the harness renders a configuration
notice instead of the widget. Port 3002 is already in the server's
`app.cors.allowed-origins`; a real client origin has to be added there by
hand at onboarding.

## Where this is going

npm package (`@pleasebookme/widgets`, one `exports` entry per variant), then
a loader script plus iframe for non-Next.js sites. Both build on this folder
without restructuring it. For the iframe stage: the loader must bootstrap
from the **host page**, not inside the iframe, or every tenant's request
carries the iframe's origin and origin validation stops meaning anything.
