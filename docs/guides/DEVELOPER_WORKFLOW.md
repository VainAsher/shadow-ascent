---
doc_type: developer_guide
status: living
last_updated: 2026-05-08
---
# Developer Workflow Guide

Operational guide for the human developer working on Shadow Ascent. Defines how the three AI tools are assigned, how they interact on this specific codebase, and the step-by-step workflows to follow for common development tasks.

---

## Operating Pattern — Claude→Codex Worker Loop

```text
Claude frames.   Codex grinds.   Claude judges.   User approves.
```

### Core Roles

| Role | Tool | Responsibility |
| --- | --- | --- |
| Architect | Claude Code | Understands goal, frames the task, reviews result, makes the decision |
| Worker | Codex CLI | Executes bounded terminal tasks: validation, scanning, patching, scaffolding |
| Implementer | Copilot | In-editor boilerplate and pattern completion |
| Approval gate | User | Authorises all write-mode and broad-scope actions |

Claude Code never treats Codex as an independent decision-maker. Codex inspects, validates, or patches, but Claude frames the task and reviews the result before anything proceeds.

### Task Classification

Before every substantial task, classify it:

| Class | What it means | Default tool |
| --- | --- | --- |
| `DESIGN` | Architecture, GDD, narrative, planning | Claude Code |
| `INSPECT` | Understand current repo state | Claude Code (targeted) or Codex |
| `VALIDATE` | Compile, test, regression, doc-staleness checks | Codex |
| `PATCH` | Small code or document change | Claude Code or Codex (narrow) |
| `REVIEW` | Critique existing change or diff | Claude Code |

Never advance from `VALIDATE` to `PATCH` without an explicit narrow task boundary confirmed by Claude Code.

For broad `INSPECT` tasks that require reading multiple large files simultaneously (e.g., all five data contracts before authoring a new plateau), spawn parallel `Explore` sub-agents rather than burning main context. Main Claude context is then reserved for the design reasoning that follows:

```text
Agent(subagent_type="Explore", prompt="Read plateaus.json, story_flags.json, narrative_beats.json
and summarize tag ranges and blocked flag keys relevant to a new Act II plateau...")
```

### Token Rationing Rule

Preserve Claude context wherever possible.

Do not read huge files, broad directories, full build logs, or entire generated outputs unless necessary. Prefer:
- Targeted file reads (specific lines, not whole files)
- Grep/ripgrep summaries
- Git diff stats
- Compiler and test section summaries
- Codex compressed reports

When using Codex, always require concise structured output. Do not ask Codex to return full logs or entire file contents.

---

## Codex Output Contract

Every Codex task must instruct Codex to return only this structure:

```markdown
# Codex Result

## Verdict
PASS | FAIL | PARTIAL | PATCHED

## Commands Run
- command 1
- command 2

## Files Read
- path

## Files Changed
- path: one-line reason

## Key Findings
1. finding
2. finding

## Risks / Uncertainties
1. risk

## Recommended Next Step
One narrow next action for Claude Code.
```

After Codex returns, Claude must:
1. Check whether the task matched the user's goal
2. Check whether Codex exceeded scope
3. Inspect changed files or summaries when necessary
4. Identify risks
5. State the next smallest useful step

---

## Codex Invocation Reference

Reusable task prompts live in `.codex/tasks/`. Invoke them from the VSCode terminal (PowerShell):

**Read-only / validation (suggest mode — Codex proposes, you approve each action):**

```powershell
codex exec "$(Get-Content .codex\tasks\<task-file>.md -Raw)"
```

**Write / patch (auto-edit mode — Codex edits files freely, pauses before running commands):**

```powershell
codex --approval-mode auto-edit "$(Get-Content .codex\tasks\<task-file>.md -Raw)"
```

**Never** invoke the interactive Codex TUI from inside Claude Code:

```powershell
# DO NOT run this from Claude Code
codex
```

**Never** use `full-auto` without explicit user approval and a disposable environment.

### Inline Template — Read-Only Validation

For ad-hoc validation tasks not covered by a task file:

```powershell
codex exec "
You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence.

Task:
<INSERT TASK>

Return only the standard Codex Result format (Verdict, Commands Run, Files Read, Files Changed, Key Findings, Risks, Recommended Next Step).
"
```

### Inline Template — Narrow Patch

For ad-hoc patch tasks not covered by a task file:

```powershell
codex --approval-mode auto-edit "
You are a bounded patch worker for Shadow Ascent.

Make the smallest safe change only.
Do not refactor unrelated code.
Do not reformat unrelated files.
Do not change behaviour unless explicitly requested.
After editing, run the smallest relevant validation command.

Task:
<INSERT TASK>

Return only the standard Codex Result format.
"
```

---

## Human Approval Gates

Ask the user before:

- Broad refactors affecting multiple files or modules
- Deleting files or directories
- Changing public APIs or inter-module interfaces
- Changing gameplay behaviour or mechanics
- Changing narrative canon (plateau IDs, beat IDs, story flags)
- Changing build or CI/CD configuration
- Running write-mode Codex for anything larger than a narrow patch
- Accepting a Codex patch as final without Claude Code review

Do not ask for approval for read-only inspection unless the command is unusual or sensitive.

---

## The Three-Tool Model

Each tool operates at a different cognitive altitude. Using the wrong tool for a task loses speed and quality.

| Tool | Altitude | What it does well |
| --- | --- | --- |
| **Claude Code** (this session) | Codebase | Reasons across files, understands architecture, makes decisions |
| **GitHub Copilot** (VSCode inline) | Cursor | Completes what you're already writing, follows established patterns |
| **Codex CLI** (VSCode terminal) | Task | Executes a bounded, scoped instruction end-to-end in the terminal |

The common mistake is using Copilot for architecture or Claude Code for boilerplate. Inverting that costs velocity.

---

## Tool Role Assignments — Shadow Ascent Specific

### Claude Code

**Role: Architect, Reviewer, Debugger, Migration Lead**

Use Claude Code for:

- **LibGDX production client architecture** — `DesktopLauncher` + `ShadowAscentGame` are the shipping entry points. Tiled `.tmx` map loading, `GameSimulator.drainEvents()` wiring, AABB collision integration, and scene/screen transitions all require cross-file reasoning. Copilot cannot hold the full rendering/simulation boundary in mind.
- **Migration wave planning** — Before importing anything from `indie-ninja-adventures` or the integrated package, Claude Code should review the donor class against existing clean-start interfaces and produce a bounded slice spec. See `docs/MIGRATION_MAP.md`.
- **Cross-contract narrative authoring** — When adding a new plateau or quest chain, Claude Code reasons across `plateaus.json`, `narrative_beats.json`, `story_flags.json`, and `chunk_grammar.json` simultaneously to catch tag leakage and flag conflicts before they're authored.
- **M6 open-world runtime expansion** — Regional streaming (`RegionManifest`, `RegionLoader`, `MutationOverlay`) is complete as of 2026-05-08. Active M6 work is open-world runtime on top of that foundation — Claude Code owns the design.
- **Regression logic review** — `RegressionTest.java` (~100KB, 49 tests) does serious work. Claude Code reviews new test cases for logical correctness, not just structural conformance.
- **PlaytestClient.java QA harness** — ~80KB Swing harness. Wave 4/5 subsystem extractions complete (CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer). No new feature code goes here; Claude Code governs what is extracted vs. left in place.
- **Bug diagnosis** — Paste the relevant files and error. Copilot guesses; Claude Code traces the actual code.

### GitHub Copilot

**Role: In-editor execution of known patterns**

Use Copilot for:

- **JSON contract authoring** — The schema in `data/schemas/narrative_data_schema.json` is established. Copilot completes new quest entries, NPC definitions, and beat structures reliably once you've started typing.
- **New ECS components** — Pattern is established: extend `Component`, implement fields, follow `TransformComponent`/`HealthComponent`/`AIComponent`. Copilot autocompletes faster than any other tool here.
- **Gradle task additions** — Once you've written one `runXxx` task, Copilot will complete additional tasks with high accuracy.
- **Regression test scaffolding** — The labelled-section structure in `RegressionTest.java` is repetitive. Copilot generates scaffold well. Claude Code then reviews the logic.
- **PlaytestClient subsystem boilerplate** — After Claude Code has designed the decomposition and defined subsystem interfaces, Copilot fills in method bodies and field declarations.

Copilot's project-specific instructions are already configured in `.vscode/settings.json`. These stop it from generating plausible-but-wrong Java that violates layer boundaries or hardcodes contract values.

### Codex CLI

**Role: Terminal agent for bounded, verifiable tasks**

Use Codex for:

- **Data cross-reference validation** — Cross-reference all `required_flags` entries across JSON contracts against `story_flags.json`. Use `.codex/tasks/cross-reference-flags.md`.
- **Playtest log analysis** — Parse all session logs under `logs/playtest/` and produce telemetry summaries with cross-session deltas. Use `.codex/tasks/playtest-log-summary.md`.
- **Wave completeness checks** — Verify a wave import is complete: compile, regression, layer check, map accuracy. Use `.codex/tasks/migration-completeness-check.md`.
- **Stale doc audits** — Check docs against git history for outdated class names, wrong milestone status, or contradicted implementation. Use `.codex/tasks/stale-doc-audit.md`.
- **Save migration utility scripts** — When SAVE_V4 fields are introduced, Codex writes the migration utility. Logic is bounded and testable.
- **Repo hygiene** — Check whether `bin/` or `build/` artifacts are tracked by git; suggest `.gitignore` additions.
- **Running and summarising Gradle output** — Full CI gate or targeted diagnostics. Use `.codex/tasks/validate-full-gate.md` or individual task files.

For Codex setup, invocation syntax, and approval modes, see `docs/guides/CODEX_CLI_SETUP.md`.

---

## Workflow 1 — New Plateau Addition (M4/M5)

```
Step 1  Claude Code     Review plateaus.json + story_flags.json + narrative_beats.json
                        Identify legal tag ranges, blocked flags, dependency chains
                        Produce plateau grammar spec + milestone flag list

Step 2  Copilot         Author the JSON entries from the spec
                        Complete NPC role assignments in npc_registry.json
                        Add chunk_grammar.json entries following existing tag patterns

Step 3  Codex CLI       Cross-reference all five contracts for the new plateau:
                        codex exec "$(Get-Content .codex\tasks\plateau-contract-check.md -Raw)"

Step 4  Claude Code     Review Codex plateau-contract-check result
                        Resolve any unresolved flags, tag leakage, or quest chain issues

Step 5  Copilot         Add regression test scaffolding for the new plateau
                        (labelled section, assertions, pass/fail counter)

Step 6  Claude Code     Review test logic for correctness, not just structure

Step 7  Codex CLI       Run contract diagnostics:
                        codex exec "$(Get-Content .codex\tasks\validate-data-contracts.md -Raw)"
```

---

## Workflow 2 — PlaytestClient.java QA Harness Maintenance

`PlaytestClient.java` is ~80KB. Wave 4/5 subsystem extractions are complete: CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer. Do not add new feature code to it — the LibGDX client is the shipping path. Only touch PlaytestClient when a regression test requires it or a QA harness improvement is needed.

```
Step 1  Claude Code     Identify the specific QA harness gap or regression requirement
                        Scope the smallest change to PlaytestClient that closes the gap
                        Do not redesign subsystem boundaries — extractions are complete

Step 2  Copilot         Implement the targeted change inside the existing harness structure

Step 3  Codex CLI       Diff risk check — verify no new feature code crept in:
                        codex exec "$(Get-Content .codex\tasks\diff-risk-review.md -Raw)"

Step 4  Claude Code     Review Codex diff-risk-review result

Step 5  Codex CLI       Run full regression suite:
                        codex exec "$(Get-Content .codex\tasks\validate-regression.md -Raw)"
```

---

## Workflow 3 — Migration Wave (Donor Repo Import)

Applies to any future wave import from `indie-ninja-adventures` or `shadow_ascent_integrated_package`. Waves 0–5 are complete as of 2026-05-08. See `docs/MIGRATION_MAP.md` for the authoritative wave status table before starting any new import.

```
Step 1  Claude Code     Review the donor class or module to be imported
                        Map it against existing clean-start interfaces
                        Identify what to slice vs skip vs adapt
                        Produce a bounded import spec (what files, what changes)

Step 2  Claude Code     Perform the import with deliberate adaptation
                        Not a bulk paste — a port that respects layer contracts

Step 3  Copilot         Fill bridging boilerplate (adapters, wrappers, delegation)

Step 4  Claude Code     Review final integration
                        Verify no layer contract violations introduced

Step 5  Codex CLI       Wave completeness check:
                        codex exec "$(Get-Content .codex\tasks\migration-completeness-check.md -Raw)"

Step 6  Claude Code     Review Codex wave-check result
                        Confirm MIGRATION_MAP.md is updated before closing the wave

Step 7  Claude Code     Run /review on the wave commit or PR
                        Checks layer boundaries, API changes, and contract violations
                        across all changed files — not just the diff summary
```

---

## Workflow 4 — LibGDX Production Client Development

The shipping client is `DesktopLauncher` + `ShadowAscentGame` (LibGDX, `lwjgl3` backend). `PlaytestClient` (Swing) remains as the regression/QA harness until parity is reached. Both coexist as separate Gradle tasks (`runGame` and `runPlayableClient`). Do not delete PlaytestClient during this migration.

Key boundaries:

- `GameSimulator.drainEvents()` is the canonical event bus — no pub/sub framework.
- Custom AABB physics only — Box2D is out of scope.
- Tiled `.tmx` maps are the authored level geometry format.

```
Step 1  Claude Code     Design the next LibGDX screen, system, or integration piece
                        Reason across ShadowAscentGame, the relevant sim classes,
                        GameSimulator.drainEvents(), and the Tiled map contract
                        Produce a narrow implementation spec

Step 2  Copilot         Implement LibGDX Screen, InputProcessor, SpriteBatch draw calls,
                        and AssetManager wiring from the spec
                        (LibGDX patterns are repetitive — Copilot handles boilerplate well)

Step 3  Claude Code     Review the implementation
                        Check: drainEvents() wiring correct, no Box2D references,
                        no direct SimPlayer/GameSimulator mutation from render thread,
                        no Swing imports in client

Step 4  Codex CLI       LibGDX boundary + regression check:
                        codex exec "$(Get-Content .codex\tasks\libgdx-client-check.md -Raw)"

Step 6  Claude Code     Review results. Note any PlaytestClient parity gap closed
                        by this change and update feature parity tracking.
```

---

## Workflow 5 — Daily Development Loop

```
Morning planning        Claude Code:
                        "What should I work on today given the backlog and current
                        milestone state?"
                        Feed it CURRENT_STATE.md + IMPLEMENTATION_BACKLOG.md

Active coding           Copilot in VSCode (primary in-editor driver)

Stuck on a bug          Claude Code: paste the relevant files + error output
                        (not Copilot — it guesses, Claude Code traces the actual code)

Scripted task needed    Codex CLI in the integrated terminal

Pre-commit check        Codex CLI (optional but recommended):
                        codex exec "$(Get-Content .codex\tasks\diff-risk-review.md -Raw)"

End-of-session review   Claude Code:
                        "Review what I changed today" — pipe in git diff or paste files
                        Or use: codex exec "$(Get-Content .codex\tasks\stale-doc-audit.md -Raw)"
```

---

## CI as Passive Validation

The GitHub Actions workflow (`ci.yml`) runs the full gate on every push:

```
On every PR:    runRegressionTests + runDataContractDiagnostics + runWorldgenDiagnostics
On merge:       + runWorldSimulationDiagnostics
```

Contract validation runs in `FAIL_FAST` mode on CI. This means CI is doing continuous architectural validation without manual effort. Do not disable `FAIL_FAST` on CI without a documented reason in the PR.

---

## Mental Model Summary

| Phase | Tool | Input | Output |
| --- | --- | --- | --- |
| Ideation / design | Claude Code | Design doc, backlog, current state | Spec, decision, plan |
| Architecture review | Claude Code | Source files, layer contracts | Analysis, refactor plan |
| Active coding | Copilot | Partial code in editor | Completion, boilerplate |
| Debugging | Claude Code | Error + relevant files | Root cause + fix |
| Scripted tasks | Codex CLI | `.codex/tasks/` prompt file | Structured Codex Result report |
| Migration planning | Claude Code | Donor class + existing interfaces | Bounded import spec |
| Data contract authoring | Copilot | Partial JSON + schema | Completed entry |
| LibGDX client coding | Copilot + Claude Code | Screen spec + sim contracts | Screen impl + boundary review |
| Regression review | Claude Code | Test file + spec | Logic correctness check |
| Pre-commit validation | Codex CLI | diff-risk-review task file | Layer/scope/contract risk report |
| CI validation | GitHub Actions | Every push | Pass/fail + diagnostic summary |
