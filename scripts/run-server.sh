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
#   ./run-server.sh /path/to/spring-app                # Custom project root
#   ./run-server.sh "" .env.production                 # Default root, custom env file
#   ./run-server.sh /path/to/spring-app .env.prod      # Both custom

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Get project root from first parameter or default to parent of script directory
if [ -n "$1" ]; then
    # If parameter is absolute path, use it directly; otherwise, relative to current dir
    if [[ "$1" = /* ]]; then
        PROJECT_ROOT="$1"
    else
        PROJECT_ROOT="$(pwd)/$1"
    fi
else
    PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
fi

# Verify project root exists and has gradlew
if [ ! -d "$PROJECT_ROOT" ]; then
    echo "Error: Project root directory not found at $PROJECT_ROOT"
    exit 1
fi

if [ ! -f "$PROJECT_ROOT/gradlew" ]; then
    echo "Error: gradlew not found in $PROJECT_ROOT"
    echo "Please ensure you're pointing to a valid Spring Boot project root."
    exit 1
fi

# Get environment file from second parameter or use default
if [ -n "$2" ]; then
    # If parameter is absolute path, use it directly; otherwise, relative to script dir
    if [[ "$2" = /* ]]; then
        ENV_FILE="$2"
    else
        ENV_FILE="$SCRIPT_DIR/$2"
    fi
else
    ENV_FILE="$SCRIPT_DIR/.env"
fi

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: Environment file not found at $ENV_FILE"
    echo ""
    echo "Usage: $0 [env-file]"
    echo "  env-file: Path to environment file (default: .env)"
    echo ""
    echo "Please create an environment file with the following variables:"
    echo "  JWT_SECRET_KEY=your-secret-key"
    echo "  JWT_TOKEN_LIFETIME=480"
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
./gradlew bootRun "$@"
