# TrackDev Spring Boot Server Startup Script (PowerShell)
# Loads environment variables from .env and starts the server
#
# Usage: .\run-server.ps1 [-EnvFile <path>] [-SpringProfile <profile>]
#   -EnvFile: Path to environment file (default: .env)
#   -SpringProfile: Spring Boot profile to activate (default: dev)
#
# Examples:
#   .\run-server.ps1                                    # Uses .env, dev profile
#   .\run-server.ps1 -EnvFile .env.prod                 # Uses .env.prod, dev profile
#   .\run-server.ps1 -EnvFile .env.prod -SpringProfile prod  # Uses .env.prod, prod profile

param(
    [Parameter()]
    [string]$EnvFile = ".env",

    [Parameter()]
    [string]$SpringProfile = "dev"
)

$ErrorActionPreference = "Stop"

# Resolve environment file path (relative to current directory or absolute)
if ([System.IO.Path]::IsPathRooted($EnvFile)) {
    $EnvFilePath = $EnvFile
} else {
    $EnvFilePath = Join-Path (Get-Location) $EnvFile
}

# Check if environment file exists
if (-not (Test-Path $EnvFilePath)) {
    Write-Host "Error: Environment file not found at $EnvFilePath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Usage: .\run-server.ps1 [-EnvFile <path>] [-SpringProfile <profile>]"
    Write-Host "  -EnvFile: Path to environment file (default: .env)"
    Write-Host "  -SpringProfile: Spring Boot profile (default: dev)"
    Write-Host ""
    Write-Host "Please create an environment file with the following variables:"
    Write-Host "  JWT_SECRET_KEY=your-secret-key"
    Write-Host "  JWT_TOKEN_LIFETIME=60"
    Write-Host "  MAIL_HOST=smtp.gmail.com"
    Write-Host "  MAIL_PORT=587"
    Write-Host "  MAIL_USERNAME=your-email@gmail.com"
    Write-Host "  MAIL_PASSWORD=your-app-password"
    Write-Host "  DB_URL=jdbc:mysql://localhost:3306/trackdev"
    Write-Host "  DB_USERNAME=trackdev"
    Write-Host "  DB_PASSWORD=trackdev"
    Write-Host "  DISCORD_CLIENT_ID=your_discord_client_id"
    Write-Host "  DISCORD_CLIENT_SECRET=your_discord_client_secret"
    Write-Host "  DISCORD_BOT_TOKEN=your_discord_bot_token"
    Write-Host "  DISCORD_GUILD_ID=your_discord_guild_id"
    Write-Host "  DISCORD_VERIFIED_ROLE_ID=your_discord_verified_role_id"
    Write-Host "  DISCORD_REDIRECT_URI=http://localhost:8080/api/discord/callback"
    exit 1
}

# Project root is the directory containing the environment file
$ProjectRoot = Split-Path -Parent $EnvFilePath

# Verify project root has gradlew
$GradlewPath = Join-Path $ProjectRoot "gradlew.bat"
if (-not (Test-Path $GradlewPath)) {
    Write-Host "Error: gradlew.bat not found in $ProjectRoot" -ForegroundColor Red
    Write-Host "Please ensure the environment file is in a valid Spring Boot project root." -ForegroundColor Yellow
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

# Set Spring profile as environment variable
[Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", $SpringProfile, "Process")
Write-Host "  Loaded: SPRING_PROFILES_ACTIVE" -ForegroundColor Green

Write-Host ""
Write-Host "Starting TrackDev Spring Boot Server..." -ForegroundColor Yellow
Write-Host "  Project root: $ProjectRoot" -ForegroundColor Cyan
Write-Host "  Spring profile: $SpringProfile" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Yellow

# Change to project root directory and run the Spring Boot application
Set-Location $ProjectRoot
& .\gradlew.bat bootRun
