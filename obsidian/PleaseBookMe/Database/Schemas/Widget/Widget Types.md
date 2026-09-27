# Widget Types

## Definition

**Type** describes *how* an embeddable booking widget presents itself on the page it is placed on. It is drawn from the closed set `widget.widget_type` and lives on [[Table Widgets]].

It only describes the presentation shape. It says nothing about which tenant owns the widget, which services it exposes, or how it authenticates — those are the rest of the `widgets` row.

## The Types

| Type | Meaning |
|---|---|
| `INLINE` | Renders directly in the page's normal document flow, inside a container element the host page provides (e.g. `<div id="pleasebookme-widget">`). No overlay, no navigation — the booking UI sits where the embed script mounted it, like any other piece of page content. This is the only type the dashboard widget editor currently lets a tenant create; the other two are shown disabled with a "Soon" chip (`SUPPORTED_WIDGET_TYPES` in the client). |
| `POPUP` | Stays hidden until triggered (typically a button the tenant places on their own page), then opens as a floating overlay above the host page's content. The host page itself never navigates or scrolls away. |
| `EMBEDDED` | A persistent, script-injected container mounted into the host page, distinct from `INLINE` in *how* it is mounted (widget SDK/iframe versus native DOM injection) rather than *where* on the page it appears. This is the column's `DEFAULT` (`V47__widgets.sql`), since a widget created before a type is explicitly chosen should still function as a fixed embed. |

All three describe an embed on **a website the tenant owns and controls** — a barbershop's own homepage, a hotel's own booking page. They authenticate via a public/secret key pair and, optionally, origin validation against `widget.widget_origins` (see [[Table Widget Origins]] and `SECURITY.md` → "Widget Authentication").

## What This Enum Does Not Cover

### `FULL_PAGE` — removed

> **Naming note (2026-09-22).** The hosted page is now *called* the **full-page widget**, and its server code lives in `service/widget/fullpage`. That is vocabulary, not a reversal: it is still not a `widget.widget_type` value and still has no `widgets` row, key pair or origin. `widget.widget_type` describes how an **embed** presents itself on a page the tenant owns; the full-page widget is a first-party page the platform hosts, so it has nothing for this enum to describe.

The enum originally had a fourth value, `FULL_PAGE`, described as "a hosted booking page." It was removed by `V143__drop_widget_type_full_page.sql` (Postgres has no `DROP VALUE`, so the migration recreates the type without it and renames it back to `widget.widget_type`).

It was removed because it was modeling the wrong thing. A hosted page at `pleasebookme.app/booking/{organizationSlug}` is not an embed on a tenant's own website — it has no origin to validate, no host page to mount into, and (by design) no credentials at all: it is `permitAll`, scoped by the organization slug in the URL rather than by a widget's public/secret key. Keeping it in `widget.widget_type` implied every organization needed a `widgets` row with a key pair just to have a public page, which is not true — the page exists automatically once a tenant is provisioned, with nothing to configure.

The full-page booking page is built under [[../../../../.agents/tasks/active/TASK-0011-full-page-widget|TASK-0011]] as its own concept, `service/widget/fullpage` (originally `service/publicbooking`), entirely outside the `widget` schema. See `SECURITY.md` → "Full-Page Widget Endpoints" for its (deliberately different) authentication posture.

### The two widget kinds, side by side

| | Embedded widget (`INLINE` / `POPUP` / `EMBEDDED`) | Full-page booking page |
|---|---|---|
| Lives on | The tenant's own website | `pleasebookme.app/booking/{slug}` |
| Schema | `widget.widgets` row required | No row — resolved from `organization.slug` |
| Setup | Tenant creates a widget, picks a type, optionally registers an origin | None — exists the moment the organization is provisioned |
| Authenticates as | `WidgetPrincipal`, via public/secret key (+ origin) | Nobody — `permitAll` |
| Protected by | Origin check, key pair, tenant binding | Rate limiting (and, later, a bot challenge — not yet built) |

## Nothing Enforces Capability Differences Yet

The three remaining types are, today, presentational labels only. `WidgetPrincipal` carries no scopes or authority set that varies by type (`SECURITY.md` → "Not Yet Implemented"), and no server code branches on `type` to change what a widget is permitted to do. A capability model, if one is ever needed per type, is future work — not something a caller can rely on today.
