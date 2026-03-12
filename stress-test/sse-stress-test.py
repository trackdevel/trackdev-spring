#!/usr/bin/env python3
"""
SSE Stress Test for TrackDev — concurrent SSE + REST + mutations.

Uses stress-test users seeded by DemoDataSeeder (STRESS_TEST_ENABLED=true).
Each simulated user logs in as stress{N}@trackdev.com, opens an SSE
connection, and makes REST calls concurrently. Mutation workers toggle
task statuses (TODO ↔ INPROGRESS) to generate real SSE events.

Prerequisites:
  - Python 3.8+ with requests: pip install requests
  - Backend running with: STRESS_TEST_ENABLED=true SSE_ENABLED=true

Usage:
  python sse-stress-test.py                              # 10 users, 60s
  python sse-stress-test.py -n 80 -d 120                 # 80 users, 120s
  python sse-stress-test.py -u http://localhost:8080/api  # custom URL
  python sse-stress-test.py -m 0                          # disable mutations
  python sse-stress-test.py -h                            # show help
"""

import argparse
import random
import statistics
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass, field

import requests

# ── Colors ──────────────────────────────────────────────────────────────────
RED = "\033[0;31m"
GREEN = "\033[0;32m"
YELLOW = "\033[1;33m"
BLUE = "\033[0;34m"
BOLD = "\033[1m"
NC = "\033[0m"


# ── Shared state ────────────────────────────────────────────────────────────
@dataclass
class Stats:
    """Thread-safe stats collector."""

    lock: threading.Lock = field(default_factory=threading.Lock)
    # SSE
    sse_connected: int = 0
    sse_rejected: int = 0
    sse_disabled: int = 0
    sse_errors: int = 0
    sse_events_received: int = 0
    # REST reads
    rest_latencies: list = field(default_factory=list)
    rest_failures: int = 0
    # Mutations (task status toggles)
    mutations_ok: int = 0
    mutations_failed: int = 0
    mutation_latencies: list = field(default_factory=list)

    def record_sse(self, status: str):
        with self.lock:
            if status == "connected":
                self.sse_connected += 1
            elif status == "rejected":
                self.sse_rejected += 1
            elif status == "disabled":
                self.sse_disabled += 1
            else:
                self.sse_errors += 1

    def record_sse_event(self):
        with self.lock:
            self.sse_events_received += 1

    def record_rest(self, latency_ms: float, success: bool):
        with self.lock:
            if success:
                self.rest_latencies.append(latency_ms)
            else:
                self.rest_failures += 1

    def record_mutation(self, latency_ms: float, success: bool):
        with self.lock:
            if success:
                self.mutations_ok += 1
                self.mutation_latencies.append(latency_ms)
            else:
                self.mutations_failed += 1


stats = Stats()
stop_event = threading.Event()


# ── Authentication ──────────────────────────────────────────────────────────
def authenticate_user(base_url: str, email: str, password: str) -> str | None:
    """Returns JWT token or None on failure."""
    try:
        r = requests.post(
            f"{base_url}/auth/login",
            json={"email": email, "password": password},
            timeout=10,
        )
        if r.status_code == 200:
            return r.json().get("token")
    except Exception:
        pass
    return None


def authenticate_all(base_url: str, num_users: int, password: str) -> list[dict]:
    """Authenticate all stress users concurrently. Returns list of {email, token}."""
    print(f"\nAuthenticating {num_users} stress-test users...")
    users = []
    failed = 0

    with ThreadPoolExecutor(max_workers=min(num_users, 20)) as pool:
        futures = {}
        for i in range(1, num_users + 1):
            email = f"stress{i}@trackdev.com"
            f = pool.submit(authenticate_user, base_url, email, password)
            futures[f] = email

        for f in as_completed(futures):
            email = futures[f]
            token = f.result()
            if token:
                users.append({"email": email, "token": token})
            else:
                failed += 1

    print(f"  {GREEN}OK: {len(users)}{NC}  {RED}Failed: {failed}{NC}")
    return users


# ── Sprint discovery ────────────────────────────────────────────────────────
def discover_user_sprint(base_url: str, token: str) -> int | None:
    """Find an active sprint for a single user."""
    headers = {"Authorization": f"Bearer {token}"}
    try:
        r = requests.get(f"{base_url}/projects", headers=headers, timeout=10)
        if r.status_code != 200:
            return None
        data = r.json()
        projects = data.get("projects", data) if isinstance(data, dict) else data
        if not projects:
            return None

        project_id = projects[0]["id"]
        r = requests.get(
            f"{base_url}/projects/{project_id}/sprints",
            headers=headers,
            timeout=10,
        )
        if r.status_code != 200:
            return None
        data = r.json()
        sprints = data.get("sprints", data) if isinstance(data, dict) else data
        for s in sprints:
            if s.get("status") == "ACTIVE":
                return s["id"]
    except Exception:
        pass
    return None


def discover_all_sprints(
    base_url: str, users: list[dict]
) -> dict[int, list[dict]]:
    """Map each user to their active sprint. Returns {sprint_id: [users]}.

    Each stress user belongs to one project with one active sprint.
    Users in the same project share the same sprint.
    """
    sprint_users: dict[int, list[dict]] = {}

    print("Discovering sprints for all users...")
    with ThreadPoolExecutor(max_workers=min(len(users), 20)) as pool:
        futures = {
            pool.submit(discover_user_sprint, base_url, u["token"]): u
            for u in users
        }
        for f in as_completed(futures):
            user = futures[f]
            sid = f.result()
            if sid:
                sprint_users.setdefault(sid, []).append(user)

    no_sprint = len(users) - sum(len(v) for v in sprint_users.values())
    print(
        f"  {GREEN}{len(sprint_users)} active sprints{NC} across "
        f"{sum(len(v) for v in sprint_users.values())} users"
        + (f"  ({YELLOW}{no_sprint} users without sprint{NC})" if no_sprint else "")
    )
    for sid, susers in sprint_users.items():
        print(f"    Sprint {sid}: {len(susers)} users")
    return sprint_users


# ── Task discovery for mutations ────────────────────────────────────────────
def get_user_id(base_url: str, token: str) -> str | None:
    """Get the current user's ID from /auth/self."""
    try:
        r = requests.get(
            f"{base_url}/auth/self",
            headers={"Authorization": f"Bearer {token}"},
            timeout=10,
        )
        if r.status_code == 200:
            return r.json().get("id")
    except Exception:
        pass
    return None


def discover_toggleable_tasks(
    base_url: str, token: str, sprint_id: int, user_id: str | None = None
) -> list[dict]:
    """Fetch sprint board and find subtasks in TODO or INPROGRESS status.

    If user_id is provided, only returns tasks assigned to that user
    (required by business rule: only assignee can change task status).

    Returns list of {id, status} for tasks that can be toggled.
    """
    headers = {"Authorization": f"Bearer {token}"}
    try:
        r = requests.get(
            f"{base_url}/sprints/{sprint_id}/board",
            headers=headers,
            timeout=10,
        )
        if r.status_code != 200:
            return []
        data = r.json()
        tasks = data.get("tasks", [])
        toggleable = []
        for t in tasks:
            task_type = t.get("type", "")
            status = t.get("status", "")
            # Only toggle subtasks (TASK/BUG), not USER_STORYs
            if task_type not in ("TASK", "BUG") or status not in ("TODO", "INPROGRESS"):
                continue
            # Only toggle tasks assigned to this user (business rule)
            if user_id is not None:
                assignee = t.get("assignee")
                if not assignee or assignee.get("id") != user_id:
                    continue
            toggleable.append({"id": t["id"], "status": status})
        return toggleable
    except Exception:
        return []


# ── Baseline measurement ───────────────────────────────────────────────────
def measure_baseline(base_url: str, token: str, n: int = 5) -> float:
    """Measure average REST latency before SSE load."""
    headers = {"Authorization": f"Bearer {token}"}
    times = []
    for _ in range(n):
        start = time.monotonic()
        try:
            r = requests.get(
                f"{base_url}/auth/self", headers=headers, timeout=10
            )
            elapsed = (time.monotonic() - start) * 1000
            if r.status_code == 200:
                times.append(elapsed)
        except Exception:
            pass
    return statistics.mean(times) if times else 0


# ── SSE worker ──────────────────────────────────────────────────────────────
def sse_worker(
    base_url: str, token: str, sprint_id: int, user_label: str, duration: int
):
    """Hold an SSE connection open, counting events until stop_event is set."""
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "text/event-stream",
    }
    status = "error"
    try:
        # (connect_timeout, read_timeout) — keep the stream open for the full test
        with requests.get(
            f"{base_url}/sprints/{sprint_id}/events",
            headers=headers,
            stream=True,
            timeout=(5, duration + 30),
        ) as r:
            if r.status_code != 200:
                stats.record_sse("error")
                return

            for line in r.iter_lines(decode_unicode=True):
                if stop_event.is_set():
                    break
                if not line:
                    continue
                if line.startswith("event:") or line.startswith("event: "):
                    event_type = line.split(":", 1)[1].strip()
                    if event_type == "connected":
                        status = "connected"
                        stats.record_sse("connected")
                    elif event_type == "disabled":
                        status = "disabled"
                        stats.record_sse("disabled")
                        return
                    elif event_type == "rejected":
                        status = "rejected"
                        stats.record_sse("rejected")
                        return
                    elif event_type == "task_event":
                        stats.record_sse_event()

    except requests.exceptions.Timeout:
        if status == "error":
            stats.record_sse("error")
    except Exception:
        if status == "error":
            stats.record_sse("error")


# ── REST read worker ────────────────────────────────────────────────────────
def rest_worker(
    base_url: str, tokens: list[str], sprint_id: int | None, interval: float
):
    """Make periodic REST GET calls using random user tokens until stop_event."""
    while not stop_event.is_set():
        token = random.choice(tokens)
        headers = {"Authorization": f"Bearer {token}"}

        # GET /auth/self (lightweight)
        try:
            start = time.monotonic()
            r = requests.get(
                f"{base_url}/auth/self", headers=headers, timeout=10
            )
            ms = (time.monotonic() - start) * 1000
            stats.record_rest(ms, r.status_code == 200)
        except Exception:
            stats.record_rest(10000, False)

        # GET /sprints/{id}/board (heavier, realistic)
        if sprint_id:
            try:
                start = time.monotonic()
                r = requests.get(
                    f"{base_url}/sprints/{sprint_id}/board",
                    headers=headers,
                    timeout=10,
                )
                ms = (time.monotonic() - start) * 1000
                stats.record_rest(ms, r.status_code == 200)
            except Exception:
                stats.record_rest(10000, False)

        stop_event.wait(interval + random.random())


# ── Mutation worker ─────────────────────────────────────────────────────────
def mutation_worker(
    base_url: str,
    tokens: list[str],
    sprint_id: int,
    interval: float,
):
    """Toggle task statuses (TODO <-> INPROGRESS) to generate SSE events.

    Each iteration picks a random user, resolves their ID, and only toggles
    tasks assigned to them (business rule: only assignee can change status).
    """
    # Build a map of token → user_id for all users in this sprint
    token_user_ids: dict[str, str | None] = {}
    for token in tokens:
        uid = get_user_id(base_url, token)
        if uid:
            token_user_ids[token] = uid
    valid_tokens = [t for t, uid in token_user_ids.items() if uid]
    if not valid_tokens:
        valid_tokens = tokens  # fallback

    while not stop_event.is_set():
        token = random.choice(valid_tokens)
        user_id = token_user_ids.get(token)
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
        }

        # Discover toggleable tasks assigned to this user
        tasks = discover_toggleable_tasks(base_url, token, sprint_id, user_id)
        if not tasks:
            stop_event.wait(interval)
            continue

        task = random.choice(tasks)
        new_status = "INPROGRESS" if task["status"] == "TODO" else "TODO"

        try:
            start = time.monotonic()
            r = requests.patch(
                f"{base_url}/tasks/{task['id']}",
                headers=headers,
                json={"status": new_status},
                timeout=10,
            )
            ms = (time.monotonic() - start) * 1000
            stats.record_mutation(ms, r.status_code == 200)
        except Exception:
            stats.record_mutation(10000, False)

        stop_event.wait(interval + random.random() * 2)


# ── Live progress ───────────────────────────────────────────────────────────
def progress_reporter(duration: int):
    """Print periodic stats while the test runs."""
    start = time.monotonic()
    while not stop_event.is_set():
        stop_event.wait(5)
        if stop_event.is_set():
            break
        elapsed = int(time.monotonic() - start)
        with stats.lock:
            n = len(stats.rest_latencies)
            avg = statistics.mean(stats.rest_latencies) if n else 0
            mx = max(stats.rest_latencies) if n else 0
            sse = stats.sse_connected
            evts = stats.sse_events_received
            muts = stats.mutations_ok

        color = GREEN if avg < 500 else (YELLOW if avg < 2000 else RED)
        print(
            f"  [{elapsed:3d}s/{duration}s]  "
            f"SSE: {sse} conn  "
            f"REST: {color}{avg:.0f}ms avg{NC} / {mx:.0f}ms max  "
            f"({n} reads, {muts} mutations, {evts} SSE events)"
        )


# ── Results ─────────────────────────────────────────────────────────────────
def print_results(baseline_ms: float):
    lats = stats.rest_latencies
    mlats = stats.mutation_latencies

    print(f"\n{BOLD}{'=' * 60}{NC}")
    print(f"{BOLD}  RESULTS{NC}")
    print(f"{BOLD}{'=' * 60}{NC}")

    # SSE
    print(f"\n{BOLD}SSE Connections:{NC}")
    print(f"  Connected:  {GREEN}{stats.sse_connected}{NC}")
    print(
        f"  Rejected:   {YELLOW}{stats.sse_rejected}{NC} (connection limits working)"
    )
    print(f"  Disabled:   {BLUE}{stats.sse_disabled}{NC} (kill switch active)")
    print(f"  Errors:     {RED}{stats.sse_errors}{NC}")
    print(f"  Events rx:  {stats.sse_events_received}")

    # REST reads
    print(f"\n{BOLD}REST API reads (under load):{NC}")
    if lats:
        avg = statistics.mean(lats)
        p50 = statistics.median(lats)
        sorted_lats = sorted(lats)
        p95 = sorted_lats[int(len(lats) * 0.95)] if len(lats) >= 2 else avg
        p99 = sorted_lats[int(len(lats) * 0.99)] if len(lats) >= 2 else avg
        mx = max(lats)
        over500 = sum(1 for x in lats if x > 500)
        over2000 = sum(1 for x in lats if x > 2000)

        print(f"  Baseline avg:   {baseline_ms:.0f}ms (before load)")
        print(f"  Under-load avg: {avg:.0f}ms")
        print(f"  p50:            {p50:.0f}ms")
        print(f"  p95:            {p95:.0f}ms")
        print(f"  p99:            {p99:.0f}ms")
        print(f"  Max:            {mx:.0f}ms")
        print(f"  Total calls:    {len(lats)}")
        print(f"  Failures:       {RED}{stats.rest_failures}{NC}")
        print(f"  > 500ms:        {YELLOW}{over500}{NC}")
        print(f"  > 2000ms:       {RED}{over2000}{NC}")

        if baseline_ms > 0:
            degradation = avg / baseline_ms * 100
            print(f"\n  Degradation:    {degradation:.0f}% of baseline")
            if degradation <= 200:
                print(f"  {GREEN}✓ REST reads are healthy under load{NC}")
            elif degradation <= 500:
                print(f"  {YELLOW}⚠ REST reads show some degradation{NC}")
            else:
                print(f"  {RED}✗ REST reads are severely degraded!{NC}")
    else:
        print(f"  {YELLOW}No REST read checks completed{NC}")

    # Mutations
    print(f"\n{BOLD}Mutations (task status toggles → SSE broadcasts):{NC}")
    if mlats:
        mavg = statistics.mean(mlats)
        mp50 = statistics.median(mlats)
        sorted_mlats = sorted(mlats)
        mp95 = (
            sorted_mlats[int(len(mlats) * 0.95)] if len(mlats) >= 2 else mavg
        )
        print(f"  Successful:     {GREEN}{stats.mutations_ok}{NC}")
        print(f"  Failed:         {RED}{stats.mutations_failed}{NC}")
        print(f"  Avg latency:    {mavg:.0f}ms")
        print(f"  p50:            {mp50:.0f}ms")
        print(f"  p95:            {mp95:.0f}ms")
        print(f"  Max:            {max(mlats):.0f}ms")
    else:
        total = stats.mutations_ok + stats.mutations_failed
        if total == 0:
            print(f"  {YELLOW}No mutations attempted{NC}")
        else:
            print(f"  {RED}All {stats.mutations_failed} mutations failed{NC}")

    # SSE event delivery check
    if stats.mutations_ok > 0 and stats.sse_connected > 0:
        # Each mutation should produce at least 1 event per connected client
        expected_min = stats.mutations_ok
        ratio = stats.sse_events_received / expected_min if expected_min else 0
        print(f"\n{BOLD}SSE event delivery:{NC}")
        print(f"  Mutations fired: {stats.mutations_ok}")
        print(f"  Events received: {stats.sse_events_received}")
        print(f"  Ratio:           {ratio:.1f}x (expected ~{stats.sse_connected}x for {stats.sse_connected} listeners)")
        if ratio >= 0.5:
            print(f"  {GREEN}✓ SSE events are being delivered{NC}")
        else:
            print(f"  {RED}✗ SSE events may be lost or delayed{NC}")

    print(f"\n{BOLD}{'=' * 60}{NC}")


# ── Main ────────────────────────────────────────────────────────────────────
def main():
    parser = argparse.ArgumentParser(
        description="SSE Stress Test for TrackDev",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="Start backend with: STRESS_TEST_ENABLED=true SSE_ENABLED=true",
    )
    parser.add_argument(
        "-u", "--url", default="http://localhost:8080/api", help="Base API URL"
    )
    parser.add_argument(
        "-p", "--password", default="stress", help="Shared password"
    )
    parser.add_argument(
        "-s", "--sprint-id", type=int, default=0, help="Sprint ID (0=auto)"
    )
    parser.add_argument(
        "-n",
        "--num-users",
        type=int,
        default=10,
        help="Number of concurrent users / SSE connections",
    )
    parser.add_argument(
        "-d", "--duration", type=int, default=60, help="Duration in seconds"
    )
    parser.add_argument(
        "-i",
        "--interval",
        type=float,
        default=2,
        help="REST read interval per worker (seconds)",
    )
    parser.add_argument(
        "-r",
        "--rest-workers",
        type=int,
        default=0,
        help="Number of REST read worker threads (default: num_users/4, min 2)",
    )
    parser.add_argument(
        "-m",
        "--mutation-workers",
        type=int,
        default=0,
        help="Number of mutation workers toggling tasks (default: num_users/8, min 1)",
    )
    parser.add_argument(
        "--mutation-interval",
        type=float,
        default=3,
        help="Min seconds between mutations per worker (default: 3)",
    )
    args = parser.parse_args()

    base_url = args.url
    num_users = args.num_users
    rest_workers = args.rest_workers or max(2, num_users // 4)
    mutation_workers = args.mutation_workers if args.mutation_workers is not None else max(1, num_users // 8)
    # -m 0 explicitly disables mutations
    if args.mutation_workers == 0 and "--mutation-workers" not in sys.argv and "-m" not in sys.argv:
        mutation_workers = max(1, num_users // 8)

    print(f"{BOLD}{'=' * 60}{NC}")
    print(f"{BOLD}  TrackDev SSE Stress Test (Python){NC}")
    print(f"{BOLD}{'=' * 60}{NC}")
    print(f"  Server:           {BLUE}{base_url}{NC}")
    print(f"  Users / SSE:      {BLUE}{num_users}{NC}")
    print(f"  Duration:         {BLUE}{args.duration}s{NC}")
    print(f"  REST workers:     {BLUE}{rest_workers}{NC} (interval: {args.interval}s)")
    print(f"  Mutation workers: {BLUE}{mutation_workers}{NC} (interval: {args.mutation_interval}s)")
    print(f"{BOLD}{'-' * 60}{NC}")

    # 1. Authenticate
    users = authenticate_all(base_url, num_users, args.password)
    if not users:
        print(f"{RED}No users authenticated. Is STRESS_TEST_ENABLED=true?{NC}")
        sys.exit(1)

    tokens = [u["token"] for u in users]

    # 2. Discover sprints — each user maps to their project's active sprint
    if args.sprint_id:
        # Manual override: all users on one sprint
        sprint_users = {args.sprint_id: users}
        print(f"Using manual sprint ID: {args.sprint_id}")
    else:
        sprint_users = discover_all_sprints(base_url, users)
        if not sprint_users:
            print(f"{YELLOW}No active sprints found — SSE and mutation tests skipped{NC}")

    # 3. Baseline
    print("Measuring baseline REST latency...", end=" ")
    baseline = measure_baseline(base_url, tokens[0])
    print(f"{GREEN}{baseline:.0f}ms avg{NC}")

    # 4. Launch everything concurrently
    total_sse = sum(len(v) for v in sprint_users.values())
    print(
        f"\n{BOLD}Starting load test: "
        f"{total_sse} SSE + {rest_workers} REST + {mutation_workers} mutation "
        f"for {args.duration}s...{NC}"
    )

    threads: list[threading.Thread] = []

    # SSE threads — each user connects to their own project's sprint
    for sprint_id, susers in sprint_users.items():
        for u in susers:
            t = threading.Thread(
                target=sse_worker,
                args=(base_url, u["token"], sprint_id, u["email"], args.duration),
                daemon=True,
            )
            t.start()
            threads.append(t)
    if total_sse:
        print(f"  {GREEN}Launched {total_sse} SSE connections across {len(sprint_users)} sprints{NC}")

    # REST read worker threads — scope tokens per sprint so workers only query sprints they have access to
    sprint_list = list(sprint_users.items()) if sprint_users else [(None, users)]
    for i in range(rest_workers):
        sid, susers = sprint_list[i % len(sprint_list)]
        sprint_tokens = [u["token"] for u in susers]
        t = threading.Thread(
            target=rest_worker,
            args=(base_url, sprint_tokens, sid, args.interval),
            daemon=True,
        )
        t.start()
        threads.append(t)
    print(f"  {GREEN}Launched {rest_workers} REST read workers{NC}")

    # Mutation workers — one per sprint (at least), using only users with access
    if sprint_users and mutation_workers > 0:
        # Distribute mutation workers across sprints
        sprint_list = list(sprint_users.items())
        launched_mutations = 0
        for i in range(mutation_workers):
            sprint_id, susers = sprint_list[i % len(sprint_list)]
            sprint_tokens = [u["token"] for u in susers]
            t = threading.Thread(
                target=mutation_worker,
                args=(base_url, sprint_tokens, sprint_id, args.mutation_interval),
                daemon=True,
            )
            t.start()
            threads.append(t)
            launched_mutations += 1
        print(
            f"  {GREEN}Launched {launched_mutations} mutation workers "
            f"across {len(sprint_list)} sprints (toggling TODO ↔ INPROGRESS){NC}"
        )

    # Progress reporter
    progress_t = threading.Thread(
        target=progress_reporter, args=(args.duration,), daemon=True
    )
    progress_t.start()

    # 5. Wait for duration
    try:
        time.sleep(args.duration)
    except KeyboardInterrupt:
        print(f"\n{YELLOW}Interrupted — stopping...{NC}")

    stop_event.set()
    time.sleep(2)

    # 6. Print results
    print_results(baseline)


if __name__ == "__main__":
    main()
