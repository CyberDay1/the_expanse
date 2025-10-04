#!/usr/bin/env python3
"""Validate VerticalAnchor configuration for carver y fields.

This script checks that each configured carver in the datapack uses
VerticalAnchor syntax for its `y` field. We ensure that:

* `type` is not present inside the `y` field.
* `min_inclusive` and `max_inclusive` keys are present.
* Each anchor object contains a supported key (`absolute`, `below_top`, or
  `above_bottom`).

Running this guard helps prevent regression to the incorrect FloatProvider
format that breaks loading on newer NeoForge builds.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

# Directory containing the carver configuration JSON files.
CARVER_DIR = Path("src/main/resources/data/the_expanse/worldgen/configured_carver")

SUPPORTED_ANCHOR_KEYS = {"absolute", "below_top", "above_bottom"}


def validate_anchor(anchor: object, *, location: str) -> list[str]:
    """Validate a single VerticalAnchor dictionary."""
    errors: list[str] = []
    if not isinstance(anchor, dict):
        errors.append(f"{location} is not an object")
        return errors

    if not any(key in anchor for key in SUPPORTED_ANCHOR_KEYS):
        keys = ", ".join(sorted(SUPPORTED_ANCHOR_KEYS))
        errors.append(f"{location} must contain one of: {keys}")
    return errors


def validate_carver(path: Path) -> list[str]:
    """Validate the y field inside a configured carver file."""
    errors: list[str] = []
    try:
        data = json.loads(path.read_text())
    except json.JSONDecodeError as exc:  # pragma: no cover - developer aid
        return [f"{path}: invalid JSON - {exc}"]

    config = data.get("config")
    if not isinstance(config, dict):
        return [f"{path}: missing config object"]

    y_field = config.get("y")
    if not isinstance(y_field, dict):
        return [f"{path}: missing y object"]

    if "type" in y_field:
        errors.append(f"{path}: y field must not declare a type (VerticalAnchor)")

    for bound in ("min_inclusive", "max_inclusive"):
        if bound not in y_field:
            errors.append(f"{path}: y field missing {bound}")
        else:
            errors.extend(
                validate_anchor(y_field[bound], location=f"{path} -> y.{bound}")
            )

    return errors


def main() -> int:
    if not CARVER_DIR.exists():
        print(f"Configured carver directory not found: {CARVER_DIR}", file=sys.stderr)
        return 1

    errors: list[str] = []
    for path in sorted(CARVER_DIR.glob("*.json")):
        errors.extend(validate_carver(path))

    if errors:
        print("Configured carver VerticalAnchor validation failed:")
        for error in errors:
            print(f" - {error}")
        return 1

    print("All configured carver y fields use VerticalAnchor syntax correctly.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
