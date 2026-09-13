---
name: concurrency-integrity
description: Use when a srv-team change involves uniqueness, simultaneous commands, optimistic/pessimistic locking, placement reservation, current-row constraints, or HTTP 409 conflict behavior.
---

# Concurrency and integrity

## Core rule

Choose the protection according to the race being prevented:

- same existing row edited concurrently -> `@Version` and optimistic conflict;
- serialized aggregate transition -> pessimistic lock/equivalent where documented;
- two different new rows competing for one logical invariant -> database unique/conditional constraint;
- sports placement -> physical placement reservation with a unique key.

Never claim `existsBy(...)` makes a create operation race-safe.

## Workflow

1. Describe the competing transactions explicitly.
2. Identify the invariant that must survive both commit orders.
3. Put the final integrity guarantee in the database when structurally expressible.
4. Add application pre-check only for clearer UX, not as the only guarantee.
5. Catch the expected persistence conflict and translate it to the documented domain/API conflict, normally HTTP 409.
6. Add a concurrent integration test against MySQL/Testcontainers.
7. Verify rollback leaves no orphan audit/history/reservation records.

Do not broaden locks beyond the aggregate/key that needs serialization.