# Stage 4.1: Gateway and real applications

## Verification status

The monolith controllers, security and CORS configuration, and datasource were inspected. Its default datasource is remote, and it uses `ddl-auto=update`. The Windows `MySQL80` service was running, but no local connection with explicitly authorized credentials was available for this session. The monolith was not started with the remote datasource, and no SQL operations were executed. Communication from the Gateway to the real monolith or Reporting Service was not verified.

At the time of this check, ports 8080, 8081, and 8085 had no listeners, while port 3306 had a listener. There was no observed conflict among the application ports, but none of the three applications was running on them. Automated results reported nine passing Gateway tests and 19 selected passing monolith tests, with no failures or skips. No MySQL tests were run.

The Gateway suite uses the real Gateway with temporary HTTP destinations. Separately run IAM tests use mocked repositories and do not validate a real login against MySQL. These results do not constitute full-system integration.

## Inspected application routes

Every route in the table uses the `/api/v1` prefix.

| Context | Controller | Methods and routes |
| --- | --- | --- |
| IAM | AuthenticationController | POST `/authentication/sign-in`, `/authentication/sign-up` |
| IAM | UsersController | GET `/users`, `/users/{id}`, `/users/role/{role}` |
| Assets | SiteController | POST/GET `/sites`; GET `/sites/{id}`, `/sites/by-owner/{ownerId}`; DELETE `/sites/{id}` |
| Monitoring | EquipmentController | POST/GET `/equipment`; GET `/equipment/{id}`, `/equipment/by-owner/{ownerId}`; DELETE `/equipment/{id}` |
| Monitoring | AlertController | POST/GET `/alert`; GET `/alert/{id}`; PATCH `/alert/{id}/acknowledge`; DELETE `/alert/{id}` |
| Service requests | ServiceRequestsController | POST `/service-requests`; GET `/service-requests/{id}`, `/service-requests/requester/{requesterId}`, `/service-requests/provider/{providerId}`; DELETE `/service-requests/{id}` |
| Service requests | ServiceRequestsController | PATCH `/service-requests/{id}/accept`, `/reject`, `/cancel`, `/assign-technician`, `/complete` |
| Service requests | InterventionsController | POST `/interventions`; GET `/interventions/{id}`, `/interventions/serviceRequest/{serviceRequestId}` |
| Technicians | TechniciansController | POST/GET `/technicians`; GET/PUT/DELETE `/technicians/{id}` |
| Feedback | ReviewsController | POST/GET `/reviews`; GET `/reviews/{id}`, `/reviews/service-request/{serviceRequestId}` |
| Reporting | ReportController | POST/GET `/reports`; GET/PUT/DELETE `/reports/{id}` |

Observed filters include `/alert?equipmentId=...`, `/technicians?providerId=...`, `/service-requests/provider/{providerId}?status=...`, and `/reports?equipmentId=...`. The `serviceRequest` suffix in intervention routes preserves its capitalization, and `/alert` is singular.

The Gateway sends only `/api/v1/reports` and its descendants to port 8085. All other `/api/**` routes go to port 8081 without removing prefixes or modifying JSON. Reporting still exists in the monolith but is not used as a fallback.

## IntelliJ IDEA run configurations

Import all three POM files as separate Maven projects and select JDK 21. Do not combine their classpaths or use the monolith module to run the services.

### Monolith

- Main class: `com.frigora.frigoraplatform.FrigoraPlatformApplication`.
- Classpath: root module; working directory: application root.
- Required variables for this local check:

~~~text
PORT=8081
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/<authorized-local-test-database>?serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<authorized-local-user>
SPRING_DATASOURCE_PASSWORD=<authorized-local-password>
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
AUTHORIZATION_JWT_SECRET=<test-secret-shared-with-reporting>
~~~

Replace placeholders with real test-environment values; they are not executable credentials. The local database must exist and contain a compatible schema and a test user provisioned with a BCrypt password. Do not enable `update` to work around schema errors. Do not run with the default remote configuration.

### Reporting Service

- Main class: `com.frigora.frigoraplatform.ReportingServiceApplication`.
- Classpath: `reporting-service`; working directory: `services/reporting-service`.
- Existing port: 8085.
- Variables: `REPORTING_DB_HOST=localhost`, `REPORTING_DB_PORT=3306`, `REPORTING_DB_USERNAME`, `REPORTING_DB_PASSWORD`, `REPORTING_JWT_SECRET`.
- Use its separate `frigora_reporting_db` database and the same test HMAC secret as the monolith. Flyway prepares only this database according to stage 3.1.

### Gateway

- Main class: `com.frigora.gateway.ApiGatewayApplication`.
- Classpath: `api-gateway`; working directory: `services/api-gateway`.
- Port: 8080; recommended explicit variables:

~~~text
MONOLITH_URL=http://localhost:8081
REPORTING_SERVICE_URL=http://localhost:8085
ANGULAR_ORIGIN=http://localhost:4200
~~~

Before startup, confirm the ports are free. After startup, the three ports should belong to three distinct Java processes:

~~~powershell
Get-NetTCPConnection -State Listen |
    Where-Object LocalPort -In 8080,8081,8085 |
    Select-Object LocalAddress,LocalPort,OwningProcess
~~~

Start the monolith and Reporting first; review their logs without exposing secrets. Then start the Gateway. Do not run two instances with the same configuration.

## Pending manual check (authorized test data only)

1. POST login directly to the monolith at `http://localhost:8081/api/v1/authentication/sign-in` and through the Gateway at `http://localhost:8080/api/v1/authentication/sign-in`, using the same real JSON `{ "username": "...", "password": "..." }` for a test user. Check the `id`, `username`, `token`, and `role` contract. Do not require literal JWT equality or write tokens to console or files.
2. With the returned token, compare direct and Gateway GET requests for `/api/v1/users/{id}` and the listings of each context in the table. Use only known fixture IDs. Compare status codes and JSON. Verify filters with real test IDs.
3. Repeat without Authorization and with an invalid token. Check the real monolith 401 response and its propagation. A valid token for a missing ID may return 404 depending on the controller.
4. Test CORS from an allowed and a disallowed origin. A rejected origin should receive 403 from the Gateway; distinguish it from a 403 returned by the monolith. The SecurityFilterChain has no general role-based rule, so do not assume a different role will return 403.
5. Compare direct Reporting GET requests on 8085 with Gateway requests on 8080, using new tokens that contain `userId`. Verify owner isolation and 404 for another owner's reports in the separate database. An old token without `userId` should still authenticate to the monolith, while Reporting returns 401.
6. Do not run business POST, PUT, PATCH, or DELETE requests against existing data for this check. Their transport is covered automatically; real write tests require fixtures and explicit authorization to use a disposable database.

Login does not create business data, but it requires authorized existing credentials. Do not create accounts or modify databases to manufacture an integration result.

## Observed security and CORS

The monolith has CSRF disabled and uses stateless sessions. Login and registration are public; other routes require authentication. The filter validates the JWT, loads the user by username, and preserves the neutral identity. This stage does not change that mechanism.

Monolith CORS allows `http://localhost:4200` and `https://frigora-frontend.vercel.app`, GET/POST/PUT/PATCH/DELETE/OPTIONS, any header, and credentials. The current Gateway allows only its configured local origin and Authorization, Content-Type, and Accept headers. Deduplication prevents both processes from emitting duplicate CORS headers. If Angular uses another origin, coordinate and update both configurations before connecting it.

## Reproducible tests without MySQL

From the application root, with `JAVA_HOME` configured:

~~~powershell
./mvnw.cmd -f services/api-gateway/pom.xml package
./mvnw.cmd '-Dtest=ReportAuthorizationTests,TokenServiceImplTests,AuthenticatedIdentityTests' test
~~~

Do not run the full monolith suite as a substitute for these selectors: `contextLoads` uses the main configuration, and earlier stages include Testcontainers tests that are outside this check.

Pending: configure an authorized local MySQL database, start the applications, and perform the real integration check before changing Angular's API base URL.
