---
name: verification-gates
description: Use before declaring any srv-team implementation phase complete. Runs the required build, unit, integration, migration, authorization and integrity checks and reports evidence.
---

# Verification gates

A phase is not complete because code was generated, compiled, committed, pushed, or placed in a pull request.

## Mandatory environment precheck

Before evaluating implementation completion, verify that the work is based on the authoritative `release` branch and that the repository snapshot contains the current automation/refinement files.

If the task/PR base is not `release`, the branch is stale relative to `release`, or required context files are absent, return `PRECONDITION_FAILED` and stop. Do not evaluate the implementation as complete against an obsolete repository state.

## Verification sequence

1. Run the Maven Wrapper with the narrowest relevant tests during iteration.
2. Run the full required verification before closing the gate.
3. Verify MySQL/Testcontainers tests for migrations, locking, generated/conditional uniqueness and SQL behavior whenever the gate depends on database semantics.
4. H2 may be used only for tests whose correctness is database-agnostic; it must never substitute for mandatory MySQL/Testcontainers evidence.
5. Verify negative authorization/workflow cases.
6. Verify expected conflicts map to controlled API responses.
7. Check that required histories are written atomically and remain append-only.
8. Check no plaintext secret/token/credential was introduced.
9. Compare the implementation against the owning refinement, not only against existing legacy code.
10. Report skipped/unavailable checks explicitly.
11. Inspect repository CI/check results when available.

## Status model

Use exactly one of these statuses:

- `PASS` — every mandatory available local check passed and every required final CI/check has concluded successfully.
- `FAIL` — at least one mandatory check failed, was cancelled, returned `action_required`, or a blocker/high-severity invariant violation remains.
- `BLOCKED` — verification cannot proceed because a required environment, secret, external dependency, product decision, or authoritative context is unavailable.
- `PENDING_EXTERNAL_CI` — local verification passed, but required PR/CI checks have not yet completed or cannot be observed in the current session.
- `PRECONDITION_FAILED` — execution is not based on the authoritative repository state.

A pending, skipped-when-mandatory, failed, cancelled, or `action_required` check is never PASS.

## Evidence requirements

For each mandatory check report:

- exact command/check name;
- result;
- relevant failure reason when not successful;
- whether the failure is caused by this slice or pre-existing;
- whether the gate remains open.

When failing, provide the exact failing command/test/check and keep the current gate open. Do not announce or begin the next gate.