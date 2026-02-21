#!/usr/bin/env python3
"""Generate coverage reports (JSON/MD/PDF) from JaCoCo CSV files."""

from __future__ import annotations

import csv
import json
import os
import pathlib
import argparse
from dataclasses import dataclass
from datetime import datetime, timezone

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


@dataclass
class CoverageRow:
    package: str
    class_name: str
    instruction_missed: int
    instruction_covered: int
    branch_missed: int
    branch_covered: int

    @property
    def instruction_total(self) -> int:
        return self.instruction_missed + self.instruction_covered

    @property
    def instruction_ratio(self) -> float:
        total = self.instruction_total
        return 0.0 if total == 0 else self.instruction_covered / total

    @property
    def fqcn(self) -> str:
        return f"{self.package}.{self.class_name}"


def _read_csv(path: pathlib.Path) -> list[CoverageRow]:
    if not path.exists():
        return []

    rows: list[CoverageRow] = []
    with path.open(encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        for item in reader:
            rows.append(
                CoverageRow(
                    package=item["PACKAGE"],
                    class_name=item["CLASS"],
                    instruction_missed=int(item["INSTRUCTION_MISSED"]),
                    instruction_covered=int(item["INSTRUCTION_COVERED"]),
                    branch_missed=int(item["BRANCH_MISSED"]),
                    branch_covered=int(item["BRANCH_COVERED"]),
                )
            )
    return rows


def _coverage_payload(csv_paths: list[pathlib.Path]) -> dict:
    all_rows: list[CoverageRow] = []
    for csv_path in csv_paths:
        all_rows.extend(_read_csv(csv_path))

    instruction_missed = sum(row.instruction_missed for row in all_rows)
    instruction_covered = sum(row.instruction_covered for row in all_rows)
    instruction_total = instruction_missed + instruction_covered
    instruction_pct = 0.0 if instruction_total == 0 else round((instruction_covered / instruction_total) * 100.0, 2)

    branch_missed = sum(row.branch_missed for row in all_rows)
    branch_covered = sum(row.branch_covered for row in all_rows)
    branch_total = branch_missed + branch_covered
    branch_pct = 0.0 if branch_total == 0 else round((branch_covered / branch_total) * 100.0, 2)

    weakest = sorted(
        [row for row in all_rows if row.instruction_total >= 50],
        key=lambda row: (row.instruction_ratio, -row.instruction_total, row.fqcn),
    )[:120]

    package_totals: dict[str, dict[str, int]] = {}
    for row in all_rows:
        bucket = package_totals.setdefault(row.package, {"missed": 0, "covered": 0})
        bucket["missed"] += row.instruction_missed
        bucket["covered"] += row.instruction_covered

    package_summary = []
    for package_name, values in package_totals.items():
        total = values["missed"] + values["covered"]
        ratio = 0.0 if total == 0 else round((values["covered"] / total) * 100.0, 2)
        package_summary.append(
            {
                "package": package_name,
                "instructionCoveragePercent": ratio,
                "instructionCovered": values["covered"],
                "instructionMissed": values["missed"],
            }
        )

    package_summary.sort(key=lambda item: item["instructionCoveragePercent"])

    return {
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "sourceCsvFiles": [str(path) for path in csv_paths],
        "classCount": len(all_rows),
        "instructionCoveragePercent": instruction_pct,
        "instructionCovered": instruction_covered,
        "instructionMissed": instruction_missed,
        "branchCoveragePercent": branch_pct,
        "branchCovered": branch_covered,
        "branchMissed": branch_missed,
        "weakestClasses": [
            {
                "className": row.fqcn,
                "instructionCoveragePercent": round(row.instruction_ratio * 100.0, 2),
                "instructionCovered": row.instruction_covered,
                "instructionMissed": row.instruction_missed,
            }
            for row in weakest
        ],
        "weakestPackages": package_summary[:80],
    }


def _write_markdown(payload: dict, output_path: pathlib.Path) -> None:
    lines: list[str] = []
    lines.append("# Coverage Report")
    lines.append("")
    lines.append(f"- Generated (UTC): `{payload['generatedAtUtc']}`")
    lines.append(f"- Classes analyzed: `{payload['classCount']}`")
    lines.append(f"- Instruction coverage: `{payload['instructionCoveragePercent']}%`")
    lines.append(f"- Branch coverage: `{payload['branchCoveragePercent']}%`")
    lines.append("")
    lines.append("## Weakest Packages")
    lines.append("")
    lines.append("| Package | Instruction Coverage % | Covered | Missed |")
    lines.append("|---|---:|---:|---:|")
    for item in payload.get("weakestPackages", [])[:40]:
        lines.append(
            f"| `{item['package']}` | {item['instructionCoveragePercent']} | "
            f"{item['instructionCovered']} | {item['instructionMissed']} |"
        )
    lines.append("")
    lines.append("## Weakest Classes")
    lines.append("")
    lines.append("| Class | Instruction Coverage % | Covered | Missed |")
    lines.append("|---|---:|---:|---:|")
    for item in payload.get("weakestClasses", [])[:80]:
        lines.append(
            f"| `{item['className']}` | {item['instructionCoveragePercent']} | "
            f"{item['instructionCovered']} | {item['instructionMissed']} |"
        )
    output_path.write_text("\n".join(lines), encoding="utf-8")


def _write_pdf(payload: dict, output_path: pathlib.Path) -> None:
    styles = getSampleStyleSheet()
    story = []

    story.append(Paragraph("Coverage Report", styles["Title"]))
    story.append(Spacer(1, 4 * mm))
    story.append(Paragraph(f"Generated (UTC): {payload['generatedAtUtc']}", styles["Normal"]))
    story.append(Paragraph(f"Classes analyzed: {payload['classCount']}", styles["Normal"]))
    story.append(
        Paragraph(
            f"Instruction coverage: {payload['instructionCoveragePercent']}% "
            f"(covered {payload['instructionCovered']}, missed {payload['instructionMissed']})",
            styles["Normal"],
        )
    )
    story.append(
        Paragraph(
            f"Branch coverage: {payload['branchCoveragePercent']}% "
            f"(covered {payload['branchCovered']}, missed {payload['branchMissed']})",
            styles["Normal"],
        )
    )
    story.append(Spacer(1, 4 * mm))

    package_rows = [["Package", "Coverage %", "Covered", "Missed"]]
    for item in payload.get("weakestPackages", [])[:20]:
        package_rows.append(
            [
                item["package"],
                str(item["instructionCoveragePercent"]),
                str(item["instructionCovered"]),
                str(item["instructionMissed"]),
            ]
        )

    package_table = Table(package_rows, colWidths=[95 * mm, 25 * mm, 30 * mm, 30 * mm])
    package_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.lightgrey),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.grey),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
            ]
        )
    )
    story.append(Paragraph("Weakest Packages", styles["Heading3"]))
    story.append(package_table)
    story.append(Spacer(1, 4 * mm))

    story.append(Paragraph("Weakest Classes", styles["Heading3"]))
    for item in payload.get("weakestClasses", [])[:50]:
        text = (
            f"{item['instructionCoveragePercent']}% - {item['className']} "
            f"(covered {item['instructionCovered']}, missed {item['instructionMissed']})"
        )
        story.append(Paragraph(text, styles["BodyText"]))
        story.append(Spacer(1, 1.2 * mm))

    document = SimpleDocTemplate(str(output_path), pagesize=A4)
    document.build(story)


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate coverage quality artifacts from JaCoCo CSV files.")
    parser.add_argument(
        "output_dir",
        nargs="?",
        default=None,
        help="Output directory for generated report files.",
    )
    parser.add_argument(
        "--output-dir",
        dest="output_dir_flag",
        default=None,
        help="Output directory for generated report files.",
    )
    args = parser.parse_args()

    raw_paths = os.getenv(
        "JACOCO_CSV_PATHS",
        "target/site/jacoco/jacoco.csv,target/site/jacoco-it/jacoco.csv",
    )
    csv_paths = [pathlib.Path(path.strip()) for path in raw_paths.split(",") if path.strip()]

    output_dir_arg = args.output_dir_flag or args.output_dir or "target/quality-reports/coverage"
    output_dir = pathlib.Path(output_dir_arg)
    output_dir.mkdir(parents=True, exist_ok=True)

    payload = _coverage_payload(csv_paths)

    json_path = output_dir / "coverage-report.json"
    md_path = output_dir / "coverage-report.md"
    pdf_path = output_dir / "coverage-report.pdf"

    json_path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    _write_markdown(payload, md_path)
    _write_pdf(payload, pdf_path)

    print(f"Generated coverage report artifacts in {output_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
