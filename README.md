# FrivLegion

FrivLegion is a centralized web platform for casual minigames. Additional details: [HELP.md](HELP.md)

[![Open Issues](https://img.shields.io/github/issues-raw/UdL-EPS-SoftArch/frivlegion?logo=github)](https://github.com/orgs/UdL-EPS-SoftArch/projects/12)
[![CI/CD](https://github.com/UdL-EPS-SoftArch/frivlegion/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/UdL-EPS-SoftArch/frivlegion/actions)
[![Cucumber Reports](https://img.shields.io/badge/cucumber-reports-brightgreen)](https://UdL-EPS-SoftArch.github.io/frivlegion/reports/cucumber-report.html)
[![Deployment status](https://img.shields.io/uptimerobot/status/m792691238-18db2a43adf8d8ded474f885)](https://frivlegion.fly.dev/records)

## Vision

**For** casual players
**who** want a single place to discover and play quick minigames
**the project** is a centralized web platform for casual minigames
**that** provides user accounts with authentication and ownership-based access control over platform data
**Unlike** scattered standalone game sites, FrivLegion unifies many minigames under one account-driven platform

## Features per Stakeholder

| USER                          | ADMIN                |
|-------------------------------|----------------------|
| Register                      |                      |
| Login                         |                      |
| Create Record                 |                      |
| Retrieve Record               |                      |
| Update Record                 |                      |
| Delete Record                 |                      |

## Entities Model

```mermaid
classDiagram
    class UriEntity {
        uri : String
    }

    class UserDetails
    <<interface>> UserDetails

    class User  {
        username : String
        password : String
        email : String
    }

    class Record {
        id: Long
        name: String
        description: String
        created: ZonedDateTime
        modified: ZonedDateTime
        status: Status
    }

    UriEntity <|-- User
    UserDetails <|-- User
    UriEntity <|-- Record
    User "1" <-- "*" Record: ownedBy
```

## Profiles and Default Data

- `dev` and `test` seed a `player` account and an `admin` account (ROLE_ADMIN) using `default-password`, overridable with `DEFAULT_PASSWORD`.
- Run locally with the dev profile: `mvn spring-boot:run -Dspring-boot.run.profiles=dev` (it has its own development JWT secret).
- The production profile (`flyio`) seeds no accounts and does not define a default password.
- In production, players register through `POST /auth/register`; the administrator is created from the `ADMIN_*` secrets below.
- The initial game catalog is seeded by the Flyway migration `V4__game_url_and_catalog_seed.sql` (not in the `dev`/`test` profiles, which use Hibernate `ddl-auto`).

## Deployment (Fly.io)

Pushing to `main` deploys the app `frivlegion` to Fly.io through GitHub Actions (`FLY_API_TOKEN` secret). Configure these Fly.io secrets **before** the first deploy (`fly secrets set NAME=value -a frivlegion`):

| Secret | Required | Purpose |
|---|---|---|
| `JWT_SECRET` | **Yes** | Signs the JWT tokens. There is no fallback: the app refuses to start without it. Generate one with `openssl rand -base64 48`. |
| `DATABASE_URL` | Recommended | `postgres://user:password@host[:port]/db[?sslmode=require]`. When set, the app uses PostgreSQL and Flyway migrates it; otherwise it falls back to in-memory H2 and **all data is lost whenever the machine stops** (`auto_stop_machines`). |
| `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Optional | Creates (or promotes) that account as administrator at startup, so the catalog can be managed through `/games`. |

CORS origins for production are in `src/main/resources/application-flyio.yml` (`allowed-origins`); update them with the real frontend URL.

## CI/CD and Testing

The GitHub Actions workflow (`.github/workflows/ci-cd.yml`) runs on every pull request to `main` and on pushes to `main`. Pull requests are automatically validated by compiling the project and running the automated tests (including Cucumber BDD scenarios) against the initialized database schema before merge/deploy steps.

### Database initialization

- Database schema is managed with Flyway migrations in `src/main/resources/db/migration/` (`V1` initial schema, `V2` unique game state, `V3` admin role, `V4` game URL and catalog seed).
- Migrations run automatically on application/test startup, so the CI environment initializes the basic schema before executing tests.

### Testing commands

```bash
# Lint/compile check
mvn -B -DskipTests compile

# Run all automated tests (JUnit + Cucumber BDD)
mvn -B test

# Run only Cucumber behavior tests
mvn -B test -Dtest=CucumberTest
```

The Cucumber HTML report is generated at `target/cucumber-report.html`.

## AI-Assisted Spec-Driven Development (SDD)

This repository serves as a starter template for **AI-Assisted Spec-Driven Development (SDD)** with **Spring Data REST** and **Cucumber BDD**.

It is pre-configured with agent skills (`skills/`), repository instructions (`AGENTS.md`, `CONVENTIONS.md`), and Aider configuration (`.aider.conf.yml`) to support both:
1. **Full-Capability AI Agents** (OpenCode, Claude Code, Cursor, GitHub Copilot, Windsurf).
2. **Token-Constrained APIs** (Aider paired with Groq free tier - 8K token limits).

For complete stage-by-stage instructions and command workflows, see **[AI_DEVELOPMENT_GUIDE.md](AI_DEVELOPMENT_GUIDE.md)**.

### SDD Agent Skills (`skills/`)
- **`skills/sdd-feature-designer/SKILL.md`**: Draft Cucumber `.feature` specs for Spring Data REST endpoints (<50 lines).
- **`skills/sdd-cucumber-steps/SKILL.md`**: Implement MockMvc step definition classes extending `StepDefs.java`.
- **`skills/spring-datarest-entity/SKILL.md`**: Create JPA `@Entity` classes extending `UriEntity` and `@RepositoryRestResource` interfaces with SpEL row-level security queries.
- **`skills/spring-datarest-hander/SKILL.md`**: Create lifecycle event handlers based on the `@RepositoryEventHandler`.
- **`skills/sdd-bdd-verifier/SKILL.md`**: Run `mvn test -Dtest=CucumberTest` and analyze test failure summaries.
