# `integration`

> Owns third-party connectivity — delegated Google authorization, external sync destinations (Calendar, Sheets, Drive), and the async sync work queue.

## Tables

| Table                  | Documentation                          |
| ------------------------ | ------------------------------------------- |
| `oauth_connections`       | [[Table OAuth Connections]]       |
| `destination_calendars`   | [[Table Destination Calendars]]   |
| `destination_sheets`      | [[Table Destination Sheets]]      |
| `destination_drives`      | [[Table Destination Drives]]      |
| `sync_jobs`               | [[Table Sync Jobs]]               |

## Related Schemas

- [[Auth Schema]]
- [[Tenant Schema]]
- [[Core Schema]]

The `oauthstate` concern within this domain has no database table — it is Redis-backed (`integration:oauthstate`) and is not documented here.
