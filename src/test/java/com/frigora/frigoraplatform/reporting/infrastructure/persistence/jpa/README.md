# Reporting JPA tests with MySQL

Requirements: JDK 21 or later, Docker with a Linux engine running, and access to the `mysql:8.4` image. From the application root, run:

~~~powershell
./mvnw.cmd -Dtest=ReportMySqlIntegrationTests test
~~~

If Java is not on PATH, set `JAVA_HOME` to your JDK before running. The first run must download the test dependencies and the image.

The suite is skipped when Docker is unavailable. Skipped tests do not validate MySQL. If Docker is available but MySQL fails to start, the suite must fail and the error must be investigated; it must not be replaced with another database.

## Isolation

- Uses only a disposable MySQL container and temporary credentials.
- Does not load `application.properties`, the main application, or repositories from other modules.
- Does not accept an external URL as a fallback or read project credentials.
- Hibernate creates and drops the schema only inside that container.
- Each test uses a transaction with rollback, forces a flush, and clears the context to verify database reads rather than in-memory entities alone.
- Uses the project's current physical naming strategy, adapters, and real services.

## Scope and limitations

The suite checks CRUD operations, owner and equipment queries, owner immutability during updates, and physical enum names and values through native SQL. Reporting model and repository IDs are `Integer`; enums are stored as Java names (`INSPECTION`, `IN_PROGRESS`), not their JSON labels.

The test schema is generated from the model. This does not prove that an existing database has the same DDL. Before migration, compare columns, nullability, indexes, and types with an authorized schema dump.

`ReportAudit` has Spring Data annotations, but `Report` does not declare `AuditingEntityListener`. The `documentsCurrentMissingAutomaticAuditListener` test documents missing automatic dates; it does not present this limitation as working auditing. Another test checks explicitly assigned date mappings and verifies that `created_at` is not updatable. These tests do not change entities or automatically fix the auditing limitation.
