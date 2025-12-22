#!/bin/bash

# TWS CLI Tool
# Usage: ./cli.sh <command> [options]

# Kill any process running on port 8080
PID=$(lsof -ti:8080 2>/dev/null)
if [ ! -z "$PID" ]; then
  echo "Killing process $PID on port 8080..."
  kill -9 $PID 2>/dev/null
  sleep 1
fi

# Parse command
COMMAND=$1
shift

case $COMMAND in
  historical)
    echo "Running Historical Data CLI..."
    ./mvnw spring-boot:run \
      -Dspring-boot.run.arguments="--tws.cli.historical.enabled=true" \
      -Dspring-boot.run.profiles=cli \
      -q
    ;;

  connection)
    echo "Running Connection Test CLI..."
    ./mvnw spring-boot:run \
      -Dspring-boot.run.arguments="--tws.cli.connection.enabled=true" \
      -Dspring-boot.run.profiles=cli \
      -q
    ;;

  *)
    echo "TWS CLI Tool"
    echo ""
    echo "Usage: ./cli.sh <command>"
    echo ""
    echo "Commands:"
    echo "  historical    Fetch historical market data (interactive)"
    echo "  connection    Test TWS connection"
    echo ""
    echo "Examples:"
    echo "  ./cli.sh historical"
    echo "  ./cli.sh connection"
    echo ""
    exit 1
    ;;
esac
