# Copilot automation for the MVP implementation

This repository uses Copilot custom instructions, agents, skills and prompt files to execute the approved MVP implementation plan incrementally.

## Mandatory execution precondition

The authoritative implementation base is `release`.

Every autonomous implementation run must validate this before modifying files:

```text
current task / PR / working tree
        |
        v
based on current release?
      /       \
    no         yes
    |           |
STOP            v
PRECONDITION  mandatory context files present?
FAILED          /       \
              no         yes
              |           |
            STOP          v
       PRECONDITION   discover next gate
          FAILED
```

Mandatory context files include:

- `AGENTS.md`
- `.github/copilot-instructions.md`
- `.github/COPILOT_AUTOMATION.md`
- `.github/agents/implementation-orchestrator.md`
- `.github/skills/implementation-plan/SKILL.md`
- `.github/skills/verification-gates/SKILL.md`
- `docs/Refinamentos Técinicos/MVP/revisao-transversal-plano-implementacao.md`

A `copilot/*` branch name is not evidence that the task started from `release`.

If a task is based on `main`, another stale branch, or a snapshot missing these files, the agent must stop without making implementation changes and report `PRECONDITION_FAILED`.

## Components

### Always-on instructions

- `.github/copilot-instructions.md`
- `/AGENTS.md`

These protect repository-wide invariants and execution rules.

### Agents

- `implementation-orchestrator`: determines the current gate and executes the next safe slice.
- `database-migration-specialist`: Flyway/MySQL/migrations/concurrency persistence specialist.
- `domain-implementation-specialist`: domain/workflow/authorization implementation specialist.
- `quality-gate-reviewer`: independent completion review.

### Skills

- `implementation-plan`
- `spring-platform-modernization`
- `flyway-schema-evolution`
- `domain-slice-implementation`
- `concurrency-integrity`
- `verification-gates`

Skills are intentionally small and composable. More specialized skills should be added only when repeated implementation work proves they are useful.

### Prompt files

- `/implement-next-phase` starts/resumes implementation from repository state.
- `/review-current-phase` independently validates the current gate.

Prompt files are convenience entry points; the durable behavior belongs in instructions/agents/skills.

## Recommended execution loop

Routine implementation gates use local verification as the completion authority:

```text
analysis/refinement
        |
        v
small implementation slice
        |
        v
narrow local tests during iteration
        |
        v
full local `mvnw clean verify`
        |
 local verification PASS?
      /       \
    no         yes
    |           |
 fix gate       v
          review current gate
                |
                v
              PASS
                |
                v
              PR/merge
```

GitHub Actions is not required for each gate or PR.

The `Verify` workflow is manually triggered for release-level validation, such as a release candidate, homologation candidate, or pre-production build:

```text
release candidate
        |
        v
manually trigger GitHub Actions Verify
        |
 CI successful?
      /       \
    no         yes
    |           |
   FAIL     PASS_RELEASE
```

Creating a pull request is not evidence that a gate passed. Local verification must still be complete and reproducible.

Do not request "implement the entire MVP" as one unbounded run. Prefer analysis first and then small coherent implementation slices. The goal is autonomous execution inside a bounded slice, not removal of architecture/data-safety checkpoints.

## Execution strategy and Copilot consumption

Prefer local VS Code Copilot Agent Mode for implementation. Keep cloud-agent usage for cases where remote execution provides concrete value.

Recommended workflow:

1. analyze the next gate without changes;
2. review architecture/business interpretation;
3. implement one coherent sub-slice at a time;
4. run narrow tests during iteration;
5. run `mvnw clean verify` before closing the gate;
6. create/review the PR;
7. merge after gate review;
8. run GitHub Actions manually only at release-level checkpoints.

This reduces unnecessary remote agent/CI consumption while keeping deterministic local evidence for every gate.

## MCP strategy

MCP is not the implementation engine. It gives agents access to external systems.

For the current repository implementation, all approved product/architecture context is versioned under `docs/`, so no extra MCP server is required for the core implementation loop.

Useful MCP integrations when available:

1. **GitHub MCP** — repository/issues/PR/actions context.
2. **Playwright MCP** — useful later for end-to-end frontend/browser validation.
3. **Confluence MCP** — recommended when corporate standards/documentation must be consulted during implementation. Configure this only with an approved corporate MCP endpoint/authentication model; do not commit credentials.
4. Observability/database/service-catalog MCPs — add only when a concrete implementation gate requires those external tools.

For local VS Code, MCP configuration can be workspace/user scoped. Do not commit local credentials into `.vscode/mcp.json` or another config file.

## Hooks

Hooks are intentionally not required as a substitute for the explicit local verification gate.

If an `agentStop` or equivalent quality hook is introduced later, it should call a repository-owned verification script using the Maven Wrapper and must not weaken or replace the manual evidence requirements of the current gate.

## Future useful skills

Create these only as their owning implementation phase starts:

- `campeonato-catalog-model`
- `season-context-authorization`
- `inscricao-workflow`
- `resultado-placement-reservation`
- `ranking-projection`
- `report-read-models`
- `legacy-compatibility-cleanup`

This avoids a large skill catalog whose instructions become stale before the corresponding code exists.