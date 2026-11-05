# Agent Instructions

These instructions apply to the entire repository. Read the root `README.md` and `CONTRIBUTING.md` before working. Read any more specific `AGENTS.md` files along the affected path.

- Before editing, check the current branch and working tree. Preserve existing changes.
- Verify repository state, POMs, and configuration instead of assuming that a migration or integration has been completed.
- Keep changes within the user's requested scope. Do not add services, directories, dependencies, architectural layers, or contract changes without authorization.
- Respect the boundaries between the monolith and extracted services. Do not move bounded contexts or change data ownership without an explicit decision.
- Preserve existing routes, DTOs, enums, JSON, and HTTP semantics unless the user approves a contract change.
- Do not connect the monolith to a remote database or run destructive SQL. Use authorized local test databases. The current monolith configuration uses `ddl-auto=update`.
- Keep Reporting Service on its separate database. Do not enable dual writes or fallback to the old monolith implementation.
- Do not use Docker, Docker Compose, or Testcontainers in the current workflow unless the user explicitly requests it.
- Never expose credentials, tokens, or secrets in files, commands, logs, or responses.
- Do not create or switch branches, commit, push, open pull requests, or merge unless the user asks.
- Run only checks authorized for the task and report their actual scope. Distinguish H2, mocks, and temporary HTTP destinations from real MySQL or service integration.
