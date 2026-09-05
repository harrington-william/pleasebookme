---
name: api-documentation
description: Generate maintainable and human-readable API documentation
---

# API Documentation Skill

## Purpose

Generate and maintain the human-readable REST API reference in the project's
Obsidian knowledge base.

This is **reference documentation** — the most implementation-oriented level
described in [[document-generating]]. It tells a client developer exactly how to
call an endpoint and what will happen when they do.

It is not architecture documentation and not service documentation. It describes
the **wire contract**, not the internal design that produces it.

---

## Activation

Activate when:

- a new controller or endpoint is added;
- an endpoint's path, verb, parameters, request body, or response shape changes;
- an endpoint is removed, replaced, or has its scoping/semantics changed;
- a domain's typed exceptions change, altering which statuses an endpoint returns;
- a new bounded context gains its first controller.

Do not activate for:

- internal refactors with no wire-visible effect;
- service-layer behavior that does not change the contract — use
  [[service-documentation]];
- entity or migration changes — those belong to the database schema docs;
- client-side code.

---

## Composition

This skill depends on:

- **[[document-generating]]** — all writing rules (paragraph length, tables over
  prose, invariants, current vs. planned behavior) come from there. Do not
  restate them here; follow them.

Do not duplicate knowledge that lives in `AGENTS.md`, `SERVER_AGENTS.md`,
`SECURITY.md`, `CODING_CONVENTIONS.md`, or the schema docs. Reference it.

---

# 1. Where documentation goes

```text
obsidian/PleaseBookMe/API/<Schema>/<Resource>/
├── <Resource> API Summary.md      one per resource — the index
├── Create a <thing>.md            one note per endpoint
├── Get a <thing>.md
├── List <things>.md
├── Update a <thing>.md
└── Delete a <thing>.md
```

`<Schema>` mirrors the server's bounded context, not the URL: `API/Core/`,
`API/Customer/`, `API/Resource/`, `API/Auth/`. `<Resource>` is the plural domain
noun as it appears in the folder tree — `Bookings`, `Attendees`, `Notes`.

**One folder per resource. One note per endpoint. No exceptions** — do not
document five endpoints in a single file, and do not split one endpoint across
two.

---

# 2. Templates

| Template | Produces |
|---|---|
| `templates/API_TEMPLATE.md` | one endpoint note |
| `templates/API_SUMMARY_TEMPLATE.md` | the resource's `API Summary` note |

These are **worked examples** (a real `Create a user` endpoint), not blank
skeletons. Read them for the target shape and level of detail, then write the
new document against the actual source code — never by find-and-replacing the
example's field names.

## The one template deviation to respect

The templates head the response section `## Success Response`. **Every existing
note in the corpus uses `## Successful Response`.** Match the sibling notes in
the folder you are writing into, not the template.

Consistency within the corpus beats fidelity to the template. If the template
and the corpus disagree on anything else, report the discrepancy rather than
silently picking a side.

---

# 3. Source of truth

Per `AGENTS.md`, current source code outranks documentation. Derive every fact
from the code, in this order:

| Fact | Read |
|---|---|
| Path, verb, status code, parameters | the `@RestController` |
| Request fields and validation | the `Request` record's `jakarta.validation` annotations |
| Response fields | the `Response` record and its `from(...)` factory |
| Defaults, fallbacks, side effects | the `ServiceImpl` method |
| Which statuses are possible | `GlobalExceptionHandler`'s mappings for that domain's typed exceptions |
| Auth requirement | `SecurityConfig` — `permitAll` paths are the exception, not the rule |

Never document an endpoint from a task contract, a plan, or an older version of
the doc. Those describe intent; the controller describes reality.

If code and an existing doc disagree, the code wins **and** you record the
discrepancy — do not quietly overwrite.

---

# 4. Naming, and why renaming matters

An endpoint note is named for **what the endpoint does now**, phrased as a human
action:

```text
Create a booking      Get a booking       List bookings
Update a booking      Delete a booking    Cancel a booking
```

The summary is `<Resource> API Summary.md` — never a bare `API Summary.md`.

## When semantics change, rename the note

A note whose title describes behavior the endpoint no longer has is worse than
no note, because it is confidently wrong.

Worked example: `GET /api/v1/bookings` stopped returning every booking and became
an organization-scoped paginated list. `Get all bookings.md` was renamed to
`List bookings.md`, because "get all" is no longer a capability the API offers.

Renaming is a three-step operation:

1. Rename the file.
2. Update the `[[wikilink]]` in the resource's API Summary endpoints table.
3. Grep the whole vault for the old name and fix every other reference.

A dangling wikilink is a defect. See the Validation gate in §8.

---

# 5. What the Description must carry

This is where most generated API docs fail. The endpoint signature is already in
the note — the Description exists to say what a caller cannot infer from it.

Always document, when true:

- **Defaults and fallbacks.** Which fields fall back to a platform default when
  omitted, and to what value.
- **Idempotency.** Whether repeating the call is safe, and what the second call
  returns.
- **What is deliberately not written.** A field the endpoint leaves null on
  purpose, with the reason. This prevents the next engineer "fixing" it.
- **Scoping, and why it takes that shape.** If scope resolves through an
  association rather than a column on the row, say so — e.g. bookings scope
  through `booking → service → organization` because `core.bookings` has no
  organization column.
- **Ignored vs. rejected input.** These are very different for a caller. An
  unknown `sort` property is *ignored*; an unknown `tab` *falls back to the
  default*; a missing required scope param is *rejected with 400*.
- **Matching semantics.** For search parameters: which columns are matched, case
  sensitivity, and whether wildcards are escaped.
- **Limits.** Page size defaults and caps, unpaginated endpoints that grow with
  tenant size.
- **Domain separations a caller might assume away.** State the boundary and its
  consequence — e.g. attendees have no FK to customers, therefore this endpoint
  cannot give you a customer's booking history.
- **Replaced endpoints.** When an endpoint supersedes an older shape, say what
  changed and that the old capability no longer exists anywhere.

Do not document internal class names, package structure, or which repository
method backs the call. That is service documentation.

---

# 6. Sections beyond the template

The template covers a body-carrying endpoint. Two additions are required in
practice.

## Query Parameters

Any endpoint with query parameters gets a table, placed after `## Headers` and
before `## Successful Response`:

```markdown
## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `organizationId` | Yes | The organization whose bookings to list. |
| `q` | No | Case-insensitive contains-match on `title`. |
```

Use `Conditional` for "one of these is required", and say which wins when
several are supplied.

## Enumerated behavior

When a parameter selects between named behaviors (a tab, a mode, a status
filter), give it its own table mapping each value to its exact predicate. Prose
cannot carry this without becoming unreadable, and a caller needs it precisely.

## Event Produced

**This section documents a planned contract, not implemented behavior.** The
platform has no event bus — there is no `ApplicationEventPublisher`, no
`publishEvent` call, and no `@EventListener` anywhere in the server. Roughly 158
existing notes nonetheless carry an `## Event Produced` section, so it is an
established corpus convention and new notes follow it.

Name events `<Resource><Action>` in past tense, matching the corpus:
`OrganizationCreated`, `BookingCancelled`, `MembershipDeleted`.

Two rules:

- Name only the event this endpoint would emit. Do not invent an event for an
  endpoint whose siblings have none, and do not enumerate downstream effects.
- Never let the Description imply a caller can *subscribe* to it today. The
  section names a future contract; the prose must not promise delivery.

If an event bus is ever implemented, this section becomes verifiable and the
rule tightens to "only what the code emits".

---

# 7. Procedure

1. **Identify the surface.** List every endpoint on the controller, including
   ones you are not changing — the summary's table must end up complete.
2. **Read the code** in the §3 order. Do not start writing until the request DTO,
   response DTO, and service method have all been read.
3. **Determine what changed** for an update: a new endpoint, a changed shape, or
   a renamed capability. Update the smallest set of notes; do not regenerate a
   folder because one endpoint moved.
4. **Write or update each endpoint note** against the template, with §5's
   Description content and §6's extra sections.
5. **Update the API Summary** — the endpoints table, and the Overview paragraph
   if the resource's story changed (e.g. listing became scoped).
6. **Rename any note whose title no longer matches its behavior** (§4), and fix
   every wikilink.
7. **Run the Validation gate** (§8).
8. **Report** what you wrote, any code/doc discrepancies found, and any endpoint
   or domain still undocumented.

---

# 8. Validation gate

Before finishing:

```bash
# no dangling wikilinks left by a rename
grep -rn "Old note name" obsidian/PleaseBookMe/

# every [[link]] in the summary resolves to a real file
ls "obsidian/PleaseBookMe/API/<Schema>/<Resource>/"
```

Then confirm:

- [ ] Every endpoint on the controller has a note.
- [ ] Every note is linked from the API Summary's endpoints table.
- [ ] Every wikilink resolves; no note references a renamed file.
- [ ] Section headings match the sibling notes in the folder (§2).
- [ ] Paths, verbs, and status codes were read from the controller, not assumed.
- [ ] Listed errors correspond to real `GlobalExceptionHandler` mappings.
- [ ] `## Event Produced` names one plausible `<Resource><Action>` event and the prose does not imply it is subscribable today.
- [ ] Response JSON field names match the `Response` record exactly.
- [ ] Required/optional in the parameter table matches the actual annotations.
- [ ] No invented fields, no fields omitted.

---

# 9. Anti-patterns

Never produce API documentation that:

- restates the endpoint signature as prose and calls that a Description;
- keeps a note title describing behavior the endpoint no longer has;
- lists the template's generic error set instead of the endpoint's real one;
- invents a header or a response field that does not exist;
- promises an event is subscribable today (see §6 — the event bus does not exist);
- documents internal class or package names;
- describes a planned endpoint as though it ships today;
- copies an example's field names into a different resource's note;
- leaves a wikilink pointing at a renamed or deleted note;
- documents `GET /resource` as "retrieves every X" when it is scoped;
- omits the reason a field is deliberately left unwritten.

---

# 10. Definition of Done

API documentation is complete when a client developer who has never seen the
codebase can:

- find the endpoint from the resource's API Summary;
- construct a valid request without reading Java;
- predict the response shape field for field;
- know which inputs are rejected, which are ignored, and which fall back;
- know what the call does *not* do;
- know every status code they must handle.
