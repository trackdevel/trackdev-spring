#!/bin/bash
#
# TrackDev API Test Runner
# This script runs the Postman collection using Newman and generates test reports.
#
# The collection is stored as split files under postman/. This script rebuilds
# the full collection before running, or can run a single section.
#
# Prerequisites:
#   - Python 3 (for rebuilding the collection)
#   - Node.js installed
#   - Newman installed globally: npm install -g newman
#   - Newman HTML reporter: npm install -g newman-reporter-html
#   - Newman HTML Extra reporter (optional): npm install -g newman-reporter-htmlextra
#
# Usage:
#   ./run-api-tests.sh [options]
#
# Options:
#   -s, --section     Run only a specific section (number or glob, e.g. 09 or "09*")
#   -l, --list        List available sections and exit
#   -e, --env-file    Path to environment file (default: postman/environment.json)
#   -u, --base-url    Base URL of the API (default: http://localhost:8080)
#   -o, --output      Output directory for reports (default: ./test-reports)
#   -h, --help        Show this help message
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Resolve script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Default values
POSTMAN_DIR="$PROJECT_DIR/postman"
COLLECTION_FILE="$PROJECT_DIR/TrackDev-API-Tests.postman_collection.json"
OUTPUT_DIR="$PROJECT_DIR/test-reports"
BASE_URL=""
ENV_FILE="$POSTMAN_DIR/environment.json"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
SECTION=""
LIST_SECTIONS=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--section)
            SECTION="$2"
            shift 2
            ;;
        -l|--list)
            LIST_SECTIONS=true
            shift
            ;;
        -e|--env-file)
            ENV_FILE="$2"
            shift 2
            ;;
        -u|--base-url)
            BASE_URL="$2"
            shift 2
            ;;
        -o|--output)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        -h|--help)
            echo "TrackDev API Test Runner"
            echo ""
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  -s, --section     Run a single section by number (e.g. 09, 14)"
            echo "  -l, --list        List available sections and exit"
            echo "  -e, --env-file    Path to environment file (default: postman/environment.json)"
            echo "  -u, --base-url    Base URL override (default: from environment.json)"
            echo "  -o, --output      Output directory for reports (default: ./test-reports)"
            echo "  -h, --help        Show this help message"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# Check that postman directory exists
if [ ! -d "$POSTMAN_DIR" ]; then
    echo -e "${RED}Error: Postman directory not found: $POSTMAN_DIR${NC}"
    exit 1
fi

# List sections and exit
if [ "$LIST_SECTIONS" = true ]; then
    echo -e "${BLUE}Available sections:${NC}"
    for f in "$POSTMAN_DIR"/[0-9][0-9]-*.json; do
        base=$(basename "$f" .json)
        [ "$base" = "00-shared-metadata" ] && continue
        echo "  $base"
    done
    exit 0
fi

# Print banner
echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║              TrackDev API Test Runner                       ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Check if Newman is installed
if ! command -v newman &> /dev/null; then
    echo -e "${YELLOW}Newman is not installed. Installing...${NC}"
    npm install -g newman newman-reporter-html newman-reporter-htmlextra
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Build the collection to run
if [ -n "$SECTION" ]; then
    # Running a single section: build a temporary collection with just that section
    SECTION_FILE=$(ls "$POSTMAN_DIR"/${SECTION}-*.json 2>/dev/null | head -1)
    if [ -z "$SECTION_FILE" ]; then
        # Try zero-padded
        SECTION_FILE=$(ls "$POSTMAN_DIR"/$(printf '%02d' "$SECTION")-*.json 2>/dev/null | head -1)
    fi
    if [ -z "$SECTION_FILE" ] || [ ! -f "$SECTION_FILE" ]; then
        echo -e "${RED}Error: Section not found: $SECTION${NC}"
        echo "Use -l to list available sections."
        exit 1
    fi

    SECTION_NAME=$(basename "$SECTION_FILE" .json)
    COLLECTION_FILE="$OUTPUT_DIR/.tmp-collection-${SECTION_NAME}.json"
    REPORT_LABEL="$SECTION_NAME"

    echo -e "${BLUE}Building collection for section: ${SECTION_NAME}${NC}"

    python3 -c "
import json, sys

with open('$POSTMAN_DIR/00-shared-metadata.json') as f:
    shared = json.load(f)

with open('$SECTION_FILE') as f:
    section = json.load(f)

collection = {
    'info': {**shared['info'], 'name': shared['info']['name'] + ' - ' + section['name']},
    'item': [section],
    'event': shared.get('event', []),
    'variable': shared.get('variable', []),
}

with open('$COLLECTION_FILE', 'w') as f:
    json.dump(collection, f, indent='\t', ensure_ascii=False)
    f.write('\n')
"
else
    # Running all sections: rebuild full collection from split files
    REPORT_LABEL="full"

    echo -e "${BLUE}Rebuilding full collection from postman/ files...${NC}"

    if ! python3 "$POSTMAN_DIR/rebuild-collection.py"; then
        echo -e "${RED}Error: Failed to rebuild collection${NC}"
        exit 1
    fi

    if [ ! -f "$COLLECTION_FILE" ]; then
        echo -e "${RED}Error: Rebuilt collection file not found: $COLLECTION_FILE${NC}"
        exit 1
    fi
fi

echo -e "${BLUE}Configuration:${NC}"
echo "  Collection: $COLLECTION_FILE"
echo "  Environment: $ENV_FILE"
if [ -n "$BASE_URL" ]; then
    echo "  Base URL: $BASE_URL (override)"
else
    echo "  Base URL: (from environment file)"
fi
echo "  Output Directory: $OUTPUT_DIR"
if [ -n "$SECTION" ]; then
    echo "  Section: $SECTION_NAME"
fi
echo ""

# Build Newman command
NEWMAN_CMD="newman run \"$COLLECTION_FILE\""

# Add environment file
if [ -n "$ENV_FILE" ] && [ -f "$ENV_FILE" ]; then
    NEWMAN_CMD="$NEWMAN_CMD -e \"$ENV_FILE\""
fi

# Override base URL only if explicitly provided
if [ -n "$BASE_URL" ]; then
    NEWMAN_CMD="$NEWMAN_CMD --env-var \"baseUrl=$BASE_URL\""
fi

# Add reporters
NEWMAN_CMD="$NEWMAN_CMD --reporters cli,json,html"
NEWMAN_CMD="$NEWMAN_CMD --reporter-json-export \"$OUTPUT_DIR/test-results-${REPORT_LABEL}-$TIMESTAMP.json\""
NEWMAN_CMD="$NEWMAN_CMD --reporter-html-export \"$OUTPUT_DIR/test-report-${REPORT_LABEL}-$TIMESTAMP.html\""

# Try to use htmlextra reporter if available
if npm list -g newman-reporter-htmlextra &> /dev/null; then
    NEWMAN_CMD="${NEWMAN_CMD/--reporters cli,json,html/--reporters cli,json,htmlextra}"
    NEWMAN_CMD="${NEWMAN_CMD/--reporter-html-export/--reporter-htmlextra-export}"
fi

# Add extra options
NEWMAN_CMD="$NEWMAN_CMD --color on"
NEWMAN_CMD="$NEWMAN_CMD --delay-request 100"  # 100ms delay between requests

echo -e "${BLUE}Starting API tests...${NC}"
echo ""

# Run Newman (disable set -e to capture exit code)
set +e
eval $NEWMAN_CMD
TEST_EXIT_CODE=$?
set -e

echo ""

# Clean up temporary collection file for single-section runs
if [ -n "$SECTION" ] && [ -f "$COLLECTION_FILE" ]; then
    rm -f "$COLLECTION_FILE"
fi

# Generate summary
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                    ALL TESTS PASSED!                        ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
else
    echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║                   SOME TESTS FAILED!                        ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
fi

echo ""
echo -e "${BLUE}Reports generated:${NC}"
echo "  JSON: $OUTPUT_DIR/test-results-${REPORT_LABEL}-$TIMESTAMP.json"
echo "  HTML: $OUTPUT_DIR/test-report-${REPORT_LABEL}-$TIMESTAMP.html"
echo ""

# Parse JSON results and print summary
JSON_REPORT="$OUTPUT_DIR/test-results-${REPORT_LABEL}-$TIMESTAMP.json"
if [ -f "$JSON_REPORT" ]; then
    echo -e "${BLUE}Test Summary:${NC}"

    # Use Node.js to parse JSON (more portable than jq)
    node -e "
        const fs = require('fs');
        const data = JSON.parse(fs.readFileSync('$JSON_REPORT'));
        const run = data.run;
        const stats = run.stats;

        console.log('  Total Requests: ' + stats.requests.total);
        console.log('  Failed Requests: ' + stats.requests.failed);
        console.log('  Total Assertions: ' + stats.assertions.total);
        console.log('  Failed Assertions: ' + stats.assertions.failed);
        console.log('  Total Duration: ' + (run.timings.completed / 1000).toFixed(2) + 's');

        if (stats.assertions.failed > 0) {
            console.log('');
            console.log('  Failed Tests:');
            run.executions.forEach(exec => {
                if (exec.assertions) {
                    exec.assertions.forEach(assertion => {
                        if (assertion.error) {
                            console.log('    - ' + exec.item.name + ': ' + assertion.assertion);
                        }
                    });
                }
            });
        }
    " 2>/dev/null || echo "  (Install Node.js to view detailed summary)"
fi

echo ""
echo -e "${BLUE}To view the HTML report, open:${NC}"
echo "  file://$PWD/$OUTPUT_DIR/test-report-${REPORT_LABEL}-$TIMESTAMP.html"
echo ""

exit $TEST_EXIT_CODE
