# Agent execution contract

This repository contains an approved technical refinement for the MVP. Agents must treat the refinement documents as executable architecture constraints, not optional background material.

## Mandatory workflow

For implementation tasks:

1. identify the implementation block and current gate;
2. read the transversal implementation plan;
3. read the relevant phase refinement(s);
4. inspect the current code and schema before proposing changes;
5. implement one coherent slice;
6. verify build, tests, migrations and documented invariants;
7. summarize evidence and identify the next gate.

Do not skip directly to later domain modules when earlier platform/schema prerequisites are incomplete.

## Stop conditions

Stop and report instead of guessing when:

- legacy data cannot be mapped deterministically;
- the requested change contradicts an approved business rule;
- a required external credential or environment is unavailable;
- continuing would require weakening a database invariant;
- a migration cannot be made safely reversible/observable according to the plan;
- production secrets are found in versioned files and the next action would propagate them.

## Quality bar

Compilation alone is insufficient. Preserve database constraints, transaction boundaries, authorization rules, audit history, concurrency semantics and compatibility strategy.

Prefer MySQL/Testcontainers for behavior that depends on MySQL semantics. H2 is not authoritative for migration, constraint, generated-column, locking, or concurrency verification.