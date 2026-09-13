In a Next.js frontend, the browser can give you the user's **IANA timezone** directly, which is exactly the format you want (`America/Los_Angeles`, `Asia/Tokyo`, `Europe/London`, etc.).

### Recommended: `Intl.DateTimeFormat`

You don't actually need a library:

```ts
const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

console.log(timezone);
// "Australia/Sydney"
// "America/Los_Angeles"
// "Europe/London"
```

In a React component:

```tsx
'use client';

import { useEffect, useState } from 'react';

export default function MyComponent() {
  const [timezone, setTimezone] = useState<string | null>(null);

  useEffect(() => {
    setTimezone(Intl.DateTimeFormat().resolvedOptions().timeZone);
  }, []);

  return <div>Timezone: {timezone}</div>;
}
```

### Important distinction

If you mean **browser timezone**, `Intl.DateTimeFormat` is the best approach.

If you mean **timezone based on IP/internet location**, that's different. You'd need an IP geolocation service, but I generally **wouldn't use IP timezone detection** when the browser API is available. IP-based detection can be wrong, especially with VPNs, mobile networks, proxies, or users traveling.

For example:

```text
Browser:
Intl.DateTimeFormat().resolvedOptions().timeZone
                    ↓
            "America/Los_Angeles"
```

This is already an **IANA timezone identifier**, so you can pass it directly to libraries such as `date-fns-tz`, Luxon, Day.js timezone, or the backend.

### One Next.js caveat

Don't call this during server rendering:

```ts
// ❌ Server-side
const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
```

The server doesn't know the user's browser timezone. Run it client-side (`useEffect`, event handler, etc.), or send the detected timezone to your backend.

If you tell me whether you're using **date-fns, Day.js, Luxon, or just native JS**, I can show the cleanest way to use `"America/Los_Angeles"` throughout your Next.js app.