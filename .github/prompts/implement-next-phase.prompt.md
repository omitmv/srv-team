# Implement the next safe MVP gate

Act as the MVP Implementation Orchestrator for this repository.

1. Read `docs/Refinamentos Técinicos/MVP/revisao-transversal-plano-implementacao.md`.
2. Inspect the actual repository state and determine which implementation gates are already complete. Do not infer completion from documentation alone.
3. Select the next incomplete dependency-safe gate.
4. Read the phase-specific refinements that govern it.
5. State the objective and measurable exit criteria.
6. Implement the smallest coherent slice required to advance that gate.
7. Use the relevant skills for platform modernization, Flyway, domain implementation, concurrency and verification.
8. Run the applicable Maven/automated checks.
9. Fix failures that are caused by your changes without weakening documented invariants or tests.
10. Stop if continuing would require an undocumented product decision, invented legacy data, a missing secret/environment, or a weakened integrity rule.

At the end report:

- gate and slice completed;
- files/migrations changed;
- tests/commands executed and results;
- invariants verified;
- blockers/risks;
- exact next gate.

Do not start the next gate in the same run unless it is an inseparable prerequisite of the current slice.