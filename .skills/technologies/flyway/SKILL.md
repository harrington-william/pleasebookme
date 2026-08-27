---
title: Flyway
description: Flyway convention
---

# Flyway Convention

I don't know why I create this convention but I actually like it 😄

Every Flyway migration script must not contain any lines of comment, only pure SQL. Comments should live in documentation

## Version numbering

- Flyway version numbers must stay strictly incremental with no gaps.
- The next migration is always `largest existing version across every schema/domain + 1`. Never jump ahead.
- Before adding migration, check the actual highest version number across the whole `db/migration` tree (all domains share one global version sequence).
- Sequence new migrations by their real dependency order, not just by when they were authored, a migration that alters a column/table must have a version number *after* the migration that creates it, even if it was written later chronologically. If a new migration is discovered to depend on one that hasn't been numbered yet, renumber to keep dependency order intact rather than bolting the new one on wherever is convenient.