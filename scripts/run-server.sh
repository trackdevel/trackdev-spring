#!/bin/bash
# TrackDev Spring Boot Server Startup Script
# Loads environment variables from .env and starts the server
#
# Usage: ./run-server.sh [root-dir] [env-file]
#   root-dir: Path to Spring Boot project root (default: parent of script directory)
#   env-file: Path to environment file (default: .env in script directory)
#
# Examples:
#   ./run-server.sh                                    # Uses defaults
#   ./run-server.sh env-file spring-profile            # Custom env file

set -e

# Get project root from first parameter or default to parent of script directory
if [ -n "$1" ]; then
    PROJECT_ROOT="$(cd "$(dirname "$1")" && pwd)"
    ENV_FILE="$1"
else
    PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    ENV_FILE="$PROJECT_ROOT/.env"
fi

if [ -n "$2" ]; then
    SPRING_PROFILE="$2"
else
    SPRING_PROFILE="dev"
fi

echo "PROJECT_ROOT: $PROJECT_ROOT"
echo "ENV_FILE: $ENV_FILE"
echo "SPRING_PROFILE: $SPRING_PROFILE"

# Verify project root exists and has gradlew
if [ ! -d "$PROJECT_ROOT" ]; then
    echo "Error: Project root directory not found at $PROJECT_ROOT"
    exit 1
fi

cd "$PROJECT_ROOT"

if [ ! -f "$PROJECT_ROOT/gradlew" ]; then
    echo "Error: gradlew not found in $PROJECT_ROOT"
    echo "Please ensure you're pointing to a valid Spring Boot project root."
    exit 1
fi

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: Environment file not found at $ENV_FILE"
    echo ""
    echo "Usage: $0 [env-file]"
    echo "  env-file: Path to environment file (default: .env)"
    echo ""
    echo "Please create an environment file with variables like:"
    echo "  JWT_SECRET_KEY=your-secret-key"
    echo "  JWT_TOKEN_LIFETIME=60"
    echo "  MAIL_HOST=smtp.gmail.com"
    echo "  MAIL_PORT=587"
    echo "  MAIL_USERNAME=your-email@gmail.com"
    echo "  MAIL_PASSWORD=your-app-password"
    echo "  DB_URL=jdbc:mysql://localhost:3306/trackdev"
    echo "  DB_USERNAME=trackdev"
    echo "  DB_PASSWORD=trackdev"
    echo "  DISCORD_CLIENT_ID=your_discord_client_id"
    echo "  DISCORD_CLIENT_SECRET=your_discord_client_secret"
    echo "  DISCORD_BOT_TOKEN=your_discord_bot_token"
    echo "  DISCORD_GUILD_ID=your_discord_guild_id"
    echo "  DISCORD_VERIFIED_ROLE_ID=your_discord_verified_role_id"
    echo "  DISCORD_REDIRECT_URI=http://localhost:8080/api/discord/callback"
    exit 1
fi

# Load environment variables from .env file
echo "Loading environment variables from .env..."
while IFS='=' read -r key value; do
    # Skip comments and empty lines
    if [[ -z "$key" || "$key" =~ ^# ]]; then
        continue
    fi
    # Remove any leading/trailing whitespace and quotes
    key=$(echo "$key" | xargs)
    value=$(echo "$value" | xargs)
    # Export the variable
    export "$key=$value"
    echo "  Loaded: $key"
done < "$ENV_FILE"

echo ""
echo "Starting TrackDev Spring Boot Server..."
echo "  Project root: $PROJECT_ROOT"
echo "========================================="

# Run the Spring Boot application from project root
cd "$PROJECT_ROOT"
./gradlew bootRun --args="--spring.profiles.active=$SPRING_PROFILE"
