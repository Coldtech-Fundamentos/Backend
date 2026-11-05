# Reporting Service

Independent Maven project using Spring Boot 4.0.6, Java 21, and port 8085. The monolith remains at the application root with its original POM and routes. There is no Maven aggregator or project dependency between the monolith and this service.

~~~text
Frigora-Platform-main/
├── pom.xml, mvnw, mvnw.cmd, src/   # Monolith, unchanged
└── services/reporting-service/
    ├── pom.xml
    ├── README.md
    └── src/
        ├── main/java/com/frigora/frigoraplatform/
        │   ├── ReportingServiceApplication.java
        │   ├── reporting/         # Initial copy of the domain and API
        │   └── shared/infrastructure/
        │       ├── security/      # Neutral identity and local security
        │       └── persistence/   # Copied physical naming strategy
        ├── main/resources/application.properties
        └── test/java/com/frigora/frigoraplatform/
            ├── ReportingServiceApplicationTests.java
            └── reporting/interfaces/rest/ReportAuthorizationTests.java
~~~

## Build and test

From the application root, with `JAVA_HOME` pointing to the JDK:

~~~powershell
./mvnw.cmd -f services/reporting-service/pom.xml package
~~~

The existing wrapper runs Maven; this service does not inherit from the monolith POM. You can also run `mvn package` from this directory. The build creates `target/reporting-service-0.0.1-SNAPSHOT.jar`.

Tests start the application on a random HTTP port and use in-memory H2, without external credentials. They use signed JWTs, real JPA, and auditing. Inherited MockMvc tests also check the contracts and two owners. H2 does not validate MySQL compatibility. Docker and Testcontainers are not used. Test credentials and the dummy secret are not runtime configuration.

## IntelliJ IDEA configuration

1. Add this `pom.xml` as an additional Maven project; do not replace the root POM.
2. Select JDK 21 and the `reporting-service` module as the runtime classpath.
3. Create an Application configuration for `com.frigora.frigoraplatform.ReportingServiceApplication`.
4. Set this directory as the working directory and configure the environment variables below.
5. Run only after creating the separate empty database and granting its permissions.

| Variable | Purpose |
| --- | --- |
| `REPORTING_DB_HOST` | MySQL host; defaults to `localhost` |
| `REPORTING_DB_PORT` | MySQL port; defaults to `3306` |
| `REPORTING_DB_USERNAME` | Required user dedicated to the separate database |
| `REPORTING_DB_PASSWORD` | Required external password |
| `REPORTING_JWT_SECRET` | Required externally supplied HMAC key from the IAM issuer |

The database is `frigora_reporting_db` and must be empty before first startup. Flyway applies `src/main/resources/db/migration/V1__create_reports.sql` and records the version in `flyway_schema_history`; Hibernate then validates the mapping with `ddl-auto=validate`. No data copy or migration from the monolith is implemented.

Expected fields include `id` (auto-increment integer), `tenant_id`, `equipment_id`, `type`, `title`, `status`, `summary`, `content` (TEXT), `url`, `created_at`, and `updated_at`. Enums are persisted using Java enum names, not REST labels. The physical naming strategy preserves the monolith's table and column names.

Use a MySQL user whose permissions are restricted to `frigora_reporting_db`. Do not reuse the monolith schema, credentials, or URL. Do not override `spring.datasource.url` to point to the old storage. The two applications must use separate databases during this stage. The monolith remains the writer for existing data; the new service may write only to its own database.

## Security and API contract

All `/api/v1/reports` routes require `Authorization: Bearer <JWT>`. JJWT 0.12.6 verifies the HMAC signature using the key's UTF-8 bytes, exactly as IAM does; the key is not Base64-decoded.

The service requires a non-expired `exp`, a non-empty `sub`, and a positive integer `userId` compatible with `Integer`. Old tokens without `userId`, expired or altered tokens, and tokens with invalid claims receive 401. Tokens and keys are never logged.

The service does not query users or passwords and has no `UserRepository`, `UserDetailsService`, or references to IAM code. Local user auto-configuration is disabled. Identity implements the neutral interface copied into this project; it is not linked to monolith source files. Authorities are empty because current tokens have no roles; authorization is owner-based.

DTOs, JSON enums, and routes are preserved: POST returns 201; GET, PUT, and DELETE return 200. Missing or foreign resources return 404. Equipment filters also include the owner. Updates cannot change `tenantId`.

Auditing is enabled only in this copy through `AuditingEntityListener` and a UTC `DateTimeProvider` that returns `OffsetDateTime`, without changing SQL types.

## Pending work

- Run and validate Flyway V1 against real local MySQL and compare it with the original DDL.
- Plan data copying, write cutover, and rollback before routing real traffic.
- Share the key through a secrets channel and plan its rotation; HMAC currently shares a signing key with the issuer, with a future move to public-key signing left for later.
- Define Gateway routing, CORS, and runtime OpenAPI documentation.
- Renew old sessions that lack `userId`.

Creating this project does not change traffic routing or deploy a service.

## Prepare local MySQL (Stage 3.1)

Run this manually with an authorized administrator account for the local MySQL instance. Replace the password placeholder; never store real credentials in the repository. Do not run this against the monolith database.

~~~sql
CREATE DATABASE frigora_reporting_db CHARACTER SET utf8mb4;
CREATE USER 'reporting_app'@'localhost' IDENTIFIED BY '<chosen-local-password>';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
    ON frigora_reporting_db.* TO 'reporting_app'@'localhost';
~~~

There is no need to create `reports` manually. In IntelliJ, set the variables above and run `ReportingServiceApplication` on port 8085. Alternatively, from the application root, with the same external variables:

~~~powershell
./mvnw.cmd -f services/reporting-service/pom.xml spring-boot:run
~~~

The `spring-boot-starter-flyway` dependency runs migrations before JPA; `flyway-mysql` adds MySQL-specific support. Spring Boot manages their versions. Flyway uses the service's dedicated datasource; separate Flyway credentials are not needed. A separate migration account can be introduced later if DDL permissions should be removed from the runtime account.

`baseline-on-migrate=false` prevents automatic adoption of a non-empty schema without migration history; `clean-disabled=true` disables schema cleanup. If the schema is unexpected, stop and investigate. Do not enable baseline, clean, or repair, or drop tables to bypass an error. Applied migrations are immutable; add V2, V3, and later migrations for changes.

V1 uses signed INT identifiers, VARCHAR(255) for text without an explicit length, TEXT for content, and DATETIME(6) for UTC auditing. All fields except the primary key allow NULL, matching the current mapping. ENUMs contain the current Java names. An index on `(tenant_id, equipment_id)` supports owner-and-equipment listings; ID queries use the primary key. There are no foreign keys to users or equipment owned by other services. Spring Data assigns dates; the SQL has no date defaults or triggers.

The monolith's actual DDL was not inspected. MySQL version, effective types and lengths, nullability, ENUM values and order, timestamp precision, charset/collation, indexes, defaults, and existing constraints still need comparison. This migration is for a new database, not for modifying or adopting the old table.

Existing H2 HTTP tests disable Flyway and create their schema in memory. `ReportMigrationTests` runs V1 with Flyway against a separate H2 database in MySQL mode, checks migration history and repeatability, and rejects implicit baselining. This locally checks the script; it does not validate MySQL or the original DDL.
