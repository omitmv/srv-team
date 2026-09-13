---
name: flyway-schema-evolution
description: Use for Flyway baseline, MySQL DDL, constraints, indexes, legacy data migration, schema validation, and migration testing in srv-team.
---

# Flyway schema evolution

## Rules

- Flyway is the only source of schema evolution.
- Preserve the migration dependency order from the transversal plan.
- Never edit an already-applied production migration to change history; add a new migration.
- Use MySQL-compatible SQL and verify it against MySQL/Testcontainers.
- Prefer DB constraints for structural invariants.
- Do not use application pre-checks as the sole protection against concurrent duplicate creation.
- Legacy migration must be deterministic. If a row cannot be mapped unambiguously, preserve it and report divergence; never invent values.

## Workflow

1. Inspect existing migration versions and baseline state.
2. Identify table/FK dependency order.
3. Write the smallest migration for the slice.
4. Add constraints/indexes only with a documented invariant/query need.
5. Write migration/persistence tests using MySQL Testcontainers.
6. Verify clean/baselined startup and failure behavior.
7. Document any manual reconciliation requirement.

For conditional/current-row uniqueness, use the approved MySQL strategy from the owning phase refinement rather than assuming a portable partial index exists.