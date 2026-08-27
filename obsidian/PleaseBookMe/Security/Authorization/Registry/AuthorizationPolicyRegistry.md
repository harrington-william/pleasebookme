# What It Is

`AuthorizationPolicyRegistry` is the component that turns "every `AuthorizationPolicy` bean Spring happens to know about" into "the specific, ordered list of policies relevant to this one resource type and action." It's the piece that makes the promise in **[[Authorization Engine]]** literally true — that new authorization logic is added by registering a bean, never by editing existing code — because nothing else in the engine has to change when a new policy shows up; the registry just picks it up automatically and starts including it in the right resolutions.

Package: `com.pleasebookme.server.security.authorization.registry` (interface), `.impl` (the one implementation).

```java
public interface AuthorizationPolicyRegistry {
    List<AuthorizationPolicy> resolve(String resourceType, String action);
}
```

One method. Given what a request concerns, hand back exactly the policies that should get a say — already filtered, already ordered.

# How the Policy List Gets There in the First Place

```java
@Component
@RequiredArgsConstructor
public class DefaultAuthorizationPolicyRegistry implements AuthorizationPolicyRegistry {
    private final List<AuthorizationPolicy> policies;
    // ...
}
```

This constructor parameter is doing more work than it looks like. Spring, asked to inject a `List<AuthorizationPolicy>`, collects **every bean in the application context that implements the interface** — every `@Component`-annotated policy anywhere in the codebase lands in this list with zero registration ceremony beyond existing as a bean. This is the actual mechanism behind "extend by registering a bean": there is no separate list to edit, no factory method to update, no config file to touch. Write a class, annotate it `@Component`, implement `AuthorizationPolicy`, and it's already part of what this registry indexes the next time the application starts.

# Indexing at Startup

```java
@PostConstruct
void index() {
    Map<String, List<AuthorizationPolicy>> grouped = new HashMap<>();

    for (AuthorizationPolicy policy : policies) {
        grouped.computeIfAbsent(policy.resourceType(), key -> new ArrayList<>()).add(policy);
    }

    for (List<AuthorizationPolicy> group : grouped.values()) {
        group.sort(Comparator.comparingInt(AuthorizationPolicy::order));
    }

    wildcardPolicies = List.copyOf(grouped.getOrDefault(AuthorizationPolicy.WILDCARD_RESOURCE_TYPE, List.of()));

    Map<String, List<AuthorizationPolicy>> indexed = new HashMap<>();
    grouped.forEach((resourceType, group) -> indexed.put(resourceType, List.copyOf(group)));
    byResourceType = Map.copyOf(indexed);
}
```

This runs once, right after Spring finishes constructing the bean (`@PostConstruct`), not on every call to `resolve(...)`. Every injected policy is grouped by its own `resourceType()` — including the wildcard constant `"*"`, which ends up as just another key in the same map at this stage. The wildcard bucket is then pulled out into its own field (`wildcardPolicies`), and everything — wildcard bucket included — is also kept in `byResourceType`, keyed by whatever `resourceType()` each policy declared.

Both the grouping and the later per-request combination sort by the same comparator (`Comparator.comparingInt(AuthorizationPolicy::order)`), which means sorting genuinely happens twice — once here, per group, at startup, and once again inside `resolve(...)` on the already-combined wildcard+specific list. The second sort is what actually determines final ordering for any real call, since it runs on the combined list; the first is redundant for that purpose today; nothing currently reads `byResourceType`/`wildcardPolicies` directly without going through `resolve(...)` afterward, so it costs nothing beyond one extra sort at boot, but it's worth knowing it isn't load-bearing on its own.

# Resolving a Request

```java
@Override
public List<AuthorizationPolicy> resolve(String resourceType, String action) {
    List<AuthorizationPolicy> specific = AuthorizationPolicy.WILDCARD_RESOURCE_TYPE.equals(resourceType)
        ? List.of()
        : byResourceType.getOrDefault(resourceType, List.of());

    List<AuthorizationPolicy> combined = new ArrayList<>(wildcardPolicies.size() + specific.size());
    combined.addAll(wildcardPolicies);
    combined.addAll(specific);
    combined.removeIf(policy -> !policy.supports(action));
    combined.sort(Comparator.comparingInt(AuthorizationPolicy::order));

    return List.copyOf(combined);
}
```

Four steps, each earning its place:

1. **Look up anything registered specifically for `resourceType`** — unless `resourceType` itself literally *is* `"*"`, in which case this deliberately returns an empty list instead of the wildcard bucket. See the next section for why that guard exists.
2. **Always include every wildcard policy**, regardless of what `resourceType` was asked for — this is the mechanism that lets `ActorStatusPolicy`, `WidgetCapabilityPolicy`, `OrganizationIsolationPolicy`, and `WidgetTenantIsolationPolicy` run on literally every authorization call without each resource-specific policy needing to know they exist.
3. **Filter by `supports(action)`** — a policy can be registered for a resource type but only have an opinion on some of its actions (a policy that only cares about `DELETE`, say, should never run at all when the action is `CREATE`). This filter runs on the *combined* wildcard+specific list, so a wildcard policy that doesn't support a given action is excluded from that one call, even though it stays fully registered for every other action.
4. **Sort the result by `order()`** — this is the sort that actually matters for real calls; see "Ordering and Ties" below.

# The Wildcard-Equality Guard, Explained

```java
List<AuthorizationPolicy> specific = AuthorizationPolicy.WILDCARD_RESOURCE_TYPE.equals(resourceType)
    ? List.of()
    : byResourceType.getOrDefault(resourceType, List.of());
```

This one line exists to prevent a very specific kind of duplication. Because `index()` stores the wildcard bucket under the key `"*"` inside `byResourceType` in addition to pulling it out into `wildcardPolicies`, a naive `resolve("*", ...)` call — asking the registry to resolve for the wildcard resource type itself, rather than a real one — would otherwise find the *same* wildcard policies twice: once via `wildcardPolicies` (added unconditionally, step 2 above) and again via `byResourceType.get("*")` (step 1, since `"*"` is a valid key in that map too). Without the guard, every wildcard policy would appear twice in the combined list, evaluated twice, for no reason. The guard exists purely to make `resolve("*", action)` behave sensibly rather than double-count — and there's a dedicated test proving exactly this (`resolve_doesNotDoubleCountWildcardPolicies_whenResourceTypeItselfIsWildcard`).

# Ordering and Ties

The engine's decision algebra evaluates policies strictly in the order this method returns them (see **[[Authorization Engine]]** and **[[AuthorizationDecision]]**), so the sort here isn't cosmetic — it's what makes the cheap actor-status/widget-capability vetoes run before the more expensive scope/membership reasoning, for instance. `resolve(...)` sorts explicitly rather than trusting the order Spring happened to inject the policies in — there's a dedicated test for this too (`resolve_mergesWildcardAndResourceSpecificPolicies_sortedByOrderRegardlessOfInjectionOrder`), which deliberately injects policies in scrambled order and asserts the registry still returns them sorted correctly.

One thing the sort doesn't resolve: two policies sharing the exact same `order()` value. Java's `List.sort` is a stable sort, so equal-order policies keep whatever relative order they arrived in — which, upstream of this class, is simply whatever order Spring's dependency injection happened to hand the `List<AuthorizationPolicy>` constructor parameter, and that ordering isn't something this codebase specifies or relies on anywhere. In practice this rarely matters: the deny-overrides algebra means a `DENY` from a same-order policy wins regardless of exactly where in the tie it sits, and `ABSTAIN` never affects ordering-sensitive outcomes at all. It would only become a real concern if two same-order policies could both legitimately `PERMIT` the same request for different reasons and something downstream cared which one's `policyName` got remembered first — worth keeping in mind if a future policy is given the same `order()` as an existing one on purpose.

# What Happens When Nothing Matches at All

```java
void resolve_returnsEmptyList_whenNoPolicyIsRegisteredAtAll() { ... }
```

An empty resolved list is a completely normal, expected outcome — it isn't a special case the registry has to guard against, and it isn't treated as an error here. `DefaultAuthorizationService` (see **[[Authorization Engine]]**) simply iterates over whatever list it received; an empty list means the loop body never runs at all, `permitted` stays `null`, and the engine falls through to its own default-deny (`NO_POLICY`) — exactly the same outcome as if every policy in a non-empty list had abstained. The registry doesn't need to distinguish "nobody's registered for this" from "everyone who is registered had nothing to say" — both collapse into the same downstream behavior, correctly.

# A Deliberate Contrast: No Uniqueness Enforcement Here

It's worth knowing this registry does **not** validate anything about the policies it collects — no check for duplicates, no check that a resource type isn't claimed twice. That's a deliberate difference from its sibling, `ScopeResolverRegistry` (`authorization/scope/`), which explicitly validates at startup that no two `ScopeResolver` beans claim the same entity type, throwing `IllegalStateException` if they do. The two registries have opposite expectations by design: a `ScopeResolver` is meant to be exclusive — one entity type, one authoritative way to compute its scope, so two competing resolvers for the same type is a real bug worth failing the boot over. An `AuthorizationPolicy`, by contrast, is meant to **stack** — the whole point of the wildcard policies is that several of them coexist under the same `"*"` resourceType, each contributing one independent opinion. Enforcing uniqueness here would break the engine's entire design.

# See Also

- **[[Authorization Engine]]** — the decision algebra that consumes whatever list this registry resolves.
- **[[AuthorizationContext]]** — `resourceType`/`action` on this record are exactly what gets passed into `resolve(...)`.
- **[[AuthorizationDecision]]** — what each resolved policy produces once it actually runs.
