# IAM Service: extraction stage one

Independent Maven application, Java 21 / Spring Boot 4.0.6, port 8082.
The monolith still owns its existing IAM implementation and remains the default
Gateway destination for authentication and users. No dual writes or data transfer
are implemented.

## Local IntelliJ execution

Open this worktree and import services/iam-service/pom.xml and
services/api-gateway/pom.xml as Maven projects. Select JDK 21 or later.
Run com.frigora.frigoraplatform.IamServiceApplication with externally supplied
IAM_DB_USERNAME, IAM_DB_PASSWORD and IAM_JWT_SECRET. Optional IAM_DB_HOST and
IAM_DB_PORT default to localhost and 3306. Use only an authorized local MySQL
server and a new, exclusive frigora_iam_db schema. Flyway creates users; Hibernate
validates it. Never point IAM at the monolith or Reporting schema.

Run ApiGatewayApplication with IAM_SERVICE_URL=http://localhost:8082 to opt in
for local testing. MONOLITH_URL and REPORTING_SERVICE_URL keep their existing
defaults. Without IAM_SERVICE_URL both IAM route families still use MONOLITH_URL.
No frontend configuration change is required for tests through port 8080.

Reporting must use the same externally supplied HMAC key in REPORTING_JWT_SECRET.
The issuer interprets it as raw UTF-8 bytes, without Base64 decoding, and emits
sub (username), userId (positive int), role (Owner or Provider), iat and exp.
The new role claim is additive; Reporting ignores it and retains its existing
signature, expiration, subject and userId checks. Do not log tokens or put real credentials in run configs
tracked by Git. For a monolith run, override its remote datasource with an
authorized local datasource, PORT=8081, ddl-auto=validate and the same HMAC key.

## Preserved contracts and limitations

| Method | Route | Successful response |
| --- | --- | --- |
| POST | /api/v1/authentication/sign-up | 200, message |
| POST | /api/v1/authentication/sign-in | 200, id/username/token/role |
| GET | /api/v1/users | 200, array of id/username |
| GET | /api/v1/users/{id} | 200, id/username; 404 if absent |
| GET | /api/v1/users/role/{role} | 200, array; 400 for invalid role |

Registration accepts username/password/roles; roles are Owner or Provider.
Login accepts username/password. Users routes require authentication and retain
the existing authorization policy, including the absence of role-based restrictions.
BCrypt and the original controllers/services/security are copied without changing
their behavior. The existing command service throws for invalid credentials and
duplicate usernames instead of returning the Optional.empty branches advertised
by controllers. Tests pin the actual HTTP behavior, including error dispatch;
changing those semantics is a separate contract decision.
The verified HTTP status for wrong credentials, an unknown username and a duplicate
registration is 401: the unhandled exception dispatches to the protected error path.
This is preserved behavior, not a redesigned authentication error contract.

## Monolith identity transition

The default mode still validates JWT then calls
UserDetailsServiceImpl.loadUserByUsername against the monolith UserRepository.
An IAM-only user continues to receive 401 in that mode. It uses the database role;
a signed role claim cannot override that authority. A mismatch between the signed
userId and the local user's ID is rejected rather than mixing identities.
Legacy tokens without userId can use the loaded local user's ID.

To opt in, set MONOLITH_JWT_IDENTITY_ENABLED=true on the monolith and
IAM_SERVICE_URL=http://localhost:8082 on Gateway. Set the same external HMAC key
in IAM_JWT_SECRET, AUTHORIZATION_JWT_SECRET and REPORTING_JWT_SECRET.
The integrated monolith verifies signature, expiration, nonblank subject,
positive integer userId within INT range and the exact role Owner or Provider.
Missing or unknown roles never receive default authorities. No local user lookup
is performed to authenticate. Existing tokens without role must be renewed.

SiteController, EquipmentController and AlertController obtain user IDs from the
authenticated principal. Their normal list queries stay owner-scoped, including
Alerts filtered by equipment. Other resource-ID and explicit by-owner routes
retain their existing policies; this stage does not claim system-wide object
authorization where the original code had none.

No production role-specific route restrictions existed in the reviewed backend:
WebSecurityConfiguration uses authenticated(), and there are no hasAuthority,
hasRole, PreAuthorize or Secured rules. Consequently Owner and Provider both
retain the existing authenticated access. A product role-to-operation policy is
needed before asserting a real forbidden-role 403; it must not be invented in tests.
The model has no persisted enabled/locked/expired account flags; UserDetailsImpl
sets them true. There is no database-backed account-state authorization to copy.
Integrated tokens are valid until expiration; later role changes or user deletion
do not revoke an already issued token. The current lifetime remains seven days.

IamContextFacade, old authentication/users routes, UserCommandServiceImpl,
UserQueryServiceImpl, UserDetailsServiceImpl and token generation by username
remain in-process IAM dependencies in the monolith. Users routes query local
records for their response data, not to authenticate. Gateway must route those
routes to IAM when opting in. No automatic data transfer or dual write is added.

## Future data transfer (not executed)

The mapping has one users table: INT identity id, unique username (50), BCrypt
password (120), role ENUM Owner/Provider and mandatory created_at/updated_at.
There is no separate role table. The migration follows the current entity and
physical naming strategy, not an inspected production schema.

Before transfer, compare an authorized schema export with V1 and reconcile the
actual lengths, nullability, timestamps, collation and role values. Inventory
every external user-ID reference. Plan a registration/write freeze, backup and
rollback; import users with their exact existing IDs, hashes, roles and timestamps
into the new schema, then set AUTO_INCREMENT above MAX(id). Verify row counts,
uniqueness, ID/reference equality and sampled credential checks. Never regenerate
IDs or rehash existing BCrypt values. Fresh test IAM IDs can collide with monolith
IDs, so synthetic accounts must not be treated as migrated identities.
Do not baseline an existing schema silently or write to both databases.

## Verification

From the repository root:

```powershell
./mvnw.cmd -f services/iam-service/pom.xml test
./mvnw.cmd -f services/api-gateway/pom.xml test
./mvnw.cmd -f services/reporting-service/pom.xml test
```

IAM HTTP/JPA tests run Flyway V1 and Hibernate validate on H2 in MySQL mode.
Migration tests retain an explicit ID and reject an unversioned schema.
JWT compatibility compiles the actual Reporting JwtRequestFilter/JwtIdentity
sources into test output only; there is no runtime service dependency.
The monolith regression uses the corresponding security chain and UsersController
with an empty mocked UserRepository and a JWT from actual IAM HTTP registration/login.
It verifies legacy-mode 401 and integrated-mode 200 without authentication lookup.
Gateway tests use disposable HTTP upstreams and exercise default and opted-in
routing. H2 and these upstreams do not certify MySQL or end-to-end deployment.

The optional HTTP regression launches all four packaged applications with separate
in-memory H2 databases, ephemeral ports and an ephemeral signing key:

```powershell
./mvnw.cmd '-Dtest=ControllerIdentityTests,JwtIdentityModeTests,AuthenticatedIdentityTests,TokenServiceImplTests,ReportAuthorizationTests' package
./mvnw.cmd -f services/iam-service/pom.xml package
./mvnw.cmd -f services/reporting-service/pom.xml package
./mvnw.cmd -f services/api-gateway/pom.xml package
python tools/verify_iam_integration.py --java /path/to/jdk/bin/java
```

The script injects H2 through the Boot PropertiesLauncher only for this test;
no production H2 dependency is added. It disables documentation during the run,
creates only synthetic data, checks the empty monolith user table, authenticates
through Gateway, persists/reads a report and site, verifies owner-scoped lists,
rejects an invalid JWT and restarts the monolith in legacy mode. It terminates
only its own processes. It does not assert a production role policy or MySQL.

Local MySQL end-to-end remains pending: authorized isolated schemas and credentials
were not available in the execution environment. Do not use the configured remote
database. The pending check is Gateway login to IAM, then Reporting creation with
that JWT and a SQL read confirming the row in frigora_reporting_db.
Docker, Railway and real-user data transfer are outside this stage.
