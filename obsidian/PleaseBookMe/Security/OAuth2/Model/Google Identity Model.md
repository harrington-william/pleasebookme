Somewhere underneath every Google-related flow in this codebase — Sign-In, Delegated Authorization, One-Shot Registration, all three — Google hands back a signed token asserting who the person on the other end actually is. That token is a JWT called an **ID token**, and it's a different thing entirely from the access token that lets us call Calendar or Sheets on the user's behalf. The ID token isn't a capability; it's a claim. It says "this is who authenticated," signed by Google, and nothing more.

The rest of the application never touches that raw JWT. It's decoded and verified exactly once, at one boundary, and everything on the other side of that boundary deals with a small, plain, already-trustworthy Java record instead: `GoogleIdentity`.

```java
public record GoogleIdentity(
    String sub,
    String email,
    boolean emailVerified,
    String name,
    String pictureUrl
) {}
```

# Where It Comes From?

`DefaultGoogleTokenVerifier` is the one place a `GoogleIdentity` is ever constructed. It hands the raw ID token to a `JwtDecoder`, which does the heavy cryptographic lifting — checking the signature against Google's public keys, confirming the token hasn't expired, confirming it was actually issued for *this* application and not some other Google client. By the time the code below runs, all of that has already passed:

```java
String sub = jwt.getClaimAsString("sub");
String email = jwt.getClaimAsString("email");
Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
String name = jwt.getClaimAsString("name");
String pictureUrl = jwt.getClaimAsString("picture");

if (sub == null || email == null) {
    throw new InvalidGoogleIdTokenException("Google ID token is missing required claims");
}

return new GoogleIdentity(sub, email, Boolean.TRUE.equals(emailVerified), name, pictureUrl);
```

It's just five claims, pulled straight off the token and repackaged. `sub` and `email` are treated as non-negotiable — a token missing either one is rejected outright, because there's nothing meaningful `GoogleIdentity` could represent without them. The other three are allowed to come back empty, for reasons that are worth walking through one at a time.

# What Each Field Actually Means?

## `sub`

Google's own permanent, stable identifier for the account — a string that never changes for as long as that Google account exists, regardless of what the person does to their profile. This is the field the rest of the system treats as the *real* identity, and it's worth dwelling on why: a person's email address is not actually permanent. Someone can change the email tied to their Google account, or an organization can rename its domain. `sub` can't drift out from under you the way an email address can, which is exactly why account lookups in this codebase check `sub` first, before ever consulting email at all — see "How the Rest of the System Uses This" below.

## `email`

The address Google associates with the account at the moment of sign-in. Useful, human-meaningful, and also the thing this codebase is most careful about — see the next field.

## `emailVerified`

Arguably the single most consequential field on this entire record, out of proportion to how small it looks. Here's the story that makes it matter: suppose someone creates a Google account using an email address that happens to belong to an existing PleaseBookMe user — maybe a company email nobody's checked in a while, maybe a domain Google hasn't actually confirmed the person controls. If this codebase linked a Google account to an existing platform user purely because the email addresses matched, that person could sign in as anyone whose email they merely *claimed*, without ever proving they controlled it. `emailVerified` is Google's own attestation that this isn't a claim — that Google itself confirmed ownership of the address. Every place in this codebase that would otherwise link a Google identity to an existing account checks this flag first, and refuses the link outright if it's false. It costs nothing to a legitimate new signup — there's no existing account to protect in that case — so the check only ever bites the one scenario it exists to catch.

Notice the field's type: `boolean`, not the boxed `Boolean`. That's a small but deliberate signal. Google's own claim can technically be missing or `false`, and the verifier collapses both of those into a plain `false` (`Boolean.TRUE.equals(emailVerified)` — this reads as `true` only when the claim is present *and* explicitly `true`, and quietly treats "the claim wasn't there at all" the same as "Google said no"). Once that decision is made, the field can never be genuinely unknown from this point forward — so giving it the primitive type is the record being honest about that: this is settled information, not something the rest of the code needs to null-check.

## `name` & `pictureUrl`

A display name and an avatar URL, both purely cosmetic, both allowed to be absent because Google doesn't always return them and nothing here depends on either one being present. When `name` is missing, the resolver that provisions a new account simply falls back to the email address instead — a small, low-stakes accommodation, not a gate like `emailVerified` is.

# How the Rest of the System Uses This?

`GoogleAccountResolver` is the one consumer that actually acts on a `GoogleIdentity`, and it does so in a fixed order that mirrors the reasoning above almost exactly:

First, it looks for an existing link by `sub` — has this exact Google account signed in here before? If so, this is simply a returning user, and nothing else about the identity matters; the lookup is done.

If there's no existing link, it falls back to looking for a platform account with a matching `email` — but only *proposes* linking to it, and only actually links if `emailVerified` is true. A matching email on an unverified token is treated as a stranger, not a returning user.

If neither lookup finds anyone, it provisions a brand-new user from scratch, deriving a username out of the local part of the email address, and links the new `sub` to that new account for next time.

The same `GoogleIdentity`, produced by the same verifier, is what both Sign-In and Delegated Authorization/One-Shot Registration hand to this resolver — there's exactly one code path that turns "a Google ID token" into "a platform user," reused everywhere Google identity needs resolving, rather than three flows quietly growing three slightly different versions of the same logic.

# See Also

- **[[Google OAuth2 Architecture]]** — where ID-token verification fits alongside the authorization-code exchange in the full Delegated Authorization flow.
- **[[Google State]]** — the `state` mechanism that ties a callback back to the request that started it; a different concern from identity, but part of the same callback.
