---
name: domain-slice-implementation
description: Use when implementing one approved MVP domain slice such as Campeonato, Vinculo, Temporada, Inscricao, Resultado, Ranking or Relatorio from its refinement document.
---

# Domain slice implementation

## Workflow

1. Read the owning phase refinement and the transversal plan.
2. Inspect the existing feature package and legacy dependencies.
3. List the invariants that must remain true after the change.
4. Implement the smallest vertical slice: persistence + application rule + API only where required.
5. Keep DTOs separate from entities.
6. Keep controllers thin and repositories free of business decisions.
7. Persist enums as STRING and associations LAZY by default.
8. Add positive and negative workflow/authorization tests.
9. Add MySQL integration tests whenever behavior depends on constraints, SQL, locking or transaction semantics.
10. Verify compatibility requirements before touching legacy endpoints/tables.

## Design guidance

Prefer explicit command operations over generic setters for state transitions. Keep state mutation and mandatory audit/history recording in one transaction.

Do not introduce a domain abstraction merely because it may be useful later. Introduce it when the current slice has more than one real implementation or when it isolates a documented cross-cutting rule.