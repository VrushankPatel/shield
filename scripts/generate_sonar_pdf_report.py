#!/usr/bin/env python3
"""Generate SonarCloud quality reports (JSON/MD/PDF) for CI artifacts."""

from __future__ import annotations

import base64
import json
import os
import pathlib
import argparse
import urllib.parse
import urllib.request
from collections import Counter
from datetime import datetime, timezone

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


def _api_get(host: str, path: str, params: dict[str, str], token: str | None) -> dict:
    query = urllib.parse.urlencode(params)
    url = f"{host}{path}?{query}"
    request = urllib.request.Request(url)
    if token:
        encoded = base64.b64encode(f"{token}:".encode("utf-8")).decode("ascii")
        request.add_header("Authorization", f"Basic {encoded}")

    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            payload = response.read().decode("utf-8")
            return json.loads(payload)
    except Exception as exc:  # noqa: BLE001
        return {"error": str(exc), "url": url}


def _collect_issues(host: str, project_key: str, token: str | None, max_issues: int) -> list[dict]:
    issues: list[dict] = []
    page = 1
    page_size = 100

    while len(issues) < max_issues:
        response = _api_get(
            host,
            "/api/issues/search",
            {
                "componentKeys": project_key,
                "resolved": "false",
                "p": str(page),
                "ps": str(page_size),
                "additionalFields": "_all",
            },
            token,
        )

        if "error" in response:
            break

        page_items = response.get("issues", [])
        if not page_items:
            break

        issues.extend(page_items)
        total = response.get("total", len(issues))
        if len(issues) >= total:
            break
        page += 1

    return issues[:max_issues]


def _sonar_payload(host: str, project_key: str, token: str | None, max_issues: int) -> dict:
    metrics = _api_get(
        host,
        "/api/measures/component",
        {
            "component": project_key,
            "metricKeys": ",".join(
                [
                    "alert_status",
                    "coverage",
                    "bugs",
                    "vulnerabilities",
                    "code_smells",
                    "duplicated_lines_density",
                    "reliability_rating",
                    "security_rating",
                    "sqale_rating",
                ]
            ),
        },
        token,
    )
    quality_gate = _api_get(
        host,
        "/api/qualitygates/project_status",
        {"projectKey": project_key},
        token,
    )
    issues = _collect_issues(host, project_key, token, max_issues)

    severity = Counter(issue.get("severity", "UNKNOWN") for issue in issues)
    issue_type = Counter(issue.get("type", "UNKNOWN") for issue in issues)

    measures = {}
    for entry in metrics.get("component", {}).get("measures", []):
        measures[entry["metric"]] = entry.get("value")

    return {
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "projectKey": project_key,
        "sonarHost": host,
        "qualityGateStatus": quality_gate.get("projectStatus", {}).get("status", "UNKNOWN"),
        "qualityGateConditions": quality_gate.get("projectStatus", {}).get("conditions", []),
        "measures": measures,
        "issueCount": len(issues),
        "severityCounts": dict(severity),
        "typeCounts": dict(issue_type),
        "issues": issues,
        "apiErrors": {
            "metrics": metrics.get("error"),
            "qualityGate": quality_gate.get("error"),
        },
    }


def _write_markdown(payload: dict, output_path: pathlib.Path) -> None:
    lines: list[str] = []
    lines.append("# SonarCloud Quality Report")
    lines.append("")
    lines.append(f"- Generated (UTC): `{payload['generatedAtUtc']}`")
    lines.append(f"- Project Key: `{payload['projectKey']}`")
    lines.append(f"- Quality Gate: `{payload['qualityGateStatus']}`")
    lines.append(f"- Open Issues Captured: `{payload['issueCount']}`")
    lines.append("")
    lines.append("## Measures")
    lines.append("")
    lines.append("| Metric | Value |")
    lines.append("|---|---|")
    for key, value in sorted(payload.get("measures", {}).items()):
        lines.append(f"| `{key}` | `{value}` |")
    lines.append("")
    lines.append("## Severity Counts")
    lines.append("")
    lines.append("| Severity | Count |")
    lines.append("|---|---|")
    for key, value in sorted(payload.get("severityCounts", {}).items()):
        lines.append(f"| `{key}` | `{value}` |")
    lines.append("")
    lines.append("## Issue Type Counts")
    lines.append("")
    lines.append("| Type | Count |")
    lines.append("|---|---|")
    for key, value in sorted(payload.get("typeCounts", {}).items()):
        lines.append(f"| `{key}` | `{value}` |")
    lines.append("")
    lines.append("## Top Issues")
    lines.append("")
    lines.append("| Severity | Type | Rule | File | Message |")
    lines.append("|---|---|---|---|---|")
    for issue in payload.get("issues", [])[:100]:
        component = issue.get("component", "")
        component = component.split(":")[-1] if ":" in component else component
        message = issue.get("message", "").replace("\n", " ").replace("|", "\\|")
        lines.append(
            f"| `{issue.get('severity')}` | `{issue.get('type')}` | `{issue.get('rule')}` | "
            f"`{component}` | {message} |"
        )
    output_path.write_text("\n".join(lines), encoding="utf-8")


def _write_pdf(payload: dict, output_path: pathlib.Path) -> None:
    styles = getSampleStyleSheet()
    story = []

    story.append(Paragraph("SonarCloud Quality Report", styles["Title"]))
    story.append(Spacer(1, 4 * mm))
    story.append(Paragraph(f"Generated (UTC): {payload['generatedAtUtc']}", styles["Normal"]))
    story.append(Paragraph(f"Project: {payload['projectKey']}", styles["Normal"]))
    story.append(Paragraph(f"Quality Gate: {payload['qualityGateStatus']}", styles["Normal"]))
    story.append(Paragraph(f"Open Issues Captured: {payload['issueCount']}", styles["Normal"]))
    story.append(Spacer(1, 4 * mm))

    metric_rows = [["Metric", "Value"]]
    for key, value in sorted(payload.get("measures", {}).items()):
        metric_rows.append([key, str(value)])
    metric_table = Table(metric_rows, colWidths=[80 * mm, 80 * mm])
    metric_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.lightgrey),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.grey),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
            ]
        )
    )
    story.append(metric_table)
    story.append(Spacer(1, 4 * mm))

    severity_rows = [["Severity", "Count"]]
    for key, value in sorted(payload.get("severityCounts", {}).items()):
        severity_rows.append([key, str(value)])
    severity_table = Table(severity_rows, colWidths=[80 * mm, 80 * mm])
    severity_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.lightgrey),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.grey),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
            ]
        )
    )
    story.append(severity_table)
    story.append(Spacer(1, 4 * mm))

    story.append(Paragraph("Top Issues", styles["Heading3"]))
    for issue in payload.get("issues", [])[:60]:
        component = issue.get("component", "")
        component = component.split(":")[-1] if ":" in component else component
        message = issue.get("message", "").replace("\n", " ")
        text = (
            f"[{issue.get('severity')}] {issue.get('type')} {issue.get('rule')} "
            f"({component}) - {message}"
        )
        story.append(Paragraph(text, styles["BodyText"]))
        story.append(Spacer(1, 1.5 * mm))

    document = SimpleDocTemplate(str(output_path), pagesize=A4)
    document.build(story)


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate SonarCloud quality report artifacts.")
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

    host = os.getenv("SONAR_HOST_URL", "https://sonarcloud.io").rstrip("/")
    project_key = os.getenv("SONAR_PROJECT_KEY", "VrushankPatel_shield")
    token = os.getenv("SONAR_TOKEN")
    max_issues = int(os.getenv("SONAR_REPORT_MAX_ISSUES", "500"))

    output_dir_arg = args.output_dir_flag or args.output_dir or "target/quality-reports/sonar"
    output_dir = pathlib.Path(output_dir_arg)
    output_dir.mkdir(parents=True, exist_ok=True)

    payload = _sonar_payload(host, project_key, token, max_issues)

    json_path = output_dir / "sonar-report.json"
    md_path = output_dir / "sonar-report.md"
    pdf_path = output_dir / "sonar-report.pdf"

    json_path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    _write_markdown(payload, md_path)
    _write_pdf(payload, pdf_path)

    print(f"Generated Sonar report artifacts in {output_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
