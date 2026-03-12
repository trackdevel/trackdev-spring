#!/usr/bin/env bash
# ============================================================================
# SSE Stress Test (zero dependencies — uses only curl and bash)
#
# Uses stress-test users seeded by DemoDataSeeder (STRESS_TEST_ENABLED=true).
# Each simulated user logs in as stress{N}@trackdev.com with shared password.
#
# Usage:
#   ./sse-stress-test.sh                           # defaults: 10 SSE + REST checks
#   ./sse-stress-test.sh -u http://localhost:8080/api  # local dev server
#   ./sse-stress-test.sh -n 80 -d 120             # 80 SSE users, 120s duration
#   ./sse-stress-test.sh -h                        # show help
# ============================================================================

set -euo pipefail

# ── Defaults ─────────────────────────────────────────────────────────────────
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
PASSWORD="${PASSWORD:-stress}"
SPRINT_ID="${SPRINT_ID:-}"
NUM_SSE="${NUM_SSE:-10}"
DURATION="${DURATION:-60}"
REST_INTERVAL="${REST_INTERVAL:-2}"

# ── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

# ── Parse args ───────────────────────────────────────────────────────────────
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Uses stress-test users: stress1@trackdev.com .. stressN@trackdev.com"
    echo "Start backend with: STRESS_TEST_ENABLED=true SSE_ENABLED=true"
    echo ""
    echo "Options:"
    echo "  -u URL      Base URL (default: $BASE_URL)"
    echo "  -p PASS     Shared password (default: $PASSWORD)"
    echo "  -s ID       Sprint ID (auto-discovered if empty)"
    echo "  -n NUM      Number of SSE connections / users (default: $NUM_SSE)"
    echo "  -d SECS     Duration in seconds (default: $DURATION)"
    echo "  -i SECS     REST check interval (default: $REST_INTERVAL)"
    echo "  -h          Show this help"
    exit 0
}

while getopts "u:p:s:n:d:i:h" opt; do
    case $opt in
        u) BASE_URL="$OPTARG" ;;
        p) PASSWORD="$OPTARG" ;;
        s) SPRINT_ID="$OPTARG" ;;
        n) NUM_SSE="$OPTARG" ;;
        d) DURATION="$OPTARG" ;;
        i) REST_INTERVAL="$OPTARG" ;;
        h) usage ;;
        *) usage ;;
    esac
done

# ── Temp directory for tracking results ──────────────────────────────────────
TMPDIR=$(mktemp -d /tmp/sse-stress-XXXXXX)

# ── Results (defined early so cleanup can call it) ──────────────────────────
print_results() {
    echo ""
    echo -e "${BOLD}══════════════════════════════════════════════════${NC}"
    echo -e "${BOLD}  RESULTS${NC}"
    echo -e "${BOLD}══════════════════════════════════════════════════${NC}"

    local SSE_OK=0 SSE_REJ=0 SSE_DIS=0 SSE_ERR=0
    for f in "$TMPDIR"/sse-*.status; do
        [ -f "$f" ] || continue
        case "$(cat "$f")" in
            connected) SSE_OK=$((SSE_OK + 1)) ;;
            rejected)  SSE_REJ=$((SSE_REJ + 1)) ;;
            disabled)  SSE_DIS=$((SSE_DIS + 1)) ;;
            *)         SSE_ERR=$((SSE_ERR + 1)) ;;
        esac
    done

    echo -e "\n${BOLD}SSE Connections:${NC}"
    echo -e "  Connected:  ${GREEN}$SSE_OK${NC}"
    echo -e "  Rejected:   ${YELLOW}$SSE_REJ${NC} (connection limits working)"
    echo -e "  Disabled:   ${BLUE}$SSE_DIS${NC} (kill switch active)"
    echo -e "  Errors:     ${RED}$SSE_ERR${NC}"

    echo -e "\n${BOLD}REST API (under SSE load):${NC}"
    if [ "${REST_COUNT:-0}" -gt 0 ]; then
        local REST_AVG=$((${REST_TOTAL_MS:-0} / REST_COUNT))
        echo -e "  Baseline avg:   ${BASELINE_AVG:-0}ms (before SSE)"
        echo -e "  Under-load avg: ${REST_AVG}ms"
        echo -e "  Max latency:    ${REST_MAX_MS:-0}ms"
        echo -e "  Total checks:   ${REST_COUNT}"
        echo -e "  OK:             ${GREEN}${REST_PASS:-0}${NC}  Failed: ${RED}${REST_FAIL:-0}${NC}"
        echo -e "  > 500ms:        ${YELLOW}${REST_OVER_500:-0}${NC}"
        echo -e "  > 2000ms:       ${RED}${REST_OVER_2000:-0}${NC}"

        if [ "${BASELINE_AVG:-0}" -gt 0 ]; then
            local DEGRADATION=$((REST_AVG * 100 / BASELINE_AVG))
            echo -e "\n  Degradation:    ${DEGRADATION}% of baseline"
            if [ "$DEGRADATION" -le 200 ]; then
                echo -e "  ${GREEN}✓ REST API is healthy under SSE load${NC}"
            elif [ "$DEGRADATION" -le 500 ]; then
                echo -e "  ${YELLOW}⚠ REST API shows some degradation${NC}"
            else
                echo -e "  ${RED}✗ REST API is severely degraded by SSE!${NC}"
            fi
        fi
    else
        echo -e "  ${YELLOW}No REST checks completed${NC}"
    fi

    echo -e "\n${BOLD}══════════════════════════════════════════════════${NC}"
}

trap 'cleanup' EXIT

cleanup() {
    echo -e "\n${BLUE}Cleaning up...${NC}"
    jobs -p 2>/dev/null | xargs -r kill 2>/dev/null || true
    wait 2>/dev/null || true
    print_results
    rm -rf "$TMPDIR"
}

# ── Step 1: Authenticate all users ──────────────────────────────────────────
echo -e "${BOLD}══════════════════════════════════════════════════${NC}"
echo -e "${BOLD}  TrackDev SSE Stress Test${NC}"
echo -e "${BOLD}══════════════════════════════════════════════════${NC}"
echo -e "  Server:     ${BLUE}$BASE_URL${NC}"
echo -e "  SSE conns:  ${BLUE}$NUM_SSE${NC}"
echo -e "  Duration:   ${BLUE}${DURATION}s${NC}"
echo -e "  Password:   ${BLUE}$PASSWORD${NC}"
echo -e "${BOLD}──────────────────────────────────────────────────${NC}"

echo -e "\nAuthenticating $NUM_SSE stress-test users..."
declare -a TOKENS
AUTH_OK=0
AUTH_FAIL=0

for i in $(seq 1 "$NUM_SSE"); do
    EMAIL="stress${i}@trackdev.com"
    LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" 2>/dev/null)

    HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -1)
    BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" = "200" ]; then
        TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$TOKEN" ]; then
            TOKENS+=("$TOKEN")
            AUTH_OK=$((AUTH_OK + 1))
        else
            AUTH_FAIL=$((AUTH_FAIL + 1))
        fi
    else
        AUTH_FAIL=$((AUTH_FAIL + 1))
    fi

    if (( i % 10 == 0 )); then
        echo -e "  Authenticated $i / $NUM_SSE users"
    fi
done

echo -e "  ${GREEN}OK: $AUTH_OK${NC}  ${RED}Failed: $AUTH_FAIL${NC}"

if [ "$AUTH_OK" -eq 0 ]; then
    echo -e "${RED}No users could authenticate. Is STRESS_TEST_ENABLED=true?${NC}"
    exit 1
fi

FIRST_TOKEN="${TOKENS[0]}"

# ── Auto-discover sprint ID if not provided ─────────────────────────────────
if [ -z "$SPRINT_ID" ]; then
    echo -ne "Auto-discovering active sprint... "
    # Get user's projects first
    PROJECTS_RESPONSE=$(curl -s -H "Authorization: Bearer $FIRST_TOKEN" \
        "$BASE_URL/projects" 2>/dev/null || true)
    PROJECT_ID=$(echo "$PROJECTS_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2 || true)
    if [ -n "$PROJECT_ID" ]; then
        # Get sprints for that project and find an ACTIVE one
        SPRINTS_RESPONSE=$(curl -s -H "Authorization: Bearer $FIRST_TOKEN" \
            "$BASE_URL/projects/$PROJECT_ID/sprints" 2>/dev/null || true)
        SPRINT_ID=$(echo "$SPRINTS_RESPONSE" | grep -o '"id":[0-9]*,"value":"[^"]*","label":"[^"]*","startDate":"[^"]*","endDate":"[^"]*","status":"ACTIVE"' \
            | head -1 | grep -o '"id":[0-9]*' | cut -d: -f2 || true)
    fi
    if [ -n "$SPRINT_ID" ]; then
        echo -e "${GREEN}found sprint $SPRINT_ID (project $PROJECT_ID)${NC}"
    else
        echo -e "${YELLOW}none found, SSE tests will be skipped${NC}"
    fi
fi

# ── Step 2: Baseline REST latency ───────────────────────────────────────────
echo -ne "Measuring baseline REST latency... "
BASELINE_TOTAL=0
BASELINE_COUNT=5
for i in $(seq 1 $BASELINE_COUNT); do
    T=$(curl -s -o /dev/null -w "%{time_total}" \
        -H "Authorization: Bearer $FIRST_TOKEN" \
        "$BASE_URL/auth/self" 2>/dev/null)
    MS=$(echo "$T * 1000" | bc | cut -d. -f1)
    BASELINE_TOTAL=$((BASELINE_TOTAL + MS))
done
BASELINE_AVG=$((BASELINE_TOTAL / BASELINE_COUNT))
echo -e "${GREEN}${BASELINE_AVG}ms avg${NC}"

# ── Step 3: Open N SSE connections (each with its own user token) ───────────
if [ -n "$SPRINT_ID" ]; then
    echo -e "\n${BOLD}Opening $AUTH_OK SSE connections (each as a different user)...${NC}"

    for i in $(seq 0 $((AUTH_OK - 1))); do
        TOKEN="${TOKENS[$i]}"
        USER_NUM=$((i + 1))
        (
            RESPONSE=$(curl -s -m "$DURATION" --no-buffer \
                -H "Authorization: Bearer $TOKEN" \
                -H "Accept: text/event-stream" \
                "$BASE_URL/sprints/$SPRINT_ID/events" 2>/dev/null || true)

            if echo "$RESPONSE" | grep -q "event:connected\|event: connected"; then
                echo "connected" > "$TMPDIR/sse-$USER_NUM.status"
            elif echo "$RESPONSE" | grep -q "event:disabled\|event: disabled"; then
                echo "disabled" > "$TMPDIR/sse-$USER_NUM.status"
            elif echo "$RESPONSE" | grep -q "event:rejected\|event: rejected"; then
                echo "rejected" > "$TMPDIR/sse-$USER_NUM.status"
            else
                echo "error" > "$TMPDIR/sse-$USER_NUM.status"
            fi
        ) &

        if (( (i + 1) % 10 == 0 )); then
            echo -e "  Opened $((i + 1)) / $AUTH_OK connections"
            sleep 0.5
        fi
    done
    echo -e "  ${GREEN}All $AUTH_OK SSE connections launched${NC}"
    sleep 2
else
    echo -e "\n${YELLOW}Skipping SSE connections (no sprint ID)${NC}"
fi

# ── Step 4: REST latency monitoring under load ──────────────────────────────
echo -e "\n${BOLD}Monitoring REST API latency under SSE load (${DURATION}s)...${NC}"

REST_COUNT=0
REST_PASS=0
REST_FAIL=0
REST_TOTAL_MS=0
REST_MAX_MS=0
REST_OVER_500=0
REST_OVER_2000=0

END_TIME=$((SECONDS + DURATION))

while [ $SECONDS -lt $END_TIME ]; do
    # Pick a random user token for REST calls
    IDX=$((RANDOM % AUTH_OK))
    TOKEN="${TOKENS[$IDX]}"

    SELF_TIME=$(curl -s -o /dev/null -w "%{time_total}" --max-time 10 \
        -H "Authorization: Bearer $TOKEN" \
        "$BASE_URL/auth/self" 2>/dev/null || echo "10.0")
    SELF_MS=$(echo "$SELF_TIME * 1000" | bc | cut -d. -f1)

    SPRINT_MS=0
    if [ -n "$SPRINT_ID" ]; then
        SPRINT_TIME=$(curl -s -o /dev/null -w "%{time_total}" --max-time 10 \
            -H "Authorization: Bearer $TOKEN" \
            "$BASE_URL/sprints/$SPRINT_ID" 2>/dev/null || echo "10.0")
        SPRINT_MS=$(echo "$SPRINT_TIME * 1000" | bc | cut -d. -f1)
    fi

    for MS in $SELF_MS $SPRINT_MS; do
        [ "$MS" -eq 0 ] && continue
        REST_COUNT=$((REST_COUNT + 1))
        REST_TOTAL_MS=$((REST_TOTAL_MS + MS))
        [ "$MS" -gt "$REST_MAX_MS" ] && REST_MAX_MS=$MS
        if [ "$MS" -gt 2000 ]; then
            REST_OVER_2000=$((REST_OVER_2000 + 1))
            REST_FAIL=$((REST_FAIL + 1))
        elif [ "$MS" -gt 500 ]; then
            REST_OVER_500=$((REST_OVER_500 + 1))
            REST_PASS=$((REST_PASS + 1))
        else
            REST_PASS=$((REST_PASS + 1))
        fi
    done

    COLOR=$GREEN
    [ "$SELF_MS" -gt 500 ] && COLOR=$YELLOW
    [ "$SELF_MS" -gt 2000 ] && COLOR=$RED
    ELAPSED=$SECONDS
    printf "  [%3ds] /auth/self: ${COLOR}%4dms${NC}" "$ELAPSED" "$SELF_MS"

    if [ -n "$SPRINT_ID" ]; then
        COLOR=$GREEN
        [ "$SPRINT_MS" -gt 500 ] && COLOR=$YELLOW
        [ "$SPRINT_MS" -gt 2000 ] && COLOR=$RED
        printf "  /sprints/%s: ${COLOR}%4dms${NC}" "$SPRINT_ID" "$SPRINT_MS"
    fi
    printf "  (user: stress%d)\n" "$((IDX + 1))"

    sleep "$REST_INTERVAL"
done

