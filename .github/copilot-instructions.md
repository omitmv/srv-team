# GitHub Copilot repository instructions

This repository is executing the MVP implementation plan documented under `docs/Refinamentos Técinicos/MVP/`.

## Source of truth

Before changing production code, read:

1. `docs/Refinamentos Técinicos/MVP/revisao-transversal-plano-implementacao.md`
2. the phase-specific refinement that owns the feature being changed;
3. `docs/Refinamentos Técinicos/MVP/refinamento-implementacao.md`;
4. modernization documents when build/platform code is involved.

If code conflicts with an approved refinement, preserve the approved refinement unless the task explicitly requests a business-rule change. Never silently reinterpret a documented invariant.

## Implementation order

Follow the implementation sequence defined by the transversal plan. Do not skip dependency gates merely because a later feature is easier to implement.

## Architecture

Use package-by-feature. Keep business rules in application/service/domain code, not controllers, mappers, repositories, exporters, or DTOs.

Prefer the simplest design that preserves the documented invariants. Do not introduce interfaces, factories, events, abstractions, or infrastructure without a concrete need.

## Persistence

- MySQL is authoritative.
- Flyway is the only schema-evolution mechanism.
- Hibernate must validate schema; do not rely on automatic DDL creation/update.
- Persist enums as STRING.
- Use `BigDecimal` for score and penalty values.
- Use LAZY associations by default.
- Avoid `CascadeType.ALL` across aggregate boundaries.
- `@Version` protects concurrent changes to the same row only. Cross-row invariants require database constraints and/or explicit locking.
- Translate expected uniqueness/concurrency conflicts to HTTP 409.

## Domain invariants

Always preserve these separations:

- `Resultado` is the sports fact.
- `TemporadaPontuacao` and `TemporadaPenalidade` are the current interpretation rules.
- `Ranking` is a derived projection.
- `VinculoProfissionalAtleta` is the real professional-athlete relationship.
- `TemporadaProfissional` is contextual authorization, never a real relationship.
- `Campeonato` is a shared sports fact/context and is not owned by one `Temporada`.

Never persist season score as authoritative state inside `Resultado`.

## Security and authorization

Do not authorize by role name alone when the documented rule is contextual. Centralize season authorization and reuse it.

`CONSULTA` is read-only. It must never gain mutation permissions by accident.

Do not log credentials, JWTs, database secrets, or personal access tokens. Never reintroduce plaintext secrets into versioned configuration.

## Tests and completion criteria

A task is not complete merely because it compiles. For each implementation slice:

1. implement the smallest coherent change;
2. add/update unit tests for business rules;
3. add MySQL/Testcontainers integration tests for persistence, constraints, migrations, locking, or SQL behavior when applicable;
4. run the relevant Maven verification commands;
5. inspect failures rather than weakening tests or constraints;
6. report files changed, migrations introduced, tests run, remaining risks, and the next implementation gate.

Do not delete legacy structures until the approved migration, compatibility, and rollback conditions have been satisfied.

## Autonomous execution

When asked to implement the plan or the next phase, first determine the current completed gate from repository state. Execute only the next safe implementation slice unless the user explicitly requests a broader batch. Stop and surface a blocker when continuing would require inventing business data, weakening an invariant, exposing a secret, or making an undocumented product decision.