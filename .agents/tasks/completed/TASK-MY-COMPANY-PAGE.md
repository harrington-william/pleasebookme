# Task Contract

## 1. IDENTITY

Title: My Company Page (Frontend Architecture Smoke Test)
Domain: Frontend / Marketing-style Static Page
Priority: Low
Risk: Low
Status: Completed

---

## 2. INTENT

Build a single, self-contained, visually polished frontend page at route
`/my-company` inside the Next.js client application, styled with the
"Obsidian Infrastructure" dark design system already defined in this
repository.

The page has **no product purpose**. It exists solely to exercise the
newly built multi-agent orchestration architecture (context discovery →
task contract → delegated implementation → review) end-to-end on a
low-risk, fully disposable unit of work. The entire route and any
page-specific components created for it are expected to be deleted once
the architecture test is complete.

---

## 3. DELIVERABLES

The implementation agent must produce:

1. A new route at `client/app/my-company/page.tsx` (or an equivalent
   route segment structure consistent with Next.js App Router
   conventions already used in `client/app/`).
2. Any page-specific presentational components, colocated with the route
   (e.g. under `client/app/my-company/` or a scoped `_components/`
   folder) rather than added to shared `client/components/` — this page
   is throwaway and must not pollute shared component directories.
3. Fully static content (no data fetching, no server actions, no forms
   that submit anywhere).
4. Summary of created files.
5. Confirmation that `npm run lint` / `npm run build` (or the client's
   equivalent scripts) pass for the new route.
6. Known limitations, if any.

No review artifact is mandatory (see Section 15), but a lightweight
self-review of visual/theme compliance is expected.

---

## 4. SCOPE

### In Scope

- One new static page at `/my-company`.
- Page-local layout, hero/intro section, and 2–4 supporting sections
  (e.g. "about", "values", "stats", "team" — implementer's creative
  discretion) that showcase the Obsidian dark theme.
- Reuse of existing design tokens (`client/app/globals.css` theme
  variables), existing shared UI primitives
  (`client/components/ui/*`), and existing layout/background utilities
  (`client/components/background/grid-background.tsx`) where they fit.
- Responsive layout (mobile / tablet / desktop) per the breakpoints
  documented in the Obsidian design spec.
- Client-side only interactivity if desired (hover states, simple
  animations) — no network calls.

### Out of Scope

- Any backend/API work of any kind (no controller, service, entity,
  migration, or endpoint).
- Any real company data — all copy is placeholder/fictional.
- Authentication or session gating (the page must be reachable without
  being signed in — do not nest it under `client/app/dashboard/`).
- Registering the page in `client/components/dashboard/dashboard-navigation.ts`
  or `client/components/marketing/marketing-navigation.ts` — it is
  intentionally undiscoverable via existing nav.
- New shared/reusable UI primitives in `client/components/ui/` — if a
  new primitive feels necessary, inline it locally under the page's own
  folder instead.
- Persisting any design decision to `obsidian/PleaseBookMe` documentation
  or to `design/client/stitch/` — this is a disposable test artifact,
  not a durable design contribution.
- Modifying `client/app/layout.tsx`, `client/app/globals.css`, or any
  other shared/global file.

---

## 5. BOUNDARIES

The implementation must operate entirely within:

- `client/app/my-company/` (new route segment).
- Read-only reference to existing shared UI components and design
  tokens — do not modify them.

Do not introduce a second design system or new color tokens outside the
existing Obsidian theme variables already defined in `globals.css`.

Do not touch any file under `client/app/dashboard/`,
`client/app/(public)/`, `client/app/api/`, `client/features/`, or any
`server/` path.

Do not add this route to any navigation, sitemap, or middleware
allow/deny list.

---

## 6. CONSTRAINTS

### Architectural

- Use the Next.js App Router conventions already established in
  `client/app/` (server component by default; add `"use client"` only
  if the page needs interactivity/state).
- Reuse `cn()` from `client/lib/utils.ts` for conditional class names,
  consistent with existing components.
- Reuse existing Tailwind design tokens (`text-headline-lg`,
  `bg-surface`, `text-muted-foreground`, `p-lg`, etc.) rather than
  hardcoding raw hex values or arbitrary Tailwind values — the Obsidian
  palette is already wired into `@theme` in `globals.css`.
- Follow the same file/module conventions visible in
  `client/components/marketing/*` for section-style components (one
  component per section, composed in the page file).

### Security

- No secrets, tokens, credentials, or environment variables are needed
  or permitted in this page.
- No `fetch`/`axios` calls to internal or external APIs.
- No `dangerouslySetInnerHTML` or unsanitized dynamic content — all
  copy is static/hardcoded JSX.

### Compatibility

- Must not break the production build (`next build`) or existing
  routes.
- Must not introduce new npm dependencies — build entirely from
  packages already present in `client/package.json`
  (`lucide-react`, `class-variance-authority`, `tailwind-merge`,
  `classnames`, `@base-ui/react`, etc.).

### Disposability

- Keep the change self-contained enough that deleting
  `client/app/my-company/` alone fully removes the feature with no
  dangling references elsewhere in the codebase.

---

## 7. DEPENDENCIES

### Required Components

- Existing Tailwind/Obsidian theme tokens defined in
  `client/app/globals.css`.
- `client/lib/utils.ts` (`cn` helper).
- Existing shared primitives in `client/components/ui/`
  (`button.tsx`, `input.tsx`, `label.tsx`, `checkbox.tsx`) — reuse
  rather than reimplement where a section calls for a button/input.
- Optional: `client/components/background/grid-background.tsx` for an
  ambient background treatment, and `lucide-react` for iconography
  (already a project dependency, used throughout
  `dashboard-navigation.ts` and `marketing-navigation.ts`).

### Related Policies

- None (no authorization/authentication surface is touched).

### Required Infrastructure

- None. The Next.js dev server (`npm run dev` inside `client/`) is
  sufficient to preview the page; no Spring Boot backend, Postgres, or
  Redis dependency exists for this task.

---

## 8. INPUT CONTEXT

The agent must inspect the following before implementation:

### Design Reference

- `design/client/stitch/obsidian_infrastructure/DESIGN.md` — the
  authoritative Obsidian Infrastructure design spec: color tokens,
  typography (Geist/Geist Mono), spacing/radius scale, elevation
  levels, component treatment (buttons, cards, chips/badges, status
  indicators).
- `client/app/globals.css` — how the DESIGN.md tokens are actually
  wired into Tailwind's `@theme` (semantic color variables, type scale
  utilities, spacing utilities). Treat this file as the literal source
  of truth for class names to use (e.g. `text-headline-lg`, `p-lg`,
  `bg-surface-container`), not the raw hex values in DESIGN.md.

### Existing Frontend Patterns

- `client/app/(public)/page.tsx` and `client/components/marketing/*`
  — the closest existing precedent for a composed, section-based
  static marketing-style page.
- `client/app/dashboard/page.tsx` — an example of a simpler, single-
  panel page using the same design tokens, for a sense of restraint/
  information density expected by the design system ("calm, expensive,
  rock-solid").
- `client/components/ui/button.tsx` — example of the `cva` + `cn`
  pattern used for variant-driven shared components, in case a
  page-local component needs similar treatment.

### Decisions / Governing Docs

- `AGENTS.md` (repo root) — general engineering rules (don't introduce
  unnecessary abstractions, don't touch unrelated code).
- This task is explicitly exempt from `SERVER_AGENTS.md` and
  `SECURITY.md` concerns — no server-side or authentication-adjacent
  code is in scope.

The listed context is the minimum required context. The agent may
retrieve additional context (e.g. other `client/components/marketing/*`
files) when required.

---

## 9. FUNCTIONAL REQUIREMENTS

### 1. Route Availability

Navigating to `/my-company` in the running Next.js app must render the
page without requiring authentication and without any network request
to the backend.

### 2. Visual Theme Compliance

The rendered page must visually read as "Obsidian Infrastructure" —
deep dark background, bordered panels/cards (no drop shadows), Geist
typography, the documented blue primary accent used sparingly, and
desaturated status colors if any status/badge elements are used.

### 3. Content Sections

The page must contain, at minimum:

- A hero/header section introducing the fictional company (name,
  tagline).
- At least two additional content sections (e.g. mission/values,
  stats, "what we do", team/leadership) built from placeholder content.

### 4. Responsiveness

The page must render correctly (no horizontal overflow, no broken
layout) at mobile (<640px), tablet (640–1024px), and desktop (>1024px)
widths, per the breakpoints in `DESIGN.md`.

### 5. Isolation

The page must not appear in any existing navigation menu
(`dashboard-navigation.ts`, `marketing-navigation.ts`) and must not be
linked from any existing page.

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### Architecture

- Must not introduce new shared components, new design tokens, or new
  npm dependencies.
- Must not couple the page to any backend contract, DTO, or API route.

### Security

- Must not introduce any XSS vector, external script tag, or
  unsanitized dynamic HTML.
- Must not read or display any real user/session/company data.

### Maintainability

- The page should be trivially deletable: a single route folder with
  no external references, so removing it after the architecture test
  is a one-directory deletion.

---

## 11. ACCEPTANCE CRITERIA

### 1. Route Renders

Given the Next.js dev server is running,
when a browser navigates to `/my-company`,
the page renders successfully with HTTP 200 and no console errors.

### 2. No Auth Gate

Given no active session/cookie,
when `/my-company` is requested,
the page still renders (it must not redirect to `/login` the way
`client/app/dashboard/*` routes do).

### 3. Theme Fidelity

Given the rendered page,
when compared against `design/client/stitch/obsidian_infrastructure/DESIGN.md`,
the background, panel borders, typography, and accent color usage are
visually consistent with the documented Obsidian Infrastructure system.

### 4. No Backend Calls

Given the browser network tab while viewing `/my-company`,
no requests are made to `/api/*` or any Spring Boot backend origin.

### 5. Responsive Layout

Given the page is viewed at 375px, 768px, and 1440px viewport widths,
no horizontal scrollbar appears and no content is clipped or
overlapping.

### 6. No Navigation Leakage

Given the existing dashboard and marketing navigation menus,
`/my-company` does not appear as a link in either.

### 7. Clean Build

Given the project's standard lint/build commands,
`npm run lint` and `npm run build` (run from `client/`) both succeed
with the new route present.

---

## 12. VALIDATION

### Manual / Visual

- Load `/my-company` in a browser at mobile, tablet, and desktop widths
  and visually confirm theme compliance against `DESIGN.md`.
- Confirm no session/auth redirect occurs when not logged in.
- Confirm the browser Network tab shows no calls to `/api/*` or the
  Spring Boot backend.

### Static / Build Validation

Run, from `client/`:

- `npm run lint`
- `npm run build`

Record the commands executed and their results.

### Explicitly Not Required

- No automated unit/integration tests are required for this page — it
  is a disposable architecture smoke test, not production functionality.

---

## 13. ESCALATION

The agent must stop and escalate to the orchestrator when:

- Building the page would require adding a new npm dependency.
- Achieving the desired visual fidelity would require modifying shared
  files (`globals.css`, `layout.tsx`, files under `components/ui/`).
- Any ambiguity arises about whether a piece of content should be
  treated as "real" (e.g. should this page ever be indexed, linked, or
  kept) — the default assumption is **no**, this is temporary and will
  be deleted.
- The task appears to require any backend, authentication, or database
  change to satisfy a requirement above.

Do not silently expand scope beyond a single static page.

---

## 14. IMPLEMENTATION PLAN

1. Confirm the Next.js client app runs standalone for a static route
   (`cd client && npm run dev`), independent of the Spring Boot server.
2. Re-read `design/client/stitch/obsidian_infrastructure/DESIGN.md` and
   cross-reference each documented token (surface levels, primary
   accent, typography scale, radius scale, elevation rules) against its
   corresponding Tailwind utility in `client/app/globals.css`, so all
   styling in the new page uses existing utility classes
   (`bg-surface`, `bg-surface-container`, `text-headline-lg`,
   `text-body-md`, `border-border`, `rounded-lg`, `p-lg`, `gap-md`,
   etc.) rather than raw values.
3. Create the route folder `client/app/my-company/` with a
   `page.tsx` default export. Keep it a server component unless a
   specific section needs client-side interactivity (e.g. a hover
   card or an animated counter), in which case isolate that piece into
   its own `"use client"` child component.
4. Design fictional company content (name, tagline, mission, a handful
   of "values" or "stats" cards, optionally a closing CTA-style
   section) — all placeholder, clearly fictional, no resemblance to a
   real company/brand.
5. Build the page as a composition of small section components,
   colocated under `client/app/my-company/` (e.g.
   `_components/hero.tsx`, `_components/values-section.tsx`,
   `_components/stats-section.tsx`), mirroring the pattern in
   `client/components/marketing/*` but kept local rather than shared,
   per the disposability constraint.
6. Apply Obsidian visual language throughout:
   - Deep background (`bg-background`/`bg-surface` per token wiring).
   - Bordered, shadow-free panels (`border border-border rounded-lg`)
     for cards.
   - Geist typography via existing `text-display` / `text-headline-lg`
     / `text-headline-md` / `text-body-lg` / `text-body-md` /
     `text-mono-label` utilities.
   - Sparing use of the primary accent color for one or two focal
     elements (e.g. a CTA button, a highlighted stat) — reuse
     `client/components/ui/button.tsx`'s `default` variant rather than
     hand-rolling a new button style.
   - Optionally layer `client/components/background/grid-background.tsx`
     behind the hero section for the "infrastructure" ambient texture,
     matching how it may already be used elsewhere (verify usage
     before reusing).
7. Verify responsiveness at the three documented breakpoints using
   browser dev tools or the `claude-in-chrome` skill, adjusting
   Tailwind responsive prefixes (`md:`, `lg:`) as needed.
8. Confirm no data fetching, no API calls, and no auth check exists
   anywhere in the new files.
9. Run `npm run lint` and `npm run build` from `client/` and fix any
   errors.
10. Manually load `/my-company` in a browser (via `npm run dev` or the
    `run` skill) and visually compare against `DESIGN.md` and the
    dashboard/marketing pages for consistency.
11. Report the final file list, validation command results, and any
    known visual limitations to the orchestrator.

Do not create new shared abstractions unless the page's own composition
genuinely cannot express the required layout — this page's very
purpose is to test the orchestration pipeline on a small, contained
unit of work, so keep it simple.

---

## 15. REVIEWS

This task does not require Architecture Review or Security Review —
it touches no authentication, authorization, data, or backend surface,
and introduces no new architectural pattern.

### Recommended Lightweight Review

Optional, non-blocking:

- **Design/UI Review** — visually check the rendered page against
  `design/client/stitch/obsidian_infrastructure/DESIGN.md` for token
  fidelity (colors, type scale, radius, elevation/borders) and against
  `client/app/(public)/page.tsx` for consistency of composition style.

### Post-Test Cleanup (informational, not part of this task's scope)

Once the multi-agent architecture test is deemed complete, the
orchestrator or user should delete `client/app/my-company/` and this
task file (or move it to `.agents/tasks/completed/` with a note that
the artifact itself was subsequently removed).
