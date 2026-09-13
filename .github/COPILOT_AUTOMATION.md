# Copilot automation for the MVP implementation

This repository uses Copilot custom instructions, agents, skills and prompt files to execute the approved MVP implementation plan incrementally.

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

```text
/implement-next-phase
        |
        v
implementation-orchestrator
        |
        +--> relevant specialist/skills
        |
        v
build + tests + migration checks
        |
        v
/review-current-phase
        |
   PASS + evidence?
      /      \
    no        yes
    |          |
 fix gate    next gate
```

Do not request "implement the entire MVP" as one unbounded run. The goal is autonomous execution **inside each gated slice**, not removal of architecture/data-safety checkpoints.

## MCP strategy

MCP is not the implementation engine. It gives agents access to external systems.

For the current repository implementation, all approved product/architecture context is versioned under `docs/`, so no extra MCP server is required for the core implementation loop.

Useful MCP integrations when available:

1. **GitHub MCP** — repository/issues/PR/actions context; GitHub provides this by default to Copilot cloud agent.
2. **Playwright MCP** — useful later for end-to-end frontend/browser validation; also provided by default to Copilot cloud agent.
3. **Confluence MCP** — recommended when corporate standards/documentation must be consulted during implementation. Configure this only with an approved corporate MCP endpoint/authentication model; do not commit credentials.
4. Observability/database/service-catalog MCPs — add only when a concrete implementation gate requires those external tools.

For GitHub cloud agent, repository-level MCP configuration is managed in GitHub repository Settings -> Copilot -> MCP servers. Secrets/variables must use the supported Copilot Agents secret mechanism, never a committed token.

For local VS Code, MCP configuration can be workspace/user scoped. Do not commit local credentials into `.vscode/mcp.json` or another config file.

## Hooks

Hooks are intentionally not enabled yet.

Reason: deterministic quality hooks should call the Maven Wrapper, but the Wrapper/platform modernization is the first implementation gate and is not yet guaranteed to be present/configured correctly. Adding an unconditional `mvnw verify` hook now would make Copilot sessions fail before the modernization gate is completed.

After the modernization gate passes, consider a Copilot CLI/cloud-agent `agentStop` quality hook that runs a repository verification script. Keep the script itself in the repository so the same command can be used by humans, CI and agents.

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