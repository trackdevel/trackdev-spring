#!/bin/bash
#
# TrackDev API Test Runner
# This script runs the Postman collection using Newman and generates test reports
#
# Prerequisites:
#   - Node.js installed
#   - Newman installed globally: npm install -g newman
#   - Newman HTML reporter: npm install -g newman-reporter-html
#   - Newman HTML Extra reporter (optional): npm install -g newman-reporter-htmlextra
#
# Usage:
#   ./run-api-tests.sh [options]
#
# Options:
#   -e, --env-file    Path to environment file (JSON)
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

# Default values
COLLECTION_FILE="TrackDev-API-Tests.postman_collection.json"
OUTPUT_DIR="./test-reports"
BASE_URL="http://localhost:8080"
ENV_FILE=""
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
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
            echo "  -e, --env-file    Path to environment file (JSON)"
            echo "  -u, --base-url    Base URL of the API (default: http://localhost:8080)"
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

# Print banner
echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║              TrackDev API Test Runner                        ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Check if Newman is installed
if ! command -v newman &> /dev/null; then
    echo -e "${YELLOW}Newman is not installed. Installing...${NC}"
    npm install -g newman newman-reporter-html newman-reporter-htmlextra
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Check if collection file exists
if [ ! -f "$COLLECTION_FILE" ]; then
    echo -e "${RED}Error: Collection file not found: $COLLECTION_FILE${NC}"
    exit 1
fi

echo -e "${BLUE}Configuration:${NC}"
echo "  Collection: $COLLECTION_FILE"
echo "  Base URL: $BASE_URL"
echo "  Output Directory: $OUTPUT_DIR"
if [ -n "$ENV_FILE" ]; then
    echo "  Environment File: $ENV_FILE"
fi
echo ""

# Build Newman command
NEWMAN_CMD="newman run \"$COLLECTION_FILE\""

# Add environment file if specified
if [ -n "$ENV_FILE" ]; then
    NEWMAN_CMD="$NEWMAN_CMD -e \"$ENV_FILE\""
fi

# Override base URL
NEWMAN_CMD="$NEWMAN_CMD --env-var \"baseUrl=$BASE_URL\""

# Add reporters
NEWMAN_CMD="$NEWMAN_CMD --reporters cli,json,html"
NEWMAN_CMD="$NEWMAN_CMD --reporter-json-export \"$OUTPUT_DIR/test-results-$TIMESTAMP.json\""
NEWMAN_CMD="$NEWMAN_CMD --reporter-html-export \"$OUTPUT_DIR/test-report-$TIMESTAMP.html\""

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

# Run Newman
eval $NEWMAN_CMD

# Capture exit code
TEST_EXIT_CODE=$?

echo ""

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
echo "  JSON: $OUTPUT_DIR/test-results-$TIMESTAMP.json"
echo "  HTML: $OUTPUT_DIR/test-report-$TIMESTAMP.html"
echo ""

# Parse JSON results and print summary
if [ -f "$OUTPUT_DIR/test-results-$TIMESTAMP.json" ]; then
    echo -e "${BLUE}Test Summary:${NC}"
    
    # Use Node.js to parse JSON (more portable than jq)
    node -e "
        const fs = require('fs');
        const data = JSON.parse(fs.readFileSync('$OUTPUT_DIR/test-results-$TIMESTAMP.json'));
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
echo "  file://$PWD/$OUTPUT_DIR/test-report-$TIMESTAMP.html"
echo ""

exit $TEST_EXIT_CODE
