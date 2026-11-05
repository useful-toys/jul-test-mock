#!/usr/bin/env python3
"""Helpers for the SARIF reports produced by the static-analysis Maven profile.

normalize SARIF...   Rewrites absolute file URIs to repository-relative paths, in place.
                     GitHub Code Scanning only maps alerts to files when the paths are relative.
markdown SARIF...    Prints a markdown summary (counts per tool/level and top rules) to stdout.
coverage JACOCO_CSV  Prints a markdown coverage summary (total and per package) to stdout.
"""
import csv
import json
import os
import sys
from collections import Counter
from pathlib import Path
from urllib.parse import unquote, urlparse

TOP_RULES = 10


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


SOURCE_ROOTS = ("src/main/java", "src/test/java")


def relative_uri(uri, root):
    if not uri.startswith("file:"):
        # Some tools (SpotBugs) report paths relative to a source root, not to the repository.
        for source_root in SOURCE_ROOTS:
            if (root / source_root / uri).is_file():
                return f"{source_root}/{uri}"
        return uri
    local = unquote(urlparse(uri).path)
    if len(local) > 2 and local[0] == "/" and local[2] == ":":  # file:/C:/dir on Windows
        local = local[1:]
    try:
        return Path(local).resolve().relative_to(root).as_posix()
    except ValueError:
        return uri


def normalize(paths):
    root = Path(os.environ.get("GITHUB_WORKSPACE", os.getcwd())).resolve()
    for path in paths:
        sarif = load(path)
        for run in sarif.get("runs", []):
            for result in run.get("results", []):
                for location in result.get("locations", []):
                    artifact = location.get("physicalLocation", {}).get("artifactLocation", {})
                    if "uri" in artifact:
                        artifact["uri"] = relative_uri(artifact["uri"], root)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(sarif, f)


def markdown(paths):
    print("## Static analysis (SARIF)\n")
    print("| Tool | Error | Warning | Note | Total |")
    print("|---|---:|---:|---:|---:|")
    details = []
    for path in paths:
        for run in load(path).get("runs", []):
            driver = run.get("tool", {}).get("driver", {})
            name = driver.get("name", Path(path).stem)
            results = run.get("results", [])
            levels = Counter(r.get("level", "warning") for r in results)
            print(f"| {name} | {levels['error']} | {levels['warning']} | {levels['note']} | {len(results)} |")
            rules = Counter(r.get("ruleId", "?") for r in results).most_common(TOP_RULES)
            if rules:
                rows = "\n".join(f"| `{rule}` | {count} |" for rule, count in rules)
                details.append(f"<details><summary>{name}: top rules</summary>\n\n| Rule | Count |\n|---|---:|\n{rows}\n\n</details>")
    print()
    for block in details:
        print(block + "\n")


def percent(rows, kind):
    """Coverage of one counter (INSTRUCTION, BRANCH, LINE, METHOD) summed over the given CSV rows."""
    covered = sum(int(row[f"{kind}_COVERED"]) for row in rows)
    total = covered + sum(int(row[f"{kind}_MISSED"]) for row in rows)
    return "n/a" if total == 0 else f"{100 * covered / total:.1f}% ({covered}/{total})"


def coverage(paths):
    """Prints a markdown coverage summary from the JaCoCo CSV report."""
    with open(paths[0], newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    print("## Test coverage (JaCoCo)\n")
    print("| Instructions | Branches | Lines | Methods |")
    print("|---:|---:|---:|---:|")
    print("| " + " | ".join(percent(rows, kind) for kind in ("INSTRUCTION", "BRANCH", "LINE", "METHOD")) + " |\n")
    print("<details><summary>Coverage per package</summary>\n")
    print("| Package | Lines | Branches |\n|---|---:|---:|")
    for package in sorted({row["PACKAGE"] for row in rows}):
        selected = [row for row in rows if row["PACKAGE"] == package]
        print(f"| `{package}` | {percent(selected, 'LINE')} | {percent(selected, 'BRANCH')} |")
    print("\n</details>\n")


def main(argv):
    commands = {"normalize": normalize, "markdown": markdown, "coverage": coverage}
    if len(argv) < 3 or argv[1] not in commands:
        sys.exit(__doc__)
    commands[argv[1]](argv[2:])


if __name__ == "__main__":
    main(sys.argv)
