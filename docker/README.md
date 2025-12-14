# Docker Compose Setup

This project is composed of several independent Docker services such as PostgreSQL, Redis, Kafka, Prometheus, Tempo, and
Grafana.
To make it easier to manage and run all containers at once, we use a script-based approach with multiple Compose files.

## Structure

```
docker/
├── base/
│ ├── docker-compose.postgres.yml
│ ├── docker-compose.redis.yml
│ ├── docker-compose.pgadmin.yml
│ ├── docker-compose.mailhog.yml
│ ├── docker-compose.zookeeper-kafka.yml
│ └── docker-compose.kafka-ui.yml
├── monitoring/
│ ├── docker-compose.prometheus.yml
│ ├── docker-compose.tempo.yml
│ └── docker-compose.grafana.yml
setup.sh

```

## Services

### Base Services

- **PostgreSQL**: Database service (Port: 5432)
- **Redis**: Cache service (Port: 6379)
- **PgAdmin**: PostgreSQL web management tool
- **MailHog**: Email testing service (SMTP Port: 1025)
- **Kafka & Zookeeper**: Message broker and coordination service
- **Kafka UI**: Kafka web management tool

### Monitoring Services

- **Prometheus**: Metrics collection and storage
- **Tempo**: Distributed tracing system
- **Grafana**: Metrics visualization and dashboards

### Network Configuration

The setup script automatically manages a dedicated Docker network:

- **Network Name**: `base_kafka-network`
- **Purpose**: Enables reliable service discovery and communication between Kafka, Zookeeper, and Kafka UI
- **Why needed**: Kafka services require stable network connectivity for cluster coordination and client connections
- **Auto-creation**: The network is automatically created if it doesn't exist, preventing startup failures

For all service URLs and detailed port information, see the main [README.md](../README.md).

## How to Start Services

### Start All Services

Simply run the following script to start all services in the background:

```bash
cd docker
chmod +x setup.sh
./setup.sh
```

The script will:

1. **Check and create Kafka network**: Automatically creates the `base_kafka-network` Docker network if it doesn't exist
2. **Combine all docker-compose files**: Merges all service configurations into a single project
3. **Start services**: Launches all services in the background with proper dependency resolution

### Start Individual Services

For development, you might want to start only specific services. Since services have dependencies, start them in groups:

```bash
cd docker

# PostgreSQL and PgAdmin together
docker compose -f base/docker-compose.postgres.yml -f base/docker-compose.pgadmin.yml up -d

# Kafka ecosystem (Zookeeper, Kafka, and Kafka UI)
docker compose -f base/docker-compose.zookeeper-kafka.yml -f base/docker-compose.kafka-ui.yml up -d

# Monitoring stack (Prometheus, Tempo and Grafana)
docker compose -f monitoring/docker-compose.prometheus.yml -f monitoring/docker-compose.tempo.yml -f monitoring/docker-compose.grafana.yml up -d

# Independent services
docker compose -f base/docker-compose.redis.yml up -d
docker compose -f base/docker-compose.mailhog.yml up -d
```

### Stop Services

```bash
# Stop all services (using the same combined approach)
cd docker
docker compose -f base/docker-compose.postgres.yml \
                -f base/docker-compose.redis.yml \
                -f base/docker-compose.pgadmin.yml \
                -f base/docker-compose.mailhog.yml \
                -f base/docker-compose.zookeeper-kafka.yml \
                -f base/docker-compose.kafka-ui.yml \
                -f monitoring/docker-compose.prometheus.yml \
                -f monitoring/docker-compose.tempo.yml \
                -f monitoring/docker-compose.grafana.yml \
                down

# Or stop specific service groups
docker compose -f base/docker-compose.postgres.yml -f base/docker-compose.pgadmin.yml down
docker compose -f base/docker-compose.zookeeper-kafka.yml -f base/docker-compose.kafka-ui.yml down
```
