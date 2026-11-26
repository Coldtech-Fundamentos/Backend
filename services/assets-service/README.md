# Assets Service: extraction stage

Independent Java 21 / Spring Boot service for the existing `/api/v1/sites` and
`/api/v1/equipment` contracts. It owns an exclusive `frigora_assets_db` schema;
the monolith and other service schemas must never be used as its datasource.
No data migration or dual-write behavior is implemented in this stage.

## Local execution

Use only an authorized local MySQL instance and a new, exclusive schema. Supply
`ASSETS_DB_USERNAME`, `ASSETS_DB_PASSWORD`, and `ASSETS_JWT_SECRET` externally.
`ASSETS_DB_HOST` and `ASSETS_DB_PORT` default to `localhost` and `3306`.
`ASSETS_JWT_SECRET` must be the same UTF-8 HMAC key supplied to IAM and the
other JWT-verifying services. Do not put credentials in this file or in Git.
From the repository root, run `./mvnw.cmd -f services/assets-service/pom.xml spring-boot:run`
after supplying those environment variables.

The service uses Flyway to create its schema and Hibernate to validate it.
Flyway clean is disabled and baseline-on-migrate is disabled to avoid adopting
an unexpected database. Tests use an isolated in-memory H2 database and synthetic
JWTs; they do not connect to MySQL or IAM.

## API and identifiers

The service preserves the existing site `int` ID and equipment numeric `Long`
ID, the equipment UUID, field names, lower-case equipment status values, and
`ownerId` response field. Owner identity comes only from the validated IAM JWT;
the create payload cannot set it. Reads and deletes are owner-scoped, and
another owner's IDs return 404. The legacy `/by-owner/{ownerId}` routes remain,
but return 404 when the path names another owner.

The current model does not define a site-to-equipment association. Existing
Monitoring and Maintenance records hold site/equipment IDs as scalar values;
this service does not invent a relationship or cross-service foreign key.
Reporting and Maintenance still use 32-bit equipment IDs, while the legacy
equipment API uses `Long`. Check historical ID ranges and plan those consumers'
schema changes before migrating production data.

The Gateway's site and equipment routes are an opt-in destination selected with
`ASSETS_SERVICE_URL`; general `/api/**` traffic remains routed to the monolith.
