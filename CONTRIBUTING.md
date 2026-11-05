# Contributing

Thank you for contributing to Frigora Platform. This guide describes the development workflow and the conventions used to keep changes safe and reviewable.

## Development setup

Use JDK 21 and the Maven Wrapper at the repository root. The root POM builds the monolith; the API Gateway and Reporting Service are independent Maven projects and are not included in the root POM.

From the repository root, run checks for the module you change:

~~~powershell
./mvnw.cmd -f services/api-gateway/pom.xml test
./mvnw.cmd -f services/reporting-service/pom.xml test
~~~

The monolith may require a database configuration for its tests. Review the active profile and test properties before running it.

Each independent service requires its own `pom.xml`; these files are source configuration, not generated output. Maven recreates `target/` when building or testing, and Git ignores it throughout the repository.

| Application | POM | Local configuration |
| --- | --- | --- |
| Monolith | `pom.xml` | Set `PORT=8081`, an authorized local test datasource, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, and `AUTHORIZATION_JWT_SECRET` |
| API Gateway | `services/api-gateway/pom.xml` | Port 8080; optional `MONOLITH_URL`, `REPORTING_SERVICE_URL`, and `ANGULAR_ORIGIN` default to localhost ports 8081, 8085, and 4200 |
| Reporting Service | `services/reporting-service/pom.xml` | Port 8085; requires `REPORTING_DB_USERNAME`, `REPORTING_DB_PASSWORD`, and `REPORTING_JWT_SECRET`; optional host and port default to localhost:3306 |

Reporting uses only the separate `frigora_reporting_db` database. Flyway initializes an empty schema and Hibernate validates it. Supply the same test HMAC key as the IAM issuer externally. Its automated tests use H2 and do not establish real MySQL compatibility. Gateway tests use temporary HTTP destinations; integration with the real services still requires an authorized local environment.

## Architecture and code

- The migration is incremental. Contexts that have not been extracted remain in the monolith.
- The API Gateway handles HTTP routing and CORS. It is not a business service and must not own business rules or user authorization.
- Reporting Service owns a separate database and validates JWTs independently. Do not point it at the monolith database or add a fallback to the old Reporting implementation.
- Preserve context and service boundaries. Do not share JPA entities or import another service's business classes in place of an explicit contract.
- Preserve existing routes, HTTP methods, DTOs, enums, JSON, and response semantics unless a contract change is approved.
- Follow the existing Java and package conventions. Reuse existing code where its responsibility fits; avoid speculative dependencies, abstractions, or layers.
- Keep controllers focused on HTTP, application services focused on orchestration, and business rules in the domain.
- Update the relevant documentation when routes, configuration, contracts, or run instructions change.

## Java conventions

- Write identifiers, comments, messages, tests, and OpenAPI descriptions in English.
- Use lowercase package names, PascalCase types, camelCase methods and variables, and UPPER_SNAKE_CASE constants. Name interfaces by their responsibility without an `I` prefix. Use `Impl` consistently for existing interface implementations.
- Match public type names to filenames. Keep each bounded context organized into `domain`, `application`, `infrastructure`, and `interfaces` packages; keep REST controllers under `interfaces/rest`.
- Use `//` for single-line comments and `/* ... */` for multiline implementation comments. Use multiline `/** ... */` Javadoc for API documentation. Explain constraints and intent; remove comments that merely repeat code and delete abandoned commented-out implementations.
- Format Java using google-java-format 1.24.0 in AOSP mode (four-space indentation). Use UTF-8, braces for control flow, separate methods clearly, and prefer explicit imports. Follow `.editorconfig` for other files.
- Prefer constructor injection and final dependencies. Keep mutable state scoped to the smallest possible lifetime. Reuse immutable, thread-safe infrastructure such as JWT parsers.
- Validate request resources at the HTTP boundary. Keep authorization tied to verified identity and scope repository queries to the owner. Never accept an owner ID from an untrusted request body.
- Preserve public routes, JSON property names, enum values, and persisted names during internal refactoring. Verify observable behavior with regression tests before committing optimizations.

## Data and secrets

- Use only authorized local test databases and synthetic data for development.
- The current monolith configuration uses `ddl-auto=update`. Before starting it locally, configure an authorized test datasource and use `ddl-auto=validate`.
- Do not run destructive SQL or migrations against remote databases or real data.
- Do not enable dual writes between the monolith and an extracted service. Define and review a migration, cutover, and rollback plan before transferring data ownership.
- Never commit credentials, JWT secrets, local environment files, or sensitive logs.
- The current development workflow does not require Docker, Docker Compose, or Testcontainers. Do not introduce them as task requirements unless explicitly requested.

## Testing and review

- Add or update tests when implementing behavior changes.
- Run relevant module checks before opening a pull request and state exactly what was tested and in which environment.
- Gateway tests using temporary HTTP destinations do not verify the real services. Reporting tests using H2 do not establish MySQL compatibility.
- Do not describe mocked, H2, or temporary-server results as a full integration test.
- Keep changes focused and avoid unrelated formatting.

## Git Flow

Use `main` for released production history and `develop` for integration. Create short-lived branches from the appropriate base:

| Branch | Base | Purpose and merge target |
| --- | --- | --- |
| `feature/<short-description>` | `develop` | New functionality; merge into `develop` through a pull request |
| `bugfix/<short-description>` | `develop` | Non-urgent fix; merge into `develop` through a pull request |
| `release/vX.Y.Z` | `develop` | Stabilize a release; merge into `main` and back into `develop` |
| `hotfix/vX.Y.Z` | `main` | Urgent production fix; merge into `main` and back into `develop` |

Use lowercase kebab-case branch descriptions. Keep branches focused and do not commit directly to `main` or `develop`.

## Commit messages

Use Conventional Commits in English, with a concise imperative summary and no trailing period:

~~~text
<type>[optional scope][!]: <imperative summary>
~~~

Common types include `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `chore`, and `style`.

Examples:

~~~text
feat(gateway): route report requests
fix(reporting): enforce owner isolation
docs: clarify local backend setup
~~~

Mark breaking changes with `!` and explain them in the body or a `BREAKING CHANGE:` footer.

## Pull requests

Open feature and bugfix pull requests against `develop`. Include the problem, approach, affected modules, contract or data changes, risks, relevant tests, and documentation updates. Request review before merging and resolve or explain review feedback.
