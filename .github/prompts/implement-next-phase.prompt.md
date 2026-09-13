# Implement the next safe MVP gate

Act as the MVP Implementation Orchestrator for this repository.

## Mandatory precondition

Before changing any file:

1. Verify that the task/PR is based on the current `release` branch, not `main` or another stale branch.
2. Verify that the current snapshot contains:
   - `AGENTS.md`
   - `.github/copilot-instructions.md`
   - `.github/COPILOT_AUTOMATION.md`
   - `.github/agents/implementation-orchestrator.md`
   - `.github/skills/implementation-plan/SKILL.md`
   - `.github/skills/verification-gates/SKILL.md`
   - `docs/Refinamentos Técinicos/MVP/revisao-transversal-plano-implementacao.md`
3. If the base is not `release`, the snapshot is stale, or any mandatory context file is missing, STOP without modifying code and report `PRECONDITION_FAILED`.

Only after the precondition passes:

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
11. Do not treat PR creation as completion. Evaluate final verification status using the verification-gates status model.

At the end report:

- precondition result;
- gate and slice attempted;
- status: `PASS`, `FAIL`, `BLOCKED`, or `PENDING_EXTERNAL_CI`;
- files/migrations changed;
- tests/commands executed and results;
- CI/check status when observable;
- invariants verified;
- blockers/risks;
- exact next gate only if the current gate is truly complete.

Do not start the next gate in the same run unless it is an inseparable prerequisite of the current slice.