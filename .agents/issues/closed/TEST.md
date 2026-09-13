# Two pre-existing bugs fixed in the same pass

Both were live on the connect flow; the one-shot only made them user-facing on first contact.

- `DEFAULT_REDIRECT_AFTER` was `/settings/integrations`, which is not a route in the Next.js app. Now `/dashboard/settings/integrations`.
- `complete()` returned on `error` **before** consuming the state. Google sends `state` on error callbacks too, so this discarded the only record of where the user came from and left the state replayable for the rest of its TTL. The consume now happens first.