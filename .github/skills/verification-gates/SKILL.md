---
name: verification-gates
description: Use before declaring any srv-team implementation phase complete. Runs the required build, unit, integration, migration, authorization and integrity checks and reports evidence.
---

# Verification gates

A phase is not complete because code was generated or compiled.

## Verification sequence

1. Run the Maven Wrapper with the narrowest relevant tests during iteration.
2. Run the full required verification before closing the gate.
3. Verify MySQL/Testcontainers tests for migrations, locking, generated/conditional uniqueness and SQL behavior.
4. Verify negative authorization/workflow cases.
5. Verify expected conflicts map to controlled API responses.
6. Check that required histories are written atomically and remain append-only.
7. Check no plaintext secret/token/credential was introduced.
8. Compare the implementation against the owning refinement, not only against existing legacy code.
9. Report skipped/unavailable checks explicitly.

## Gate result

Return PASS only when all mandatory checks available in the repository pass and no known BLOCKER/HIGH issue violates an approved invariant.

When failing, provide the exact failing command/test and keep the current gate open.