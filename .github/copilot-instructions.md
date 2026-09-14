# GitHub Copilot repository instructions

This repository is executing the MVP implementation plan documented under `docs/Refinamentos Técinicos/MVP/`.

## Authoritative execution context

The authoritative implementation base branch is `release`.

Before changing production code, build files, configuration, migrations, or tests as part of the MVP implementation plan:

1. verify that the task/PR is based on the current `release` branch or that the working tree was created from the current `release` HEAD;
2. verify that the current snapshot contains `AGENTS.md`, `.github/COPILOT_AUTOMATION.md`, `.github/agents/implementation-orchestrator.md`, `.github/skills/implementation-plan/SKILL.md`, `.github/skills/verification-gates/SKILL.md`, and `docs/Refinamentos Técinicos/MVP/revisao-transversal-plano-implementacao.md`;
3. if the base is `main`, another stale branch, or mandatory context is missing, stop before modifying files and report `PRECONDITION_FAILED`.

Do not infer the authoritative base from a task name or `copilot/*` branch name. Validate the actual repository/base state.

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
- H2 must not substitute for required MySQL/Testcontainers verification when correctness depends on MySQL behavior.

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

Routine implementation gates are validated locally. GitHub Actions is reserved for release-candidate/homologation/pre-production verification and is manually triggered.

A task is not complete merely because it compiles, commits, pushes, or opens a PR. For each implementation slice:

1. implement the smallest coherent change;
2. add/update unit tests for business rules;
3. add MySQL/Testcontainers integration tests for persistence, constraints, migrations, locking, or SQL behavior when applicable;
4. run the relevant Maven verification commands;
5. before closing the gate, run full local verification, normally `mvnw clean verify` on Windows or `./mvnw clean verify` on Unix-like environments;
6. inspect failures rather than weakening tests or constraints;
7. report files changed, migrations introduced, tests run, remaining risks, and the next implementation gate only if the current gate actually passed.

A gate may be reported as `PASS` when the approved refinement is satisfied and all mandatory local checks pass. External CI is not required for routine gate completion.

A mandatory local check that fails, is skipped when mandatory, or is not executed even though available is not PASS.

When explicitly validating a release candidate, manually run GitHub Actions `Verify`; only then may release-level status be reported as `PASS_RELEASE` after CI succeeds.

The legacy system is discontinued and the legacy database does not need to be preserved. Do not introduce legacy database baseline/migration compatibility requirements unless explicitly requested by a new product decision.

## Autonomous execution

When asked to implement the plan or the next phase, first validate the authoritative execution context and then determine the current completed gate from repository state. Execute only the next safe implementation slice unless the user explicitly requests a broader batch. Stop and surface a blocker when continuing would require inventing business data, weakening an invariant, exposing a secret, or making an undocumented product decision.