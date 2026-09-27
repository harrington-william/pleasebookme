# ADR-0002: The embedded widget is copied source, organized by ecosystem and variant

Status: Accepted  
Date: 2026-09-22

## Context

PleaseBookMe sells to service businesses that mostly do not have engineering teams. Onboarding is manual and high-touch: there is no Stripe checkout, a client contacts us to buy a subscription, and **we build and deploy their landing-page site ourselves**. Those sites are Next.js, because we write them.

That makes the usual widget-distribution question — script tag, iframe, npm package — premature. We control both repositories at install time. What we actually need first is a booking UI that can be dropped into a repo we are already editing, and that stays reusable across the many client sites we expect to build.

The platform also serves several kinds of service business — barbershops, hotels, car rental, consultants — and their booking logic genuinely differs. An appointment is not a stay. A single configurable widget that tries to cover both would be a worse version of each.

## Decision

**1. The widget is distributed as copied source.** `widget/pleasebookme/` is the library and the only folder that is ever copied into a client repo. Everything else in `widget/` — `app/`, `components/`, `lib/` — is a dev harness that is never shipped. Inside `pleasebookme/` every import is relative: no `@/` alias, no `next/*`, no file from outside the folder. That single constraint is what makes a paste work in a repo whose `tsconfig`, aliases and layout we do not control.

**2. Logic is organized per ecosystem; presentation is organized per variant.**

```text
ecosystems/<ecosystem>/logic/      headless: types, steps, schema, hooks, state machine
ecosystems/<ecosystem>/variants/<name>/   components only
```

A second look for barbershops is a new folder under `variants/`. A hotel is a new folder under `ecosystems/` with its own `logic/`. A variant owns no state beyond form inputs and does no fetching, so a new style costs components and nothing else, and two variants of one ecosystem can never drift in behaviour.

`registry.ts` maps a `tenant.ecosystems.code` to a component. `GENERAL` and `BARBERSHOP` both map to the barbershop widget, because a consultant meeting is appointment-centric too.

**3. Styling is scoped and self-contained.** `core/styles/tokens.css` declares colours as `--pbm-*` on `.pbm-widget` and maps them through `@theme inline`, with a fallback to the host's own shadcn variable outside that scope. The widget themes its own subtree and never reads or overwrites the host site's `--primary`.

**4. The key pair lives in the client site's own `.env.local`.** The dashboard shows `publicKey`/`secretKey` once at creation; the agency copies them into `NEXT_PUBLIC_PBM_*` in the site's own repository. PleaseBookMe keeps no second copy and offers no vault or re-download. `PleaseBookMeProvider` takes plain string props and never reads `process.env`, so the library holds no opinion about how a site sources configuration.

## Rejected alternatives

**A loader script plus an iframe, now.** This is where we are heading, and it is the only option that works for a client site we did not build. It is not where we start, because every site today is one we wrote, and the iframe brings a `postMessage` bridge, a height protocol and a second origin to reason about — all cost with no current payoff. One detail is worth recording for when we do build it: **the loader must bootstrap from the host page, not from inside the iframe.** A bootstrap issued inside the iframe carries `widget.pleasebookme.app` as its origin for every tenant, which makes origin validation meaningless.

**A Vite bundle with Shadow DOM instead of Next.js components.** Cleaner isolation and no iframe, but it abandons the fact that every target site is Next.js and that the harness, the hosted page and the widget can otherwise share React components verbatim. It also could not be pasted into a repo as readable source, which is the current delivery mechanism.

**An npm package first.** The right second step, and `pleasebookme/` is already shaped for it — nothing reaches outside the folder, so publishing is a `package.json` with one `exports` entry per variant. It is not the first step because versioning and a registry add process to an install we currently perform by hand.

**One configurable widget for every ecosystem.** Rejected: the configuration surface needed to make one component serve both an appointment and a multi-night stay is larger and less maintainable than two components, and it makes every ecosystem's code a risk to every other's.

## Consequences

- A client site install is: create widget → copy keys into its `.env.local` → copy two folders → install peers → import tokens → render the provider → add the site's origin to `app.cors.allowed-origins`. The last step is an ops action and is part of onboarding.
- Both keys are inlined into the client's browser bundle. Accepted; the controls that carry weight are tenant pinning, the CORS allow-list, rate limiting and bootstrap-time origin validation. Recorded in `SECURITY.md`.
- Bug fixes do not reach already-installed sites automatically. That is the cost of copied source and the main reason the npm stage exists.
- CORS stays an explicit allow-list rather than `*` or a reflection of `widget_origins`, because manual onboarding makes a manual entry cheap and it is tighter.
- The hosted full-page page still carries its own copy of this flow (`client/features/public-booking/`). Making it consume the library is queued; `core/lib/date-math.ts` was copied verbatim, comments included, so that migration is a delete rather than a merge.
