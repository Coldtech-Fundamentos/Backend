# Frigora API Gateway

Independent Maven project using Java 21, Spring Boot **4.0.6**, the Spring Cloud BOM **2025.1.4**, and Spring Cloud Gateway Server WebFlux **5.0.4**. It uses Reactor Netty and does not include MVC, user security, JPA, or a MySQL connection.

The Spring Cloud compatibility verifier remains enabled. CORS is configured with Spring Cloud Gateway properties.

## Run from IntelliJ IDEA

1. Import `services/api-gateway/pom.xml` as an additional Maven project.
2. Select JDK 21 and the `api-gateway` module as the runtime classpath.
3. Create an Application configuration for `com.frigora.gateway.ApiGatewayApplication`.
4. Set `services/api-gateway` as the working directory.
5. Configure the optional environment variables below and run the application.

| Variable | Default |
| --- | --- |
| `REPORTING_SERVICE_URL` | `http://localhost:8085` |
| `MONOLITH_URL` | `http://localhost:8081` |
| `ANGULAR_ORIGIN` | `http://localhost:4200` |

Destination values must be base URLs without path prefixes. The Gateway listens on port **8080**; `SERVER_PORT` can override it. It needs no JWT secrets or MySQL credentials and can start when its destinations are unavailable.

From the backend application directory, with `JAVA_HOME` configured:

~~~powershell
.\mvnw.cmd -f services\api-gateway\pom.xml package
.\mvnw.cmd -f services\api-gateway\pom.xml spring-boot:run
~~~

You can also run `target/api-gateway-0.0.1-SNAPSHOT.jar` with `java -jar`.

## Application ports

| Application | Port | Configuration |
| --- | --- | --- |
| Gateway | 8080 | This project's configuration |
| Monolith | 8081 | Set `PORT=8081` in the IntelliJ run configuration |
| Reporting Service | 8085 | Existing service configuration |

The monolith defaults to port 8080, so set its port to 8081 to avoid a conflict. Its configuration file has not been changed. Reporting retains its MySQL, Flyway, and JWT requirements; the Gateway does not replace them.

## Routes

| ID | Predicate | Order | Destination |
| --- | --- | --- | --- |
| reporting | `/api/v1/reports` and `/api/v1/reports/**` | -10 | `REPORTING_SERVICE_URL` |
| monolith | `/api/**` | 0 | `MONOLITH_URL` |

The lower order value has priority. The Gateway does not strip or rewrite paths and does not retry automatically. It preserves `/api/v1`, HTTP methods, query strings, JSON bodies, and the Authorization header. Login and registration remain at `/api/v1/authentication/**` in the monolith. JWT validation remains the responsibility of each service.

If Reporting is unavailable, requests are not sent to the old Reporting implementation in the monolith. Connection, DNS, and premature-disconnection errors become **502 Bad Gateway**; timeouts become **504 Gateway Timeout**. HTTP responses from services, including 401 and 404, are forwarded unchanged. The connection timeout is two seconds and the response timeout is ten seconds.

## CORS

The default allowed origin is **`http://localhost:4200`**, matching the local Angular origin configured in the monolith. This checkout does not contain Angular, so confirm the actual frontend origin and set `ANGULAR_ORIGIN` if it differs. The origin must match exactly, including scheme and port.

The Gateway allows GET, HEAD, POST, PUT, PATCH, DELETE, and OPTIONS, with Authorization, Content-Type, and Accept headers. Credentials are enabled for compatibility with the existing setup, without wildcard origins. Preflight requests are handled by the Gateway and do not require an active destination. The Gateway deduplicates Access-Control-Allow-Origin and Access-Control-Allow-Credentials because the monolith also emits them. CORS applies to browsers and does not replace authorization.

## Tests and limitations

`ApiGatewayTests` starts the real Gateway on a random port and uses two temporary JDK HTTP servers as destinations. It does not require the monolith, Reporting, Angular, MySQL, or containers. It verifies:

- Reporting collection and item routes take priority over `/api/**`.
- Monolith routes, login, GET/POST/PUT/DELETE, path prefixes, query strings, bodies, and Authorization.
- Allowed and rejected preflight requests, rejected origins and headers, and CORS header deduplication.
- A disconnected destination returns 502 without internal details or fallback to the monolith.
- Destination 404 responses are preserved and paths outside `/api/**` are rejected.

These tests do not verify the real services' business logic or authentication. A manual full-system check requires the monolith and Reporting to run with valid databases and tokens. The suite also simulates slow responses and premature disconnections, verifying 504 and 502 without fallback. Unit tests cover connection timeouts, unrelated exceptions, and committed responses.

## Before connecting Angular

- Start the monolith on 8081 and Reporting on 8085, then verify their local dependencies.
- Confirm the Angular origin and change its API base URL to the Gateway in a later step.
- Agree on the data migration and cutover plan. Reports already route exclusively to the new service; existing reports have not been copied in this stage.
- Test login, JWT handling, and Reporting CRUD through port 8080 using the real services.

Existing applications, Angular, and data were not changed. This project does not implement deployment, service discovery, or a configuration server.
