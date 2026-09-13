```text
GET /api/v1/integrations/google/callback the same callback both flows use

	consume state (GETDEL)

	exchange code, verify id_token outside any transaction

	finalizeOnboarding(tokens, identity) @Transactional

		resolve-or-provision user (GoogleAccountResolver)

		persist oauth_connection (GoogleConnectionWriter)

		issue handoff code -> Redis, 60s

-> 302 {frontend}{redirectAfter}?google=connected&handoff=<code>
```

