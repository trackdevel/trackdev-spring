/**
 * SSE Stress Test for TrackDev
 *
 * Uses the stress-test users seeded by DemoDataSeeder when
 * STRESS_TEST_ENABLED=true. Each VU logs in as a different user
 * (stress1@trackdev.com .. stress80@trackdev.com), opens an SSE
 * connection, and makes REST calls concurrently.
 *
 * Prerequisites:
 *   - k6 installed (https://grafana.com/docs/k6/latest/set-up/install-k6/)
 *   - Backend running with: STRESS_TEST_ENABLED=true SSE_ENABLED=true
 *   - Dev profile (stress users are only seeded in dev)
 *
 * Usage:
 *   # Quick smoke test (10 users, 30s)
 *   k6 run sse-stress-test.js
 *
 *   # Full simulation (80 users, 5 min) — matches DemoDataSeeder defaults
 *   k6 run --env VUS=80 --env DURATION=5m sse-stress-test.js
 *
 *   # Against remote server
 *   k6 run --env BASE_URL=https://your-server.com/api \
 *          --env VUS=80 --env DURATION=5m sse-stress-test.js
 *
 *   # Ramp-up test (find the breaking point)
 *   k6 run --env SCENARIO=ramp sse-stress-test.js
 *
 *   # Custom password / single user (fallback to original mode)
 *   k6 run --env EMAIL=admin@trackdev.com --env PASSWORD=admin \
 *          --env SPRINT_ID=1 sse-stress-test.js
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { SharedArray } from "k6/data";

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080/api";
const PASSWORD = __ENV.PASSWORD || "stress";
const SPRINT_ID = __ENV.SPRINT_ID || "";  // empty = auto-discover
const VUS = parseInt(__ENV.VUS || "10");
const DURATION = __ENV.DURATION || "30s";
const SCENARIO = __ENV.SCENARIO || "constant";
const USER_COUNT = parseInt(__ENV.USER_COUNT || "80");

// If EMAIL is set, use single-user mode (backward compatible)
const SINGLE_USER_EMAIL = __ENV.EMAIL || "";

// ---------------------------------------------------------------------------
// Custom metrics
// ---------------------------------------------------------------------------
const sseConnected = new Counter("sse_connected");
const sseRejected = new Counter("sse_rejected");
const sseDisabled = new Counter("sse_disabled");
const sseErrors = new Counter("sse_errors");
const restLatency = new Trend("rest_api_latency", true);
const restSuccess = new Rate("rest_api_success");
const restUnderSLA = new Rate("rest_under_500ms");

// ---------------------------------------------------------------------------
// Generate user list (stress1@trackdev.com .. stressN@trackdev.com)
// ---------------------------------------------------------------------------
const stressUsers = new SharedArray("users", function () {
  if (SINGLE_USER_EMAIL) {
    return [{ email: SINGLE_USER_EMAIL, password: PASSWORD }];
  }
  const users = [];
  for (let i = 1; i <= USER_COUNT; i++) {
    users.push({ email: `stress${i}@trackdev.com`, password: PASSWORD });
  }
  return users;
});

// ---------------------------------------------------------------------------
// Scenarios
// ---------------------------------------------------------------------------
const scenarios = {
  constant: {
    constant_load: {
      executor: "constant-vus",
      vus: VUS,
      duration: DURATION,
    },
  },
  ramp: {
    ramp_load: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 20 },
        { duration: "30s", target: 40 },
        { duration: "30s", target: 60 },
        { duration: "30s", target: 80 },
        { duration: "1m", target: 100 },
        { duration: "30s", target: 100 },
        { duration: "30s", target: 0 },
      ],
    },
  },
};

export const options = {
  scenarios: scenarios[SCENARIO] || scenarios.constant,
  thresholds: {
    rest_api_latency: ["p(95)<2000", "p(99)<5000"],
    rest_api_success: ["rate>0.95"],
    rest_under_500ms: ["rate>0.80"],
    http_req_duration: ["p(95)<3000"],
  },
};

// ---------------------------------------------------------------------------
// Setup: authenticate all users and discover sprint IDs
// ---------------------------------------------------------------------------
export function setup() {
  const tokens = [];

  // Authenticate each unique user
  const usersToAuth = SINGLE_USER_EMAIL
    ? [stressUsers[0]]
    : stressUsers.slice(0, Math.min(VUS, stressUsers.length));

  console.log(`Authenticating ${usersToAuth.length} users...`);

  for (const user of usersToAuth) {
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ email: user.email, password: user.password }),
      { headers: { "Content-Type": "application/json" } },
    );

    if (res.status !== 200) {
      console.error(`Login failed for ${user.email} (${res.status}): ${res.body}`);
      continue;
    }

    const body = JSON.parse(res.body);
    tokens.push({ email: user.email, token: body.token });
  }

  if (tokens.length === 0) {
    throw new Error("No users could authenticate — aborting");
  }

  console.log(`${tokens.length} users authenticated successfully`);

  // Auto-discover a sprint ID if not provided
  let sprintId = SPRINT_ID;
  if (!sprintId) {
    const authHeaders = { Authorization: `Bearer ${tokens[0].token}` };
    // Get user's projects
    const projectsRes = http.get(`${BASE_URL}/projects`, { headers: authHeaders });
    if (projectsRes.status === 200) {
      try {
        const projects = JSON.parse(projectsRes.body);
        const projectList = projects.projects || projects;
        if (Array.isArray(projectList) && projectList.length > 0) {
          // Get sprints for the first project
          const projectId = projectList[0].id;
          const sprintsRes = http.get(`${BASE_URL}/projects/${projectId}/sprints`, {
            headers: authHeaders,
          });
          if (sprintsRes.status === 200) {
            try {
              const data = JSON.parse(sprintsRes.body);
              const sprintList = data.sprints || data;
              if (Array.isArray(sprintList)) {
                const active = sprintList.find(s => s.status === "ACTIVE");
                if (active) {
                  sprintId = String(active.id);
                  console.log(`Auto-discovered active sprint ID: ${sprintId} (project ${projectId})`);
                }
              }
            } catch {
              // ignore parse errors
            }
          }
        }
      } catch {
        // ignore parse errors
      }
    }
    if (!sprintId) {
      console.warn("Could not auto-discover sprint ID. SSE tests will be skipped.");
    }
  }

  return { tokens, sprintId };
}

// ---------------------------------------------------------------------------
// Main VU: each VU picks its own user, opens SSE, makes REST calls
// ---------------------------------------------------------------------------
export default function (data) {
  // Pick user based on VU ID (each VU gets a different user)
  const idx = (__VU - 1) % data.tokens.length;
  const { token } = data.tokens[idx];

  const headers = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  // ── Phase 1: Open SSE connection (if we have a sprint) ────────────────
  if (data.sprintId) {
    const sseRes = http.get(`${BASE_URL}/sprints/${data.sprintId}/events`, {
      headers: headers,
      tags: { name: "SSE_connect" },
      timeout: "10s",
    });

    const sseBody = sseRes.body || "";
    if (sseRes.status === 200) {
      if (sseBody.includes("event:connected") || sseBody.includes("event: connected")) {
        sseConnected.add(1);
      } else if (sseBody.includes("event:disabled") || sseBody.includes("event: disabled")) {
        sseDisabled.add(1);
      } else if (sseBody.includes("event:rejected") || sseBody.includes("event: rejected")) {
        sseRejected.add(1);
      } else {
        sseConnected.add(1);
      }
    } else {
      sseErrors.add(1);
    }
  }

  // ── Phase 2: REST API calls under load ────────────────────────────────
  for (let i = 0; i < 5; i++) {
    // Lightweight endpoint
    const selfRes = http.get(`${BASE_URL}/auth/self`, {
      headers: headers,
      tags: { name: "REST_self" },
    });

    restLatency.add(selfRes.timings.duration);
    restSuccess.add(selfRes.status === 200 ? 1 : 0);
    restUnderSLA.add(selfRes.timings.duration < 500 ? 1 : 0);

    check(selfRes, {
      "REST /auth/self is 200": (r) => r.status === 200,
      "REST latency < 500ms": (r) => r.timings.duration < 500,
      "REST latency < 2s": (r) => r.timings.duration < 2000,
    });

    // Heavier endpoint (if sprint available)
    if (data.sprintId) {
      const sprintRes = http.get(`${BASE_URL}/sprints/${data.sprintId}`, {
        headers: headers,
        tags: { name: "REST_sprint" },
      });

      restLatency.add(sprintRes.timings.duration);
      restSuccess.add(sprintRes.status === 200 ? 1 : 0);
      restUnderSLA.add(sprintRes.timings.duration < 500 ? 1 : 0);

      check(sprintRes, {
        "REST /sprints/:id is 200": (r) => r.status === 200,
        "REST sprint latency < 2s": (r) => r.timings.duration < 2000,
      });
    }

    sleep(1 + Math.random() * 2);
  }
}

// ---------------------------------------------------------------------------
// Teardown
// ---------------------------------------------------------------------------
export function teardown(data) {
  console.log("=".repeat(60));
  console.log("SSE STRESS TEST COMPLETE");
  console.log("=".repeat(60));
  console.log(`Target:     ${BASE_URL}`);
  console.log(`Sprint:     ${data.sprintId || "(none)"}`);
  console.log(`Scenario:   ${SCENARIO}`);
  console.log(`Users:      ${data.tokens.length}`);
  console.log("");
  console.log("Key metrics to check:");
  console.log("  rest_api_latency p95 < 2000ms  (REST still fast?)");
  console.log("  rest_api_success > 95%          (REST still working?)");
  console.log("  rest_under_500ms > 80%          (REST not degraded?)");
  console.log("  sse_connected                   (how many SSE connected?)");
  console.log("  sse_rejected                    (connection limits working?)");
  console.log("  sse_disabled                    (kill switch active?)");
  console.log("=".repeat(60));
}
