---
name: implementation-plan
description: Use when planning, starting, resuming, or advancing the approved MVP implementation. Determines the current gate, required refinement documents, dependencies, exit criteria, and next safe slice.
---

# MVP implementation plan

## Canonical document

Always begin with `docs/Refinamentos Técinicos/MVP/revisao-transversal-plano-implementacao.md`.

Then load only the phase-specific refinement(s) relevant to the current slice.

## Workflow

1. Inspect repository state rather than assuming progress from conversation/history.
2. Identify the latest completed implementation gate using code, migrations and tests as evidence.
3. Identify the next incomplete dependency-safe gate.
4. State the slice objective and explicit exit criteria.
5. Implement or delegate only that coherent slice.
6. Run verification.
7. Keep the gate open if verification fails.
8. Report the next gate.

## Gate order

Platform/build -> Flyway/Testcontainers -> Catalogs/Campeonato -> Vinculo -> Temporada/access -> scoring/penalties -> Inscricao -> Resultado -> Ranking -> Reports/exports -> compatibility cleanup.

Do not use this order as a substitute for reading the canonical plan; it is only a navigation summary.

## Blockers

Do not advance when implementation would require inventing product semantics, inventing legacy data, weakening an invariant, bypassing Flyway, or propagating secrets.