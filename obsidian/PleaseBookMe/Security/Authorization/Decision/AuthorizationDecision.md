# What It Is

`AuthorizationDecision` is the one outcome type in the whole engine, and it's deliberately used at two different scales that are easy to conflate: it's what a **single policy** hands back after looking at an `AuthorizationContext`, and it's also what the **whole engine** hands back after combining every policy's answer. There's no separate "vote" type and "final verdict" type — one shape serves both, which is a small design economy worth noticing, because it means reading a decision in a log or a debugger doesn't immediately tell you whether you're looking at one policy's opinion or the engine's final word. `policyName` (below) is how you tell them apart.

Package: `com.pleasebookme.server.security.authorization.decision`.

```java
public record AuthorizationDecision(
    DecisionEffect effect,
    String code,
    String reason,
    String policyName
) {
    public AuthorizationDecision {
        if (effect == null) {
            throw new IllegalArgumentException("Decision effect is required");
        }
    }

    public boolean granted() {
        return effect == DecisionEffect.PERMIT;
    }

    public static AuthorizationDecision permit(String policyName) { ... }
    public static AuthorizationDecision deny(String policyName, String code, String reason) { ... }
    public static AuthorizationDecision abstain(String policyName) { ... }
}
```

# `DecisionEffect` — the Three Possible Answers

```java
public enum DecisionEffect {
    PERMIT,
    DENY,
    ABSTAIN
}
```

This is the one field every consumer branches on, and the only field with a strict validity requirement — the compact constructor throws if `effect` is `null`, because an `AuthorizationDecision` with no effect isn't a degraded decision, it isn't a decision at all. `code`, `reason`, and `policyName` are just descriptive strings by contrast; nothing enforces they're non-blank, because their entire purpose is human- and log-readability, not control flow.

Three values, three genuinely different roles inside the engine described in **[[Security/Authorization/Authorization Engine]]**:

- **`PERMIT`** — this policy actively grants the request. At least one `PERMIT`, with no later `DENY`, is what it takes for the whole engine to grant anything at all.
- **`DENY`** — this policy actively refuses the request, and does so with veto power: a single `DENY`, from any policy, at any point in the evaluated list, ends the whole call as denied. Nothing that ran before it and nothing that would have run after it changes that.
- **`ABSTAIN`** — this policy has no opinion about this particular request. Evaluation simply continues to the next policy as if this one hadn't run at all.

# `granted()` Answers a Narrower Question Than It Looks Like

```java
public boolean granted() {
    return effect == DecisionEffect.PERMIT;
}
```

`granted()` collapses three states down to two — `true` only for `PERMIT`, `false` for both `DENY` and `ABSTAIN` alike. That's exactly the right behavior for the one place this method is actually called (`AuthorizationService.require(...)`, which just needs to know whether to throw), but it's worth knowing what it throws away: from `granted()` alone, there is no way to tell whether a request was actively refused by a policy or simply never affirmatively granted by any of them. If that distinction ever matters — logging, debugging, telling a caller *why* — read `effect()` and `code()` directly instead of relying on `granted()`.

# The Three Factories, and Why Each Shapes Its Arguments Differently

```java
public static AuthorizationDecision permit(String policyName) {
    return new AuthorizationDecision(DecisionEffect.PERMIT, "PERMIT", "Access granted", policyName);
}

public static AuthorizationDecision deny(String policyName, String code, String reason) {
    return new AuthorizationDecision(DecisionEffect.DENY, code, reason, policyName);
}

public static AuthorizationDecision abstain(String policyName) {
    return new AuthorizationDecision(
        DecisionEffect.ABSTAIN,
        "NO_POLICY",
        "No policy expressed an opinion for this resource type and action",
        policyName
    );
}
```

Notice these three don't have parallel signatures, and that asymmetry reflects something real about each outcome: `permit(...)` takes only a `policyName` — a grant doesn't need its own sub-reason, "yes" is the whole message, so `code`/`reason` are fixed literals (`"PERMIT"` / `"Access granted"`). `deny(...)` is the one factory that takes a caller-supplied `code` and `reason`, because there are many different ways to refuse a request (`MISSING_PERMISSION`, `OUT_OF_SCOPE`, `ACTOR_INACTIVE`, `WIDGET_CAPABILITY_DENIED`, and whatever a future policy invents), and *which* one it was is exactly the information worth preserving. `abstain(...)` again takes only `policyName`, with `code`/`reason` fixed — "I have no opinion" doesn't need elaboration either.

# One Genuine Gotcha: Two Different Things Share the Code `"NO_POLICY"`

This is worth flagging explicitly, because it's the one place reading a decision's `code` alone can mislead you. `AuthorizationDecision.abstain(...)` hardcodes its `code` to `"NO_POLICY"` — that's one policy, individually, declining to have an opinion. But `DefaultAuthorizationService.authorize(...)` also produces its own final fallback decision, coded exactly the same way, when nothing in the entire resolved policy list ever permitted anything:

```java
return permitted != null
    ? permitted
    : AuthorizationDecision.deny(
        "AuthorizationService",
        "NO_POLICY",
        "No policy permitted " + context.action() + " on " + context.resourceType()
    );
```

So `code() == "NO_POLICY"` can mean either "this one policy had nothing to say" (`effect() == ABSTAIN`) or "the entire engine, after consulting every policy, found nothing that granted this" (`effect() == DENY`, and `policyName() == "AuthorizationService"`). The two are only distinguishable by checking `effect()` alongside `code()` — never trust `code()` in isolation to mean one specific thing, here specifically.

That second detail doubles as a debugging tip worth remembering on its own: **`policyName() == "AuthorizationService"` in a denied decision is never a real policy** — it's the engine's own synthesized fallback, meaning "the resolved policy list ran to completion and nobody actually granted this." If you're tracing why a request got denied and the policy name isn't one of your actual `AuthorizationPolicy` beans, this is why.

# See Also

- **[[Security/Authorization/Authorization Engine]]** — the full decision algebra (deny-overrides, default-deny) that consumes a whole list of these and produces one final one.
- **[[AuthorizationContext]]** — what a policy is looking at when it produces one of these.
- **[[AuthorizationPolicyRegistry]]** — how the list of policies that get a chance to produce a decision is assembled in the first place.
