---
name: verification-gates
description: Use before declaring any srv-team implementation phase complete. Runs the required build, unit, integration, migration, authorization and integrity checks and reports evidence.
---

# Verification gates

A phase is not complete because code was generated, compiled, committed, pushed, or placed in a pull request.

## Mandatory environment precheck

Before evaluating implementation completion, verify that the work is based on the authoritative `release` branch and that the repository snapshot contains the current automation/refinement files.

If the task/PR base is not `release`, the branch is stale relative to `release`, or required context files are absent, return `PRECONDITION_FAILED` and stop. Do not evaluate the implementation as complete against an obsolete repository state.

## Verification strategy

Implementation gates are validated locally. GitHub Actions is a release-level verification mechanism and is not required for every gate/PR.

During gate development:

1. Run the Maven Wrapper with the narrowest relevant tests during iteration.
2. Run the full required local verification before closing the gate, normally `mvnw clean verify` on Windows or `./mvnw clean verify` on Unix-like environments.
3. Verify MySQL/Testcontainers tests for migrations, locking, generated/conditional uniqueness and SQL behavior whenever the gate depends on database semantics.
4. H2 may be used only for tests whose correctness is database-agnostic; it must never substitute for mandatory MySQL/Testcontainers evidence.
5. Verify negative authorization/workflow cases.
6. Verify expected conflicts map to controlled API responses.
7. Check that required histories are written atomically and remain append-only.
8. Check no plaintext secret/token/credential was introduced.
9. Compare the implementation against the owning refinement, not only against existing legacy code.
10. Report skipped/unavailable checks explicitly.

GitHub Actions `Verify` should be executed manually when validating a release candidate, homologation candidate, or pre-production release. A gate PR does not need CI execution when all mandatory local checks are available and pass.

## Status model

Use exactly one of these statuses:

- `PASS` — the gate matches the approved refinement and every mandatory local check available for that gate passed. External CI is not required for routine gate completion.
- `FAIL` — at least one mandatory local check failed, a mandatory test was skipped, or a blocker/high-severity invariant violation remains.
- `BLOCKED` — verification cannot proceed because a required environment, secret, external dependency, product decision, or authoritative context is unavailable.
- `PRECONDITION_FAILED` — execution is not based on the authoritative repository state.

For release-level verification, report `PASS_RELEASE` only when the manually triggered GitHub Actions `Verify` workflow and any other required release checks conclude successfully.

A pending release workflow does not invalidate an already approved implementation gate; it only means release-level verification has not yet been established.

## Evidence requirements

For each mandatory check report:

- exact command/check name;
- result;
- relevant failure reason when not successful;
- whether the failure is caused by this slice or pre-existing;
- whether the gate remains open.

When failing, provide the exact failing command/test/check and keep the current gate open. Do not announce or begin the next gate.

When validating a release candidate, additionally report the manually triggered GitHub Actions workflow result and distinguish gate-level `PASS` from release-level `PASS_RELEASE`.