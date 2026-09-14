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
6. Prefer an analysis-first approach before implementation when the gate introduces new domain concepts or multiple decisions.
7. Implement the smallest coherent slice required to advance that gate.
8. Use the relevant skills for platform modernization, Flyway, domain implementation, concurrency and verification.
9. Run narrow local tests during iteration.
10. Before closing the gate, run the complete local verification required by the slice, normally `mvnw clean verify` on Windows or `./mvnw clean verify` on Unix-like environments.
11. Fix failures caused by your changes without weakening documented invariants or tests.
12. Stop if continuing would require an undocumented product decision, invented legacy data, a missing secret/environment, or a weakened integrity rule.
13. Do not treat PR creation or GitHub Actions execution as routine gate completion. Evaluate the gate using local verification according to `verification-gates`.

GitHub Actions `Verify` is reserved for manually triggered release-candidate/homologation/pre-production verification and is not required for each implementation gate.

At the end report:

- precondition result;
- gate and slice attempted;
- status: `PASS`, `FAIL`, `BLOCKED`, or `PRECONDITION_FAILED`;
- files/migrations changed;
- tests/commands executed and results;
- local verification evidence;
- invariants verified;
- blockers/risks;
- exact next gate only if the current gate is truly complete.

If the task explicitly requests release-level verification, also report the manually triggered GitHub Actions result and use `PASS_RELEASE` only when it succeeds.

Do not start the next gate in the same run unless it is an inseparable prerequisite of the current slice.