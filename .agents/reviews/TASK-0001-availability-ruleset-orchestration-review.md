# TASK-0001 Availability Ruleset Orchestration Review

## Reviewer Role

Security review

## Scope Reviewed

- `server/src/main/java/com/pleasebookme/server/service/availabilityruleset/`
- `server/src/main/java/com/pleasebookme/server/core/schedule/dto/ScheduleRequest.java`
- `server/src/main/java/com/pleasebookme/server/core/schedule/service/impl/ScheduleServiceImpl.java`
- `server/src/main/java/com/pleasebookme/server/core/availability/dto/AvailabilityRequest.java`
- `server/src/main/java/com/pleasebookme/server/core/availability/service/impl/AvailabilityServiceImpl.java`
- New unit tests for availability rulesets, schedules, and availabilities
- `obsidian/PleaseBookMe/Services/Availability Ruleset/Availability Ruleset Service.md`

## Findings

No security findings.

## Evidence

- `ScheduleRequest` and `AvailabilityRequest` no longer contain `userId`.
- `AvailabilityRulesetRequest` and `AvailabilityWindowRequest` contain no ownership field.
- `ScheduleServiceImpl`, `AvailabilityServiceImpl`, and `AvailabilityRulesetServiceImpl` call `CurrentPrincipalProvider.requireUser().userId()` before resolving the owning user through `UserRepository.findById(...)`.
- Existing `PUT` paths require a valid current user but do not rewrite the existing row owner, avoiding an ownership-transfer side effect.
- `AvailabilityRulesetServiceImpl.createAvailabilityRuleset(...)` is a public `@Transactional` method on its own `@Service` bean and is invoked from `AvailabilityRulesetController`.
- Comment density was checked; comments are limited to the transaction boundary, ownership-transfer guard, and `List<Integer>` DTO decision.

## Validation

- `./gradlew compileJava compileTestJava` passed.
- `./gradlew test --tests "*ScheduleServiceImplTest*" --tests "*AvailabilityServiceImplTest*" --tests "*AvailabilityRulesetServiceImplTest*"` passed.
- `./gradlew test` passed.

## Recommendation

Ready to consume from the client-side follow-up. A separate authorization-hardening task should eventually verify that callers may only update schedules and availabilities they own, but that is outside TASK-0001's ownership-spoofing fix.
