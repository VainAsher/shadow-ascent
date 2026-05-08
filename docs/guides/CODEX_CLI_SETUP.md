---
doc_type: developer_guide
status: living
last_updated: 2026-05-07
platform: Windows 11 + VSCode
---
# Codex CLI — Setup and Usage Guide

Setup and operational guide for OpenAI's Codex CLI on Windows 11 using the VSCode integrated terminal. Codex is the terminal-based coding agent used for bounded, scripted tasks in the Shadow Ascent development workflow.

For how Codex fits into the overall tool strategy, see `docs/guides/DEVELOPER_WORKFLOW.md`.

---

## What Codex CLI Is

Codex CLI (`@openai/codex`) is a terminal agent that executes a scoped instruction end-to-end — it can read files, write files, run shell commands, and produce output. Unlike Copilot (which completes what you're typing) or Claude Code (which reasons across your architecture), Codex is for bounded tasks with a verifiable result: validation scripts, log analysis, scaffolding, running build commands and summarising output.

---

## Step 1 — Verify Node.js

Codex requires Node.js 22 or higher. Open the VSCode integrated terminal with `Ctrl+`` and run:

```powershell
node --version
```

If the version is below 22 or the command is not found, install Node.js from [nodejs.org](https://nodejs.org) (download the LTS installer). After installing, **restart VSCode completely** before continuing.

---

## Step 2 — Install Codex CLI

In the VSCode terminal:

```powershell
npm install -g @openai/codex
```

Verify the install:

```powershell
codex --version
```

---

## Step 3 — Set Your OpenAI API Key

Codex requires an OpenAI API key. Get one from your OpenAI account at [platform.openai.com/api-keys](https://platform.openai.com/api-keys).

**Set it permanently** (survives terminal and system restarts):

```powershell
[Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "sk-your-key-here", "User")
```

After running this, **close and reopen VSCode** (or open a new terminal tab). Verify it loaded:

```powershell
$env:OPENAI_API_KEY
```

This should print your key, not be blank.

**Alternative method:** Windows search → "Edit environment variables for your account" → New → Name: `OPENAI_API_KEY`, Value: your key.

---

## Step 4 — Understand Approval Modes

This is the most important concept. Codex can take real actions — edit files, run shell commands — so the mode controls how much it pauses to ask you before acting.

| Mode | Behaviour | When to use |
| --- | --- | --- |
| `suggest` | Shows every proposed change; you approve each one | Default. Use when exploring or touching important files |
| `auto-edit` | Edits files automatically; pauses before running shell commands | **Recommended default for this project** |
| `full-auto` | Does everything without asking | Only for throwaway or isolated tasks |

For Shadow Ascent work, **`auto-edit` is the right default** — it writes code freely but pauses before running `./gradlew` or other commands, so you stay in control of what executes.

---

## Step 5 — Basic Invocation

**Interactive mode** (opens a chat-style interface in the terminal):

```powershell
codex
```

Type your task at the prompt. Codex will propose steps and ask for approval based on your mode.

**One-shot mode** (give the task directly on the command line):

```powershell
codex exec "your task description here"
```

**With an explicit approval mode:**

```powershell
codex --approval-mode auto-edit "scaffold a new file at scripts/validate_json_refs.py"
```

**Switch mode mid-session** (inside the interactive TUI):

```text
/mode auto-edit
/help
/clear
```

---

## Step 6 — Working Directory

Always run Codex from the project root. The integrated terminal in VSCode opens in the folder you have open, so if you opened the `shadow_ascent_clean_start` folder directly (`File → Open Folder`), you're already in the right place.

Verify:

```powershell
pwd
```

Should show `...\shadow_ascent_clean_start`. If not, navigate there:

```powershell
cd "c:\Users\asher\tester\shadow_ascent_integrated_complete_prototype_package\shadow_ascent_clean_start"
```

---

## Step 7 — Shadow Ascent Task Reference

Reusable task prompt files live in `.codex/tasks/`. Each file contains a complete, structured instruction including the Codex Output Contract so results are always in a consistent format for Claude Code to review.

**Invoke a task file:**

```powershell
codex exec "$(Get-Content .codex\tasks\<task-file>.md -Raw)"
```

**Invoke with write permissions (auto-edit mode):**

```powershell
codex --approval-mode auto-edit "$(Get-Content .codex\tasks\<task-file>.md -Raw)"
```

### Available Task Files

| File | What it does | Mode |
| --- | --- | --- |
| `validate-regression.md` | Run regression suite, return section pass/fail summary | read-only |
| `validate-data-contracts.md` | Run data contract diagnostics, return issue summary | read-only |
| `validate-full-gate.md` | Run the full CI gate step-by-step, return each step result | read-only |
| `validate-worldgen.md` | Run worldgen + world simulation diagnostics | read-only |
| `shadow-ascent-wave-check.md` | Full wave health check: git state, compile, regression, layer violations, stale docs | read-only |
| `cross-reference-flags.md` | Cross-reference all story flag keys across all data contracts | read-only |
| `stale-doc-audit.md` | Audit docs/ for staleness against git history and current code | read-only |
| `migration-completeness-check.md` | Verify a wave import is complete: compile, regression, map accuracy | read-only |
| `plateau-contract-check.md` | Verify a new plateau entry is consistent across all five core contracts | read-only |
| `playtest-log-summary.md` | Parse playtest logs, produce telemetry table with deltas | read-only |
| `diff-risk-review.md` | Review uncommitted diff for layer violations, hardcoded values, scope creep | read-only |
| `libgdx-client-check.md` | Validate a LibGDX client change: Box2D/Swing guards, drainEvents() wiring, regression | read-only |

### One-Off Tasks (not in task files)

For tasks not covered by the task files above, use the inline templates from `docs/guides/DEVELOPER_WORKFLOW.md`.

### Scaffold the scripts directory

```powershell
codex --approval-mode auto-edit "create a scripts/ directory in the project root. Inside it, create validate_json_refs.py — a standalone Python script that cross-references required_flags entries across all JSON files in data/ against the keys defined in data/story_flags.json. Print any unresolved references with their source file and JSON path. The script should run with no arguments from the project root."
```

### Scaffold a new Gradle submodule (for M6)

```powershell
codex --approval-mode auto-edit "scaffold a new Gradle subproject called 'streaming' under java/streaming/. Create the directory structure, a build.gradle.kts that mirrors the core module pattern (same Java version, same dependency setup), and a stub package at src/main/java/shadowascent/streaming/ with a placeholder StreamingModule.java class."
```

### Gitignore hygiene check

```powershell
codex exec "check whether any files under bin/ or build/ are currently tracked by git. List them if found, and suggest the .gitignore additions needed to exclude them."
```

### Generate save migration utility script

```powershell
codex --approval-mode auto-edit "write a utility script at scripts/migrate_save_v2_to_v3.py that reads a SAVE_V2 save file from a path given as an argument and prints the decoded story state payload. Structure it as a starting point for future v3 migration work."
```

---

## Step 8 — Optional: Config File

To set default approval mode and model so you don't type flags every time:

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.codex"
```

Create `C:\Users\asher\.codex\config.toml`:

```toml
model = "o4-mini"
approval_mode = "auto-edit"
```

Command-line flags always override the config file.

---

## Quick Reference

```text
Install:         npm install -g @openai/codex
Verify:          codex --version
Set API key:     [Environment]::SetEnvironmentVariable("OPENAI_API_KEY","sk-...","User")
Check key:       $env:OPENAI_API_KEY

Interactive:     codex
One-shot:        codex exec "task description"
With mode:       codex --approval-mode auto-edit "task"

In-session:      /mode suggest
                 /mode auto-edit
                 /mode full-auto
                 /help
                 /clear

Default for this project: --approval-mode auto-edit
```
