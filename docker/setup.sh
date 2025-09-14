#!/bin/bash

set -e

COMPOSE_ARGS=""

DEMO_KAFKA_NETWORK_NAME="base_kafka-network"
if ! docker network inspect "$DEMO_KAFKA_NETWORK_NAME" >/dev/null 2>&1; then
  echo "🛠️ Network '$DEMO_KAFKA_NETWORK_NAME' not found. Creating..."
  docker network create "$DEMO_KAFKA_NETWORK_NAME"
else
  echo "✅ Network '$DEMO_KAFKA_NETWORK_NAME' already exists."
fi

for file in base/docker-compose*.yml monitoring/docker-compose*.yml; do
  if [ -f "$file" ]; then
    COMPOSE_ARGS="$COMPOSE_ARGS -f $file"
  fi
done

echo "🛠️ Running docker compose with files:"
echo "$COMPOSE_ARGS"

if docker compose $COMPOSE_ARGS up -d --build; then
  echo "✅ Docker Compose completed successfully!"
else
  echo "❌ Docker Compose failed. Please check the logs."
  exit 1
fi
