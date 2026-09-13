# Why a handoff code exists

The callback lands on Spring and redirects to the Next.js app, which owns the httpOnly session cookies. Spring cannot set a cookie for another origin, and putting tokens in the redirect query string would leak them into browser history, `Referer`, and every proxy log in between.

**Only the user reference goes into Redis — never the tokens.** The JWT pair is minted at exchange time. That makes the code in the URL bar a lookup key that dies on first use rather than a bearer credential with a TTL, and it means the 15-minute access token starts its life when the session actually begins rather than at callback time. `GETDEL` gives single-use consumption in one atomic command, same as `OAuthStateStore`.