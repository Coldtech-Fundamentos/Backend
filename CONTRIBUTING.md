# Contributing

Thank you for contributing to Frigora Platform. This guide describes the development workflow and the conventions used to keep changes safe and reviewable.

## Development setup

Use JDK 21 and the Maven Wrapper in `Frigora-Platform-main`. The monolith, API Gateway, and Reporting Service are independent Maven projects; there is no root aggregator POM.

From the `Frigora-Platform-main` directory, run checks for the module you change:

~~~powershell
.\mvnw.cmd -f services\api-gateway\pom.xml test
.\mvnw.cmd -f services\reporting-service\pom.xml test
~~~

The monolith may require a database configuration for its tests. Review the active profile and test properties before running it.

## Architecture and code

- The migration is incremental. Contexts that have not been extracted remain in the monolith.
- The API Gateway handles HTTP routing and CORS. It is not a business service and must not own business rules or user authorization.
- Reporting Service owns a separate database and validates JWTs independently. Do not point it at the monolith database or add a fallback to the old Reporting implementation.
- Preserve context and service boundaries. Do not share JPA entities or import another service's business classes in place of an explicit contract.
- Preserve existing routes, HTTP methods, DTOs, enums, JSON, and response semantics unless a contract change is approved.
- Follow the existing Java and package conventions. Reuse existing code where its responsibility fits; avoid speculative dependencies, abstractions, or layers.
- Keep controllers focused on HTTP, application services focused on orchestration, and business rules in the domain.
- Update the relevant documentation when routes, configuration, contracts, or run instructions change.

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
