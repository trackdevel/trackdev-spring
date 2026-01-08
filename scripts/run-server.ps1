# TrackDev Spring Boot Server Startup Script (PowerShell)
# Loads environment variables from .env and starts the server
#
# Usage: .\run-server.ps1 [-RootDir <path>] [-EnvFile <path>]
#   -RootDir: Path to Spring Boot project root (default: parent of script directory)
#   -EnvFile: Path to environment file (default: .env in project root directory)
#
# Examples:
#   .\run-server.ps1                                              # Uses defaults
#   .\run-server.ps1 -RootDir C:\projects\my-spring-app           # Custom project root
#   .\run-server.ps1 -RootDir .                                   # Use current directory as root
#   .\run-server.ps1 -EnvFile .env.production                     # Uses .env.production from project root
#   .\run-server.ps1 -RootDir . -EnvFile C:\path\to\.env          # Both custom

param(
    [Parameter()]
    [string]$RootDir,
    
    [Parameter()]
    [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Resolve project root directory
if (-not $PSBoundParameters.ContainsKey('RootDir') -or [string]::IsNullOrWhiteSpace($RootDir)) {
    # Default to parent of script directory (scripts/../)
    $ProjectRoot = Split-Path -Parent $ScriptDir
} elseif ([System.IO.Path]::IsPathRooted($RootDir)) {
    $ProjectRoot = $RootDir
} else {
    # Resolve relative path from current working directory
    $ProjectRoot = (Resolve-Path -Path $RootDir -ErrorAction SilentlyContinue).Path
    if (-not $ProjectRoot) {
        $ProjectRoot = Join-Path (Get-Location) $RootDir
    }
}

# Verify project root exists and has gradlew
if (-not (Test-Path $ProjectRoot)) {
    Write-Host "Error: Project root directory not found at $ProjectRoot" -ForegroundColor Red
    exit 1
}

$GradlewPath = Join-Path $ProjectRoot "gradlew.bat"
if (-not (Test-Path $GradlewPath)) {
    Write-Host "Error: gradlew.bat not found in $ProjectRoot" -ForegroundColor Red
    Write-Host "Please ensure you're pointing to a valid Spring Boot project root." -ForegroundColor Yellow
    exit 1
}

# Resolve environment file path (relative to project root, not script directory)
if ([System.IO.Path]::IsPathRooted($EnvFile)) {
    $EnvFilePath = $EnvFile
} else {
    $EnvFilePath = Join-Path $ProjectRoot $EnvFile
}

# Check if .env file exists
if (-not (Test-Path $EnvFilePath)) {
    Write-Host "Error: Environment file not found at $EnvFilePath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Usage: .\run-server.ps1 [-EnvFile <path>]"
    Write-Host "  -EnvFile: Path to environment file (default: .env)"
    Write-Host ""
    Write-Host "Please create an environment file with the following variables:"
    Write-Host "  JWT_SECRET_KEY=your-secret-key"
    Write-Host "  JWT_TOKEN_LIFETIME=480"
    exit 1
}

# Load environment variables from .env file
Write-Host "Loading environment variables from $EnvFilePath..." -ForegroundColor Cyan
Get-Content $EnvFilePath | ForEach-Object {
    $line = $_.Trim()
    # Skip comments and empty lines
    if ($line -and -not $line.StartsWith("#")) {
        $parts = $line -split "=", 2
        if ($parts.Length -eq 2) {
            $key = $parts[0].Trim()
            $value = $parts[1].Trim()
            # Remove surrounding quotes if present
            $value = $value -replace '^["'']|["'']$', ''
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  Loaded: $key" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "Starting TrackDev Spring Boot Server..." -ForegroundColor Yellow
Write-Host "  Project root: $ProjectRoot" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Yellow

# Change to project root directory and run the Spring Boot application
Set-Location $ProjectRoot
& .\gradlew.bat bootRun @args
