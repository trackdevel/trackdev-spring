#!/usr/bin/env python3
"""
Rebuild the monolithic Postman collection from the split files in postman/.

Reads 00-shared-metadata.json for collection info, events, and variables,
then appends each numbered section file (01-*.json, 02-*.json, ...) as an
item in the collection.  The result is written to the project root as
TrackDev-API-Tests.postman_collection.json.
"""

import json
import glob
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(SCRIPT_DIR)
OUTPUT_FILE = os.path.join(PROJECT_DIR, "TrackDev-API-Tests.postman_collection.json")

# Load shared metadata
shared_path = os.path.join(SCRIPT_DIR, "00-shared-metadata.json")
if not os.path.exists(shared_path):
    print(f"Error: {shared_path} not found", file=sys.stderr)
    sys.exit(1)

with open(shared_path, encoding="utf-8") as f:
    shared = json.load(f)

# Collect section files in numeric order
section_files = sorted(glob.glob(os.path.join(SCRIPT_DIR, "[0-9][0-9]-*.json")))
section_files = [f for f in section_files if not f.endswith("00-shared-metadata.json")]

if not section_files:
    print("Error: no section files found in postman/", file=sys.stderr)
    sys.exit(1)

items = []
for path in section_files:
    with open(path, encoding="utf-8") as f:
        items.append(json.load(f))

collection = {
    "info": shared["info"],
    "item": items,
    "event": shared.get("event", []),
    "variable": shared.get("variable", []),
}

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(collection, f, indent="\t", ensure_ascii=False)
    f.write("\n")

print(f"Rebuilt collection with {len(items)} sections -> {OUTPUT_FILE}")
