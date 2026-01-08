# TrackDev API Test Runner (PowerShell)
# This script runs the Postman collection using Newman and generates test reports
#
# Prerequisites:
#   - Node.js installed
#   - Newman installed globally: npm install -g newman
#   - Newman HTML reporter: npm install -g newman-reporter-html
#   - Newman HTML Extra reporter (optional): npm install -g newman-reporter-htmlextra
#
# Usage:
#   .\run-api-tests.ps1 [-BaseUrl "http://localhost:8080"] [-OutputDir ".\test-reports"] [-EnvFile "env.json"]
#

param (
    [string]$BaseUrl = "http://localhost:8080",
    [string]$OutputDir = ".\test-reports",
    [string]$EnvFile = "",
    [switch]$Help
)

# Collection file
$CollectionFile = "TrackDev-API-Tests.postman_collection.json"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Show help
if ($Help) {
    Write-Host "TrackDev API Test Runner (PowerShell)"
    Write-Host ""
    Write-Host "Usage: .\run-api-tests.ps1 [options]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -BaseUrl      Base URL of the API (default: http://localhost:8080)"
    Write-Host "  -OutputDir    Output directory for reports (default: .\test-reports)"
    Write-Host "  -EnvFile      Path to environment file (JSON)"
    Write-Host "  -Help         Show this help message"
    exit 0
}

# Print banner
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "             TrackDev API Test Runner                       " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Newman is installed
$newmanInstalled = $null
try {
    $newmanInstalled = Get-Command newman -ErrorAction SilentlyContinue
} catch {
    $newmanInstalled = $null
}

if (-not $newmanInstalled) {
    Write-Host "Newman is not installed. Installing..." -ForegroundColor Yellow
    npm install -g newman newman-reporter-html newman-reporter-htmlextra
}

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# Check if collection file exists
if (-not (Test-Path $CollectionFile)) {
    Write-Host "Error: Collection file not found: $CollectionFile" -ForegroundColor Red
    exit 1
}

Write-Host "Configuration:" -ForegroundColor Cyan
Write-Host "  Collection: $CollectionFile"
Write-Host "  Base URL: $BaseUrl"
Write-Host "  Output Directory: $OutputDir"
if ($EnvFile) {
    Write-Host "  Environment File: $EnvFile"
}
Write-Host ""

# Build Newman arguments
$newmanArgs = @(
    "run",
    "`"$CollectionFile`"",
    "--env-var", "`"baseUrl=$BaseUrl`"",
    "-r", "cli,json,html",
    "--reporter-json-export", "`"$OutputDir\test-results-$Timestamp.json`"",
    "--reporter-html-export", "`"$OutputDir\test-report-$Timestamp.html`"",
    "--color", "on",
    "--delay-request", "100"
)

# Add environment file if specified
if ($EnvFile) {
    $newmanArgs = @("-e", "`"$EnvFile`"") + $newmanArgs
}

# Check for htmlextra reporter
$htmlextraInstalled = npm list -g newman-reporter-htmlextra 2>$null | Select-String "newman-reporter-htmlextra"
if ($htmlextraInstalled) {
    $newmanArgs = $newmanArgs -replace "cli,json,html", "cli,json,htmlextra"
    $newmanArgs = $newmanArgs -replace "--reporter-html-export", "--reporter-htmlextra-export"
}

Write-Host "Starting API tests..." -ForegroundColor Cyan
Write-Host ""

# Run Newman using npx (more reliable on Windows)
$jsonReport = "$OutputDir\test-results-$Timestamp.json"
$htmlReport = "$OutputDir\test-report-$Timestamp.html"
$newmanCommand = "npx newman run `"$CollectionFile`" --env-var `"baseUrl=$BaseUrl`" -r cli --reporter-json-export `"$jsonReport`" --reporter-html-export `"$htmlReport`" --color on --delay-request 100"

if ($EnvFile) {
    Write-Host "Using env file..." -ForegroundColor Cyan
    Write-Host ""
    $newmanCommand = "npx newman run `"$CollectionFile`" -e `"$EnvFile`" --env-var `"baseUrl=$BaseUrl`" -r cli --reporter-json-export `"$jsonReport`" --reporter-html-export `"$htmlReport`" --color on --delay-request 100"
}

try {
    Invoke-Expression $newmanCommand
    $testExitCode = $LASTEXITCODE
} catch {
    Write-Host "Error running Newman: $_" -ForegroundColor Red
    $testExitCode = 1
}

Write-Host ""

# Generate summary
if ($testExitCode -eq 0) {
    Write-Host "============================================================" -ForegroundColor Green
    Write-Host "                   ALL TESTS PASSED!                        " -ForegroundColor Green
    Write-Host "============================================================" -ForegroundColor Green
} else {
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "                  SOME TESTS FAILED!                        " -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
}

Write-Host ""
Write-Host "Reports generated:" -ForegroundColor Cyan
Write-Host "  JSON: $jsonReport"
Write-Host "  HTML: $htmlReport"
Write-Host ""

# Parse JSON results and print summary
if (Test-Path $jsonReport) {
    try {
        $results = Get-Content $jsonReport | ConvertFrom-Json
        $run = $results.run
        $stats = $run.stats

        Write-Host "Test Summary:" -ForegroundColor Cyan
        Write-Host "  Total Requests: $($stats.requests.total)"
        Write-Host "  Failed Requests: $($stats.requests.failed)"
        Write-Host "  Total Assertions: $($stats.assertions.total)"
        Write-Host "  Failed Assertions: $($stats.assertions.failed)"
        Write-Host "  Total Duration: $([math]::Round($run.timings.completed / 1000, 2))s"

        if ($stats.assertions.failed -gt 0) {
            Write-Host ""
            Write-Host "  Failed Tests:" -ForegroundColor Red
            foreach ($exec in $run.executions) {
                if ($exec.assertions) {
                    foreach ($assertion in $exec.assertions) {
                        if ($assertion.error) {
                            Write-Host "    - $($exec.item.name): $($assertion.assertion)" -ForegroundColor Red
                        }
                    }
                }
            }
        }
    } catch {
        Write-Host "  (Could not parse test results)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "To view the HTML report, open:" -ForegroundColor Cyan
$absolutePath = Resolve-Path $htmlReport -ErrorAction SilentlyContinue
if ($absolutePath) {
    Write-Host "  $absolutePath"
} else {
    Write-Host "  $htmlReport"
}
Write-Host ""

exit $testExitCode
