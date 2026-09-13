# Availability Ruleset Service

## Purpose

The Availability Ruleset service creates one schedule and its recurring availability windows in a single server-side operation.

It exists to close [ISSUE-0003](../../../../.agents/issues/open/ISSUE-0003-schedule-availability-flow.md), where the client previously created a schedule first and then created each availability window through separate HTTP requests. A failure halfway through that sequence could leave a valid-looking but incomplete schedule.

## Endpoint

```text
POST /api/v1/availability-rulesets
```

Authentication is required with a bearer token.

## Request

```json
{
  "title": "Working Hours",
  "timezone": "Asia/Ho_Chi_Minh",
  "windows": [
    {
      "days": [1, 2, 3],
      "startTime": "09:00:00",
      "endTime": "12:00:00"
    },
    {
      "days": [4, 5],
      "startTime": "13:00:00",
      "endTime": "17:00:00"
    }
  ]
}
```

`timezone` is optional and falls back to the schedule entity default when omitted. `windows` must contain at least one window, and nested window validation is cascaded from the top-level request.

The request never accepts `userId`. Ownership is derived from the authenticated `UserPrincipal`.

## Response

```json
{
  "schedule": {
    "scheduleId": 7,
    "userId": 42,
    "title": "Working Hours",
    "timezone": "Asia/Ho_Chi_Minh",
    "createdAt": "",
    "updatedAt": ""
  },
  "availabilities": [
    {
      "availabilityId": 1,
      "userId": 42,
      "scheduleId": 7,
      "days": [1, 2, 3],
      "startTime": "09:00:00",
      "endTime": "12:00:00",
      "createdAt": "",
      "updatedAt": ""
    }
  ]
}
```

The response is built from the existing `ScheduleResponse.from(...)` and `AvailabilityResponse.from(...)` factories so it stays aligned with the single-row Schedule and Availability APIs.

## Transaction Boundary

`AvailabilityRulesetServiceImpl.createAvailabilityRuleset` is the transaction boundary. The method lives on its own `@Service` bean and is annotated with `@Transactional`, following the same rule documented for `finalizeOnboarding`.

Both facts matter: the flow writes one `core.schedules` row and N `core.availabilities` rows, so a failure while writing any window must roll the entire ruleset back. Keeping the boundary on a public method of its own Spring-managed bean also avoids the self-invocation trap where a private transactional helper would not be proxied.

## Ownership

The service calls `CurrentPrincipalProvider.requireUser().userId()` and resolves that id through `UserRepository.findById(...)` before building any entity.

That same ownership rule is now used by the existing `POST` Schedule and Availability CRUD service methods. Existing `PUT` methods still require an authenticated user, but they do not rewrite the row owner. Clients can still see `userId` in responses, but they can no longer choose it in request bodies.
