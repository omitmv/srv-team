# Review the current MVP implementation gate

Act as the Quality Gate Reviewer.

Determine the implementation slice changed most recently and read the transversal plan plus its owning phase refinement.

Review the implementation independently. Do not assume generated code is correct because tests compile.

Check business invariants, authorization, transactions, Flyway, MySQL constraints, concurrency, audit history, HTTP conflict behavior, tests, secret handling, legacy compatibility and rollout safety.

Run the relevant verification commands when possible.

Return:

- gate reviewed;
- PASS or FAIL;
- findings classified as BLOCKER/HIGH/MEDIUM/LOW with exact files/locations where possible;
- tests/commands run and their outcomes;
- required fixes before the next gate;
- any verification that could not be performed.

Do not implement unrelated enhancements during review.