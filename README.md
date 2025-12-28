# Kotlin Clean Architecture Multi-Module

## Spring Boot (Kotlin) Clean Hexagonal Architecture - Multi-Module Template

**Production-ready** | **Clean Architecture** | **Multi-Module** | **Test-Driven** | **Observability**

> **Original Repository**:
> [https://github.com/awakelife93/spring-boot-kotlin-boilerplate](https://github.com/awakelife93/spring-boot-kotlin-boilerplate)
>
> This project is migrated from [spring-boot-kotlin-boilerplate](https://github.com/awakelife93/spring-boot-kotlin-boilerplate) to implement
**Hexagonal Architecture** and **Multi-Module** structure.

A production-ready Spring Boot multi-module project template built with Kotlin. It follows **Hexagonal Architecture (Ports and Adapters)**
principles and Domain-Driven Design (DDD) patterns to ensure maintainability, testability, and scalability. The project includes a complete
**OpenTelemetry-based observability stack** for unified monitoring, distributed tracing, and log aggregation.

## Architecture Overview

### Hexagonal Architecture (Ports & Adapters)

```
                    ┌─────────────────────────────────────────────┐
                    │          Driving Adapters (Input)           │
                    │   demo-internal-api / demo-external-api     │
                    │                demo-batch                   │
                    └─────────────────────┬───────────────────────┘
                                          │
                             ┌────────────▼────────────┐
                             │      Input Ports        │
                             └────────────┬────────────┘
                                          │
                    ┌─────────────────────▼─────────────────────┐
                    │              Application Core             │
                    │                                           │
                    │   ┌───────────────────────────────────┐   │
                    │   │    demo-application (UseCases)    │   │
                    │   │       • Business Logic            │   │
                    │   │       • Orchestration             │   │
                    │   └─────────────────┬─────────────────┘   │
                    │                     │                     │
                    │   ┌─────────────────▼─────────────────┐   │
                    │   │      demo-domain (Entities)       │   │
                    │   │       • Domain Models             │   │
                    │   │       • Business Rules            │   │
                    │   │       • Port Interfaces           │   │
                    │   └───────────────────────────────────┘   │
                    │                                           │
                    └─────────────────────┬─────────────────────┘
                                          │
                             ┌────────────▼────────────┐
                             │     Output Ports        │
                             └────────────┬────────────┘
                                          │
                    ┌─────────────────────▼─────────────────────┐
                    │         Driven Adapters (Output)          │
                    │           demo-infrastructure             │
                    │   • Database (JPA/PostgreSQL)             │
                    │   • Cache (Redis)                         │
                    │   • Message Queue (Kafka)                 │
                    │   • Email Service                         │
                    └───────────────────────────────────────────┘
```

### Multi-Module Structure

```
root/
├── demo-core/              # Core utilities, configurations, and test fixtures
├── demo-domain/            # Domain entities, value objects, and Port Interfaces
├── demo-application/       # Use cases with Input/Output Ports
├── demo-infrastructure/    # Adapters for Output Ports (DB, Cache, MQ, etc.)
├── demo-internal-api/      # Adapters for Input Ports (Internal REST API)
├── demo-external-api/      # Adapters for Input Ports (External/Public API)
├── demo-batch/            # Adapters for Input Ports (Batch processing)
├── demo-bootstrap/        # Application bootstrap and main entry point
├── gradle/                # Gradle configuration and version catalogs
│   └── libs.versions.toml  # Centralized dependency version management
├── docker/                # Docker compose configurations
└── monitoring/            # Monitoring configurations (Prometheus, Grafana, Opentelemetry Collector, Tempo, Loki)
```

## Technology Stack

### Core Technologies

- **Language**: Kotlin 2.0.21
- **Framework**: Spring Boot 3.5.5
- **JVM**: Java 21 LTS

### Key Libraries & Frameworks

#### Web & API

- Spring WebMVC / WebFlux
- Spring Validation
- SpringDoc OpenAPI (Swagger UI)
- Jackson for JSON processing
- WebClient

#### Security

- Spring Security
- JWT
- BCrypt password encoding
- Role-based access control (RBAC)

#### Database & Persistence

- Spring Data JPA
- QueryDSL
- Flyway
- PostgreSQL / H2

#### Caching & Messaging

- Spring Data Redis
- Apache Kafka (via Spring Kafka)
- Event-driven architecture support

#### Testing

- JUnit 5 / Kotest
- MockK / Mockito Kotlin & Mockito Inline
- Spring Boot Test
- Testcontainers (Integration testing)
- Spring MockMvc

#### Development Tools

- Ktlint / Detekt (Code quality)
- Spring Boot DevTools (Hot reload)
- Gradle 8.10 with Kotlin DSL
- Gradle Version Catalogs ([libs.versions.toml](gradle/libs.versions.toml)) for centralized dependency management
- Docker & Docker Compose
- MailHog / PgAdmin / Kafka UI

#### Monitoring & Logging

- **Observability Stack**: OpenTelemetry Collector, Prometheus, Grafana, Tempo, Loki
- **Application Monitoring**: Spring Actuator, Sentry
- **Logging**: Kotlin Logging, Logback

## Build Configuration

### Multi-Module Structure

The root `build.gradle.kts` manages all subprojects with shared configurations through `subprojects` block.

#### Key Features

**Hexagonal Architecture Module Dependencies:**

```kotlin
// demo-core auto-dependency for all modules (except itself)
if (project.name != "demo-core") {
	api(project(":demo-core"))
}

// Test fixtures sharing for specific modules
val modulesUsingTestFixtures = listOf(
	"demo-application", "demo-infrastructure",
	"demo-internal-api", "demo-external-api",
	"demo-batch", "demo-domain"
)
```

**Test Fixtures Strategy:**

- `demo-core` provides common test utilities, mock data, and base test classes shared across modules
- Each listed module uses these fixtures for their specific testing needs:
	- `demo-application`: Use case testing
	- `demo-infrastructure`: Repository/adapter testing
	- `demo-internal-api`: Controller integration testing
	- `demo-external-api`: API endpoint testing
	- `demo-batch`: Batch job testing
	- `demo-domain`: Domain model testing
- **Excluded modules:**
	- `demo-bootstrap`: Main application module (no test fixtures needed)
	- `demo-core`: Source of test fixtures (doesn't consume itself)

**Security Vulnerability Management:**

```kotlin
// CVE-2025-48924 fix: Force commons-lang3 version globally
configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "org.apache.commons" && requested.name == "commons-lang3") {
			useVersion("3.18.0")
			because("CVE-2025-48924 - Fix Uncontrolled Recursion vulnerability")
		}
	}
}
```

**Executable vs Library Modules:**

```kotlin
val executableModules = listOf("demo-bootstrap")
if (project.name !in executableModules) {
	// Library modules: disable bootJar, enable regular jar
	tasks.named<BootJar>("bootJar") { enabled = false }
	tasks.named<Jar>("jar") { enabled = true }
}
```

## Hexagonal Architecture Implementation

### Port & Adapter Examples

**Output Port (Domain Layer):**

```kotlin
// demo-domain/src/main/kotlin/com/example/demo/user/port/UserPort.kt
interface UserPort : UserCommandPort, UserQueryPort
```

**Output Adapter (Infrastructure Layer):**

```kotlin
// demo-infrastructure/src/main/kotlin/com/example/demo/persistence/user/adapter/UserRepositoryAdapter.kt
@Repository
class UserRepositoryAdapter(
	private val jpaRepository: UserJpaRepository,
	private val userMapper: UserMapper
) : UserPort {
	override fun findOneById(userId: Long): User? =
		jpaRepository.findOneById(userId)?.let {
			userMapper.toDomain(it)
		}
	// ... other implementations
}
```

**Input (Use Case - Application Layer):**

```kotlin
// demo-application/src/main/kotlin/com/example/demo/user/usecase/CreateUserUseCase.kt
@Component
class CreateUserUseCase(
	private val userService: UserService
) : UseCase<CreateUserInput, UserOutput.AuthenticatedUserOutput> {
	override fun execute(input: CreateUserInput): UserOutput.AuthenticatedUserOutput =
		with(input) {
			userService.registerNewUser(this)
		}
}
```

**Input Adapter (REST Controller):**

```kotlin
// demo-internal-api/src/main/kotlin/com/example/demo/user/presentation/UserController.kt
@RestController
@RequestMapping("/api/v1/users")
class UserController(
	private val createUserUseCase: CreateUserUseCase
) {
	@PostMapping
	fun createUser(@RequestBody @Valid createUserRequest: CreateUserRequest): UserResponse {
		val input = UserPresentationMapper.toCreateUserInput(createUserRequest)
		val userOutput = createUserUseCase.execute(input)

		val response = UserPresentationMapper.toCreateUserResponse(userOutput)
		return ResponseEntity.status(HttpStatus.CREATED).body(response)
	}
}
```

## Configuration Management

### Application Configuration Structure

The project implements a **multi-layered configuration approach** that balances modularity with maintainability:

#### 1. Module-Specific Configuration Files

Each module manages its own library-specific configurations:

```
demo-bootstrap/
└── src/main/resources/
    └── application-bootstrap.yml          # Sentry configuration

demo-internal-api/
└── src/main/resources/
    └── application-internal-api.yml       # SpringDoc/Swagger configuration

demo-external-api/
└── src/main/resources/
    └── application-external-api.yml       # Webhook configuration

demo-infrastructure/
└── src/main/resources/
    └── application-infrastructure.yml     # Actuator/Management configuration
```

#### 2. Core Configuration (Central Management)

```
demo-core/src/main/resources/
├── application-common.yml                 # Common settings + imports
├── application-dev.yml                    # Development environment
├── application-prod.yml                   # Production environment
├── application-local.yml                  # Local development
├── application-secret-local.yml           # Local secrets
├── application-secret-dev.yml             # Development secrets
└── application-secret-prod.yml            # Production secrets
```

#### 3. Configuration Import Strategy

**Core configuration imports all module-specific settings:**

```yaml
# demo-core/src/main/resources/application-common.yml
spring:
	config:
		import:
			- "optional:classpath:application-bootstrap.yml"        # Sentry
			- "optional:classpath:application-internal-api.yml"     # SpringDoc
			- "optional:classpath:application-external-api.yml"     # Webhook
			- "optional:classpath:application-infrastructure.yml"   # Management
```

#### 4. Environment-Specific Overrides

Environment files can override any module setting:

```yaml
# application-prod.yml
springdoc:
	swagger-ui:
		enabled: false # Override from demo-internal-api module
	api-docs:
		enabled: false

sentry:
	dsn: # Override from demo-bootstrap module (empty for prod)
	logging:
		minimum-event-level: ERROR

webhook:
	slack:
		url: # Override from demo-external-api module (empty for prod)
```

> **Note:** IDE may show "Cannot resolve configuration property" warnings for cross-module properties. This is expected and can be ignored
> as the configuration works correctly at runtime.

#### 5. Configuration Loading Order

Spring Boot loads configurations in this priority order:

1. **Environment-specific files** (`application-prod.yml`)
2. **Module-specific files** (`application-bootstrap.yml`)
3. **Common configuration** (`application-common.yml`)

This ensures environment settings always take precedence over module defaults.

## Key Features

### 1. Database Management

- **DDL Management**: Uses Flyway for migration scripts instead of JPA auto-generation
- **Migration Scripts**: Located in [demo-core/src/main/resources/db/migration](demo-core/src/main/resources/db/migration)
- **Alternative**: JPA DDL auto-generation available via configuration
	in [application-common.yml](demo-core/src/main/resources/application-common.yml)

### 2. Spring Batch Configuration

- **Metadata Tables**: Create Spring Batch metadata table for all environments
- **PostgreSQL Schema**:
	Uses [batch-postgresql-metadata-schema.sql](demo-core/src/main/resources/db/sql/batch-postgresql-metadata-schema.sql)
- **Reference**:
	[Spring Batch Schema](https://github.com/spring-projects/spring-batch/blob/5.0.x/spring-batch-core/src/main/resources/org/springframework/batch/core/schema-postgresql.sql)

### 3. Webhook Integration

- **Configuration**: [enable & route endpoint](demo-core/src/main/resources/application-common.yml) (default enabled)
- **Supported Types**: Slack, Discord

```kotlin
// Usage examples
webHookProvider.sendAll(
	"Subscription request received from method ${parameter.method?.name}.",
	mutableListOf("Request Body: $body")
)

webHookProvider.sendSlack(
	"Failed to send message to Kafka",
	mutableListOf("Error: ${exception.message}")
)

webHookProvider.sendDiscord(
	"Batch processing completed",
	mutableListOf("Results: $results")
)
```

### 4. Email Testing

- **MailHog Integration**: Email testing tool with SMTP port 1025
- **Configuration**: Settings in `application-local.yml` and `application-secret-local.yml`

### 5. Code Quality Tools

- **Ktlint**: Official lint rules, configuration in [.editorconfig](.editorconfig)
	- Report output: `build/reports/ktlint`
- **Detekt**: Static analysis, rules in [detekt.yml](detekt.yml)
	- Report output: `build/reports/detekt`

### 6. Testing Strategies

**Mockito-based Testing:**

- [BaseIntegrationController](demo-internal-api/src/test/kotlin/com/example/demo/mockito/common/BaseIntegrationController.kt)

**Kotest & MockK Testing:**

- [BaseIntegrationController](demo-internal-api/src/test/kotlin/com/example/demo/kotest/common/BaseIntegrationController.kt)
- **Security Bypass**:
	[SecurityListenerFactory](demo-internal-api/src/test/kotlin/com/example/demo/kotest/common/security/SecurityListenerFactory.kt)

```kotlin
// Example: Bypassing Spring Security in tests
listeners(SecurityListenerFactory())

Then("Call DELETE /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
	// ... test implementation
}
```

### 7. Kafka Integration

- **Topic Management**:
	[KafkaTopicMetaProvider](demo-infrastructure/src/main/kotlin/com/example/demo/kafka/provider/KafkaTopicMetaProvider.kt)
- **DLQ Support**: Dynamic DLQ creation
	with [default fallback partition: 1](demo-infrastructure/src/main/kotlin/com/example/demo/kafka/DlqHelper.kt)

### 8. Event-Driven Examples

**User Registration Flow:**

- [User signup triggers email event](demo-application/src/main/kotlin/com/example/demo/user/event/UserEventHandler.kt)
- [Welcome email via Kafka](demo-infrastructure/src/main/kotlin/com/example/demo/kafka/adapter/WelcomeSignUpKafkaAdapter.kt)

**User Cleanup Flow:**

- [Hard delete after one year](demo-batch/src/main/kotlin/com/example/demo/batch/user/writer/UserDeleteItemWriter.kt)
- [User deletion via Kafka](demo-infrastructure/src/main/kotlin/com/example/demo/kafka/adapter/UserDeleteKafkaAdapter.kt)

### 9. OpenTelemetry Stack Configuration (Monitoring & Observability)

**Architecture:**
- All observability data (metrics, traces, logs) are collected through **OpenTelemetry Collector**
- Spring Boot application uses **OpenTelemetry Spring Boot Starter** (SDK) to auto-instrument and send telemetry data
- OpenTelemetry Collector routes data to Prometheus, Tempo, and Loki

**Configuration Files:**
- **OpenTelemetry Collector**: [otel-collector-config.yml](monitoring/otel-collector-config.yml)
	- Receives: OTLP gRPC (localhost:4317), OTLP HTTP (localhost:4318)
	- Exports to: Prometheus, Tempo, Loki
- **Prometheus**: [prometheus.yml](monitoring/prometheus.yml) - Metrics collection
- **Tempo**: [tempo.yml](monitoring/tempo.yml) - Distributed tracing
- **Loki**: [loki.yml](monitoring/loki.yml) - Log aggregation
- **Grafana**: Unified dashboard at http://localhost:3000

**Application Settings:**
- [application-infrastructure.yml](demo-infrastructure/src/main/resources/application-infrastructure.yml)
	- OpenTelemetry exporter configuration (`management.otel.*`)
	- Spring Actuator settings for observability

### 10. Docker & Infrastructure

- Complete Docker Compose setup for all services
- Detailed setup guide: [Docker Setup Guide](docker/README.md)

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Docker & Docker Compose** (for infrastructure services)
- **Gradle 8.10** (wrapper included)

### Quick Start

#### 1. Start Infrastructure Services

```bash
cd docker && ./setup.sh
```

> **For detailed setup information**, see [Docker Setup Guide](docker/README.md) which explains:
> - Network configuration and auto-creation
> - Individual service management
> - Service dependencies and startup order

#### 2. Run the Application

```bash
./gradlew :demo-bootstrap:bootRun
```

## Development Commands

### Build & Test

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :demo-application:test
```

### Code Quality

```bash
# Run ktlint check
./gradlew ktlintCheck

# Format code with ktlint
./gradlew ktlintFormat

# Run detekt analysis
./gradlew detekt
```

## Service Access URLs

### Application Services

- **API Documentation (Swagger)**: http://localhost:8085/swagger-ui/index.html
- **H2 Console** (local environment): http://localhost:8085/h2-console
- **Application Server**: http://localhost:8085

### Infrastructure Services

- **MailHog** (Email Testing): http://localhost:8025
- **PgAdmin** (PostgreSQL Management): http://localhost:8088
- **Kafka UI** (Kafka Management): http://localhost:9000
- **Redis** (CLI/Client access): localhost:6379
- **PostgreSQL** (Database connection): localhost:5432
- **Kafka** (Broker connection): localhost:9092
- **Zookeeper** (Coordination service): localhost:2181

### Observability

- **Grafana** (Unified Observability Dashboard): http://localhost:3000
	- Metrics (Prometheus), Traces (Tempo), Logs (Loki) visualization
	- **Data Source Configuration** (use Docker internal network addresses):
		- Prometheus: `http://prometheus:9090`
		- Tempo: `http://tempo:3200`
		- Loki: `http://loki:3100`
- **Prometheus** (Metrics Collection): http://localhost:9090
- **Tempo** (Distributed Tracing): http://localhost:3200
- **Loki** (Log Aggregation): http://localhost:3100
- **OpenTelemetry Collector**:
	- gRPC: localhost:4317
	- HTTP: localhost:4318

## Author

**Hyunwoo Park**
