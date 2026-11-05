# Frigora API Gateway

Proyecto Maven independiente con Java 21, Spring Boot **4.0.6**, Spring Cloud BOM
**2025.1.4** y Spring Cloud Gateway Server WebFlux **5.0.4** (versión del BOM).
Usa Reactor Netty; no incluye MVC, seguridad de usuarios, JPA ni conexión MySQL.

Compatibilidad comprobada en la [matriz oficial de Spring Cloud](https://spring.io/projects/spring-cloud/):
la línea 2025.1.x admite Spring Boot 4.0.x. Se conserva activo el verificador
de compatibilidad de Spring Cloud; no se fuerza ni desactiva para arrancar.
El BOM resuelto y el arranque de las pruebas confirman las versiones utilizadas.
Configuración CORS basada en las [propiedades oficiales de Gateway](https://docs.spring.io/spring-cloud-gateway/reference/configprops.html).

## Ejecutar desde IntelliJ

1. Importar `services/api-gateway/pom.xml` como proyecto Maven adicional.
2. Elegir JDK 21 y el módulo `api-gateway` como classpath.
3. Crear una configuración Application para `com.frigora.gateway.ApiGatewayApplication`.
4. Usar `services/api-gateway` como working directory.
5. Configurar las variables opcionales de la tabla siguiente y ejecutar.

| Variable | Valor predeterminado |
| --- | --- |
| `REPORTING_SERVICE_URL` | `http://localhost:8085` |
| `MONOLITH_URL` | `http://localhost:8081` |
| `ANGULAR_ORIGIN` | `http://localhost:4200` |

Los destinos deben ser URLs base sin prefijos de ruta, por ejemplo `http://localhost:8085`.
El Gateway escucha en **8080**; `SERVER_PORT` permite sobrescribirlo si es necesario.
No necesita secretos JWT ni credenciales MySQL. Puede arrancar sin los destinos activos.

Desde la raíz del backend, con `JAVA_HOME` configurado:

```powershell
./mvnw.cmd -f services/api-gateway/pom.xml package
./mvnw.cmd -f services/api-gateway/pom.xml spring-boot:run
```

También puede ejecutarse `target/api-gateway-0.0.1-SNAPSHOT.jar` con `java -jar`.

## Puertos del conjunto

| Aplicación | Puerto | Cómo configurarlo |
| --- | --- | --- |
| Gateway | 8080 | Configuración de este proyecto |
| Monolito | 8081 | Agregar **`PORT=8081`** a su configuración de ejecución en IntelliJ |
| Reporting Service | 8085 | Configuración existente del servicio |

El monolito todavía declara `server.port=${PORT:8080}`. Es necesario asignarle
8081 en su ejecución para evitar la colisión; no se cambió su archivo de configuración.
Reporting mantiene sus requisitos de MySQL/Flyway/JWT; el Gateway no los sustituye.

## Rutas

| ID | Predicado | Orden | Destino |
| --- | --- | --- | --- |
| reporting | `/api/v1/reports` y `/api/v1/reports/**` | -10 | `REPORTING_SERVICE_URL` |
| monolith | `/api/**` | 0 | `MONOLITH_URL` |

El menor orden tiene prioridad. No hay StripPrefix, RewritePath ni reintentos
automáticos. Se conservan `/api/v1`, métodos, query string, JSON y Authorization.
Login y registro siguen en `/api/v1/authentication/**` en el monolito.
La validación JWT sigue siendo responsabilidad de los servicios, sin dependencia IAM en el Gateway.

Si Reporting cae, no se utiliza como fallback el Reporting antiguo del monolito:
esto evita escribir en dos almacenamientos diferentes. Errores de conexión/DNS se
traducen a **502 Bad Gateway** y los timeouts a **504 Gateway Timeout**.
Las respuestas HTTP de los servicios, incluidos 401/404, se transmiten sin sustituirlas.
Hay 2 segundos de límite de conexión y 10 segundos de espera de respuesta.

## CORS

Origen permitido por defecto: **`http://localhost:4200`**, el origen Angular local
declarado en el CORS actual del monolito. No hay código Angular en este checkout para
confirmar otro puerto; si el frontend usa otro origen, ajustar `ANGULAR_ORIGIN`.
El origen debe coincidir exactamente, incluido esquema y puerto.

Se permiten GET, HEAD, POST, PUT, PATCH, DELETE y OPTIONS, y los encabezados
Authorization, Content-Type y Accept. Credenciales habilitadas para conservar
compatibilidad con la configuración existente, sin orígenes comodín.
Los preflight se resuelven en el Gateway y no requieren que el destino esté activo.
Se deduplican Access-Control-Allow-Origin y Access-Control-Allow-Credentials porque
el monolito también los emite. CORS solo afecta a navegadores: no reemplaza autorización.

## Pruebas

`ApiGatewayTests` arranca el Gateway real en puerto aleatorio y dos servidores HTTP
temporales del JDK como destinos. No requiere monolito, Reporting, Angular, MySQL
ni contenedores. Verifica:

- Ruta de reportes, tanto la colección como IDs, con prioridad sobre `/api/**`.
- Rutas del monolito, login, GET/POST/PUT/DELETE, prefijos, query, cuerpos y Authorization.
- Preflight permitido, origen/encabezado rechazados y deduplicación CORS.
- Destino desconectado: 502 sin detalles internos y sin fallback al monolito.
- Conservación de 404 del destino y rechazo de rutas fuera de `/api/**`.

Las pruebas no verifican la lógica ni la autenticación de los servicios reales.
La comprobación manual del sistema completo sí requiere monolito y Reporting
funcionando, con sus bases y tokens válidos. El escenario de timeout 504 está
configurado pero no se simula en esta suite.

## Antes de conectar Angular

- Iniciar el monolito en 8081 y Reporting en 8085; probar sus dependencias locales.
- Confirmar el origen Angular y, en una etapa posterior, cambiar su URL base al Gateway.
- Acordar la migración y el corte de datos: reportes ya se enrutan exclusivamente
  al servicio nuevo, sin copiar los reportes antiguos en esta etapa.
- Probar login, JWT y CRUD completos a través de 8080 con los servicios reales.

No se modificaron los proyectos existentes, Angular ni datos; no hay despliegue,
descubrimiento de servicios ni servidor de configuración.
