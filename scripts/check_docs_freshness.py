#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
import re
import sys


REPO_ROOT = Path(__file__).resolve().parent.parent
DOCS_DIR = REPO_ROOT / "docs"
REPORT_PATH = DOCS_DIR / "reports" / "docs_freshness_report.md"
VERSION_PATH = REPO_ROOT / "version.json"

SYNC_SET = [
    REPO_ROOT / "README.md",
    DOCS_DIR / "INDEX.md",
    DOCS_DIR / "CURRENT_STATE.md",
    DOCS_DIR / "ROADMAP.md",
    DOCS_DIR / "ACT_I_QA_ROUTE.md",
    DOCS_DIR / "PLAYABLE_TRUTH.md",
]

FRONTMATTER_REQUIRED = [
    DOCS_DIR / "INDEX.md",
    DOCS_DIR / "CURRENT_STATE.md",
    DOCS_DIR / "PLAYABLE_TRUTH.md",
]
FRONTMATTER_REQUIRED.extend(sorted((DOCS_DIR / "workflows").glob("*.md")))

VERSION_ANCHOR_RE = re.compile(r"^version_anchor:\s*(.+?)\s*$", re.MULTILINE)
LAST_UPDATED_RE = re.compile(r"^last_updated:\s*(.+?)\s*$", re.MULTILINE)


@dataclass
class WarningItem:
    code: str
    path: str
    detail: str


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig")


def parse_frontmatter(text: str) -> str | None:
    normalized = text.replace("\r\n", "\n")
    if not normalized.startswith("---\n"):
        return None
    end = normalized.find("\n---\n", 4)
    if end == -1:
        return None
    return normalized[4:end]


def load_version() -> str:
    data = json.loads(read_text(VERSION_PATH))
    return str(data["version"])


def collect_warnings() -> tuple[str, list[WarningItem]]:
    version = load_version()
    warnings: list[WarningItem] = []

    for path in SYNC_SET:
        if not path.exists():
            warnings.append(WarningItem("missing_file", str(path.relative_to(REPO_ROOT)), "required sync-set file is missing"))

    for path in FRONTMATTER_REQUIRED:
        if not path.exists():
            continue
        text = read_text(path)
        frontmatter = parse_frontmatter(text)
        if frontmatter is None:
            warnings.append(WarningItem("missing_frontmatter", str(path.relative_to(REPO_ROOT)), "canonical doc is missing YAML frontmatter"))
            continue

        version_match = VERSION_ANCHOR_RE.search(frontmatter)
        if version_match is None:
            warnings.append(WarningItem("missing_version_anchor", str(path.relative_to(REPO_ROOT)), "frontmatter is missing version_anchor"))
        elif version_match.group(1).strip() != version:
            warnings.append(
                WarningItem(
                    "version_anchor_mismatch",
                    str(path.relative_to(REPO_ROOT)),
                    f"version_anchor={version_match.group(1).strip()} does not match version.json={version}",
                )
            )

        if LAST_UPDATED_RE.search(frontmatter) is None:
            warnings.append(WarningItem("missing_last_updated", str(path.relative_to(REPO_ROOT)), "frontmatter is missing last_updated"))

    index_text = read_text(DOCS_DIR / "INDEX.md") if (DOCS_DIR / "INDEX.md").exists() else ""
    if "docs/workflows/INDEX.md" not in index_text:
        warnings.append(WarningItem("missing_workflow_route", "docs/INDEX.md", "root docs index does not route to docs/workflows/INDEX.md"))
    if "docs/PLAYABLE_TRUTH.md" not in index_text:
        warnings.append(WarningItem("missing_playable_truth_route", "docs/INDEX.md", "root docs index does not route to docs/PLAYABLE_TRUTH.md"))

    playable_truth_text = read_text(DOCS_DIR / "PLAYABLE_TRUTH.md") if (DOCS_DIR / "PLAYABLE_TRUTH.md").exists() else ""
    if "ACT_I_QA_ROUTE.md" not in playable_truth_text:
        warnings.append(WarningItem("missing_qa_route_link", "docs/PLAYABLE_TRUTH.md", "playable truth should link to ACT_I_QA_ROUTE.md"))
    if "CURRENT_STATE.md" not in playable_truth_text:
        warnings.append(WarningItem("missing_current_state_link", "docs/PLAYABLE_TRUTH.md", "playable truth should link to CURRENT_STATE.md"))

    current_state_text = read_text(DOCS_DIR / "CURRENT_STATE.md") if (DOCS_DIR / "CURRENT_STATE.md").exists() else ""
    history_markers = [
        "Session Handover - ",
        "Session Handover — ",
        "Completed Slice - ",
        "Completed Hotfix - ",
        "### What was built",
    ]
    marker_count = sum(current_state_text.count(marker) for marker in history_markers)
    if marker_count > 0:
        warnings.append(
            WarningItem(
                "current_state_history_sprawl",
                "docs/CURRENT_STATE.md",
                "CURRENT_STATE contains historical handover/release-ledger markers; move deep history to docs/handover or docs/reports",
            )
        )

    return version, warnings


def build_report(version: str, warnings: list[WarningItem]) -> str:
    generated = datetime.now(timezone.utc).isoformat()
    status = "PASS" if not warnings else "WARN"
    lines = [
        "# Docs Freshness Report",
        "",
        f"- Generated: {generated}",
        f"- Version anchor target: {version}",
        f"- Documents checked: {len(SYNC_SET)} sync-set files + canonical workflow/frontmatter set",
        f"- Status: {status}",
        "",
    ]
    if warnings:
        lines.append("## Warnings")
        lines.append("")
        for item in warnings:
            lines.append(f"- [{item.code}] {item.detail} ({item.path})")
    else:
        lines.append("## Result")
        lines.append("")
        lines.append("- No freshness or routing warnings found in the clean-start sync set.")
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="Lightweight docs freshness checker for the clean-start repo.")
    parser.add_argument("--emit-report", action="store_true", help="write docs/reports/docs_freshness_report.md")
    args = parser.parse_args()

    version, warnings = collect_warnings()
    report = build_report(version, warnings)
    print(report)

    if args.emit_report:
        REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
        REPORT_PATH.write_text(report, encoding="utf-8")

    return 0 if not warnings else 1


if __name__ == "__main__":
    sys.exit(main())
