You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Parse all playtest session log files under logs/playtest/ and produce a structured
telemetry summary. Include cross-session delta comparisons for key movement metrics.

Steps:
1. List all files under logs/playtest/. Note the count and date range.

2. For each session log file, extract the MOVEMENT_SIGNOFF telemetry block.
   The block contains fields including:
   - session_seconds
   - jumps_ground
   - jumps_wall
   - jumps_double
   - dashes
   - damage_events
   - deaths

3. Produce a summary table with one row per session:
   | Session file | session_seconds | jumps_ground | jumps_wall | jumps_double | dashes | damage_events | deaths |

4. For consecutive sessions, compute deltas for each metric (positive = increase).
   Show deltas in a second table.

5. Identify any session with an unusual spike or drop in any metric (more than 2x or
   less than 0.5x the previous session's value) — these may indicate a bug or a
   significant player behaviour change.

6. Note any session log files that are missing the MOVEMENT_SIGNOFF block entirely.

Do not output raw log file contents.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
None (file inspection only).

## Files Read
- [list of log files found under logs/playtest/]

## Files Changed
None.

## Key Findings
1. [Session count: N, date range: YYYY-MM-DD to YYYY-MM-DD]
2. [Summary table — paste the table here]
3. [Delta table — paste the table here]
4. [Anomalous sessions: list with the metric that spiked/dropped and the delta]
5. [Sessions missing MOVEMENT_SIGNOFF: list if any]

## Risks / Uncertainties
1. [Any log file with unexpected format or incomplete data]
2. [Any metric trend that suggests a regression in feel or difficulty]

## Recommended Next Step
One narrow next action for Claude Code to take.
