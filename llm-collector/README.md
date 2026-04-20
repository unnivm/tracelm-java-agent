# TraceLM Collector

TraceLM Collector is a Quarkus-based LLM observability service for capturing request traces, computing response evaluation scores, and surfacing operational insights in a browser dashboard.

It is designed to answer practical questions such as:

- Which prompts are costing the most?
- Which requests are slowest?
- How are latency, errors, token usage, and spend trending?
- How strong is the response quality based on a heuristic evaluation score?

## What This Module Includes

- REST APIs for trace ingestion and analytics
- PostgreSQL persistence using Hibernate ORM + Panache
- Flyway migrations for schema management
- Login, signup, session-based auth, and logout flows
- A landing page plus authenticated dashboard UI
- Heuristic scoring for LLM responses
- Retention cleanup for old traces
- Health endpoint support through Quarkus SmallRye Health

## Key Features

### Trace ingestion

The collector accepts trace payloads containing request/response content, latency, token counts, cost, model, and status. Each new trace is persisted and enriched with:

- `qualityScore`
- `evaluationScore`

At the moment, both values are populated from the same evaluator result for backward compatibility.

### Evaluation score

The project computes an `evaluationScore` using the heuristic evaluator in `HeuristicEvaluator`:

```text
evaluationScore =
  0.30 * relevance +
  0.20 * coverage +
  0.15 * length +
  0.15 * structure +
  0.10 * coherence +
  0.10 * penalty
```

This score is derived at ingestion time and then exposed through the APIs and dashboard.

### Dashboard insights

The dashboard includes:

- Request count
- Average latency
- P95 latency
- Error rate
- Total tokens
- Estimated cost
- Average evaluation score
- Recent requests
- Costly prompts
- Slow requests
- Model usage charts
- Time-series traffic view

### Authentication

The web app supports:

- Landing page at `/`
- Signup at `/signup.html`
- Login at `/login.html`
- Dashboard at `/dashboard.html`
- Logout via `/auth/logout`

Browser users authenticate through session cookies. API-key protection can also be enabled for trace APIs.

## Tech Stack

- Java 22
- Quarkus 3
- Hibernate ORM with Panache
- PostgreSQL
- Flyway
- Tailwind CSS and Chart.js in the frontend
- JUnit 5 and RestAssured for tests

## Project Structure

```text
src/main/java/org/usbtechno/collector
├── api          REST resources
├── auth         authentication services and DTOs
├── domain       JPA entities
├── dto          shared API response models
├── exception    exception mappers
├── jobs         scheduled cleanup jobs
├── repository   persistence layer
├── security     request filters
└── util         scoring utilities
```

## Running Locally

### Prerequisites

- Java 22
- Maven
- PostgreSQL running locally or remotely

### Database

By default the application expects:

- database: `llm_collector`
- username: `postgres`
- password: `postgres`
- JDBC URL: `jdbc:postgresql://localhost:5432/llm_collector`

Create the database before startup if it does not already exist.

### Start in dev mode

From this module directory:

```bash
mvn quarkus:dev
```

The app runs on:

```text
http://localhost:8080
```

Flyway migrations run automatically at startup.

## Configuration

The main runtime settings come from `src/main/resources/application.properties`.

Common environment variables:

```bash
HTTP_PORT=8080
DB_KIND=postgresql
DB_URL=jdbc:postgresql://localhost:5432/llm_collector
DB_USERNAME=postgres
DB_PASSWORD=postgres
HIBERNATE_LOG_SQL=false
CORS_ORIGINS=*
MAX_BODY_SIZE=2M
COLLECTOR_API_KEY=
TRACE_RETENTION_DAYS=30
TRACE_RETENTION_CLEANUP_INTERVAL=24h
AUTH_SESSION_DAYS=7
```

## Main Endpoints

### Authentication

- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/logout`
- `GET /auth/me`

### Traces

- `POST /traces`
- `GET /traces?limit=100`
- `GET /traces/page?page=0&size=25`
- `GET /traces/metrics`
- `GET /traces/costly-prompts?limit=5`
- `GET /traces/slow-requests?limit=5`
- `GET /traces/model-metrics`
- `GET /traces/model-analytics`
- `GET /traces/time-series`

### Health

- `GET /q/health`

## Example Trace Payload

```json
{
  "traceId": "trace-1",
  "model": "gpt-4o-mini",
  "prompt": "Summarize the quarterly business update.",
  "response": "Here is a concise summary of the quarterly update...",
  "latency": 120,
  "timestamp": 1712800000000,
  "status": "success",
  "promptTokens": 15,
  "responseTokens": 25,
  "totalTokens": 40,
  "tokenLength": 40,
  "cost": 0.0123
}
```

## Data Model Notes

The trace entity stores:

- request identity through `traceId`
- model name
- prompt and response content
- latency and timestamp
- success or error status
- prompt, response, and total token counts
- estimated request cost
- `qualityScore`
- `evaluationScore`

Schema changes are managed through Flyway migrations in `src/main/resources/db/migration`.

## Testing

The module includes Quarkus tests for:

- trace ingestion
- validation failures
- metrics
- pagination and filtering
- costly prompt and slow request endpoints
- API-key protected access
- signup/login/logout/session flows

Run tests with:

```bash
mvn test
```

## Current Limitations

This project is much closer to a usable internal platform than a demo, but a few areas are still incomplete for a fully hardened enterprise rollout:

- no role-based access control yet
- no password reset or email verification flow
- no rate limiting for ingestion endpoints
- no audit trail for user and admin actions
- no external secret manager integration
- no CI/CD or deployment guide documented here
- no load-testing guidance or production sizing notes

## License

Source files in this module carry the Apache License 2.0 header.
