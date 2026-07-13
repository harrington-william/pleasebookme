In PLATFORM V1.0.0 version, some enterprise premium schemas are intentionally excluded

Excluded schemas
- billing
- analytics
- webhook

These are premium feature, not prerequisites of the system. Building these just increase complexity. We just need to focus on other schemas.

Same to tables, some tables such as API Keys are created but have no usage because they just not fit the current architecture. Later, when have enough supporting components, they will live
