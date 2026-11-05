# Etapa 4.1: Gateway y aplicaciones reales

## Estado de la comprobación

Se inspeccionaron los controllers, las configuraciones de seguridad/CORS y el datasource
del monolito. Su datasource predeterminado es remoto y usa `ddl-auto=update`.
Se encontró el servicio Windows `MySQL80` activo, pero ninguna conexión local con
credenciales explícitamente autorizadas disponible para esta sesión.
No se arrancó el monolito con el datasource remoto ni se ejecutaron operaciones SQL.
No se validó la comunicación Gateway → monolito real ni Gateway → Reporting real.

Comprobación del entorno en esta etapa: 8080, 8081 y 8085 sin listeners; 3306 con
un listener. No hay conflicto observado entre los tres puertos de aplicación,
pero ninguna de las tres aplicaciones estaba ejecutándose en esos puertos.
Resultado automatizado: 9 pruebas del Gateway y 19 pruebas seleccionadas del
monolito aprobadas, sin fallos ni omisiones. No se ejecutó ninguna prueba MySQL.

La suite del Gateway usa el Gateway real con destinos HTTP temporales. Las pruebas
de IAM ejecutadas por separado usan repositorios simulados; no validan un login real
contra MySQL. Estos resultados no constituyen una integración del sistema completo.

## Rutas reales inspeccionadas

Todas las rutas de la tabla llevan el prefijo `/api/v1`.

| Contexto | Controller | Métodos y rutas |
| --- | --- | --- |
| IAM | AuthenticationController | POST `/authentication/sign-in`, `/authentication/sign-up` |
| IAM | UsersController | GET `/users`, `/users/{id}`, `/users/role/{role}` |
| Activos | SiteController | POST/GET `/sites`; GET `/sites/{id}`, `/sites/by-owner/{ownerId}`; DELETE `/sites/{id}` |
| Monitoreo | EquipmentController | POST/GET `/equipment`; GET `/equipment/{id}`, `/equipment/by-owner/{ownerId}`; DELETE `/equipment/{id}` |
| Monitoreo | AlertController | POST/GET `/alert`; GET `/alert/{id}`; PATCH `/alert/{id}/acknowledge`; DELETE `/alert/{id}` |
| Solicitudes | ServiceRequestsController | POST `/service-requests`; GET `/service-requests/{id}`, `/service-requests/requester/{requesterId}`, `/service-requests/provider/{providerId}`; DELETE `/service-requests/{id}` |
| Solicitudes | ServiceRequestsController | PATCH `/service-requests/{id}/accept`, `/reject`, `/cancel`, `/assign-technician`, `/complete` |
| Solicitudes | InterventionsController | POST `/interventions`; GET `/interventions/{id}`, `/interventions/serviceRequest/{serviceRequestId}` |
| Técnicos | TechniciansController | POST/GET `/technicians`; GET/PUT/DELETE `/technicians/{id}` |
| Feedback | ReviewsController | POST/GET `/reviews`; GET `/reviews/{id}`, `/reviews/service-request/{serviceRequestId}` |
| Reporting | ReportController | POST/GET `/reports`; GET/PUT/DELETE `/reports/{id}` |

Filtros reales: `/alert?equipmentId=...`, `/technicians?providerId=...`,
`/service-requests/provider/{providerId}?status=...` y `/reports?equipmentId=...`.
El sufijo `serviceRequest` de intervenciones conserva su mayúscula y `/alert` es singular.

El Gateway envía exclusivamente `/api/v1/reports` y descendientes a 8085.
Todas las otras rutas `/api/**` van a 8081 sin eliminar prefijos ni modificar JSON.
Reporting sigue existiendo en el monolito, pero no se usa como fallback.

## Configuraciones exactas para IntelliJ

Importar los tres POM como proyectos Maven independientes; seleccionar JDK 21.
No combinar sus classpaths ni usar el módulo monolítico para ejecutar los servicios.

### Monolito

- Clase: `com.frigora.frigoraplatform.FrigoraPlatformApplication`.
- Classpath: módulo de la raíz; working directory: raíz del repositorio.
- Variables obligatorias para este ensayo local:

```text
PORT=8081
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/<base-local-de-pruebas-autorizada>?serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<usuario-local-autorizado>
SPRING_DATASOURCE_PASSWORD=<contraseña-real-autorizada>
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
AUTHORIZATION_JWT_SECRET=<clave-de-pruebas-compartida-con-reporting>
```

Los marcadores deben reemplazarse con valores reales del entorno de pruebas: no son
credenciales ejecutables. La base local debe existir y contener el esquema completo
compatible y un usuario de prueba provisionado con contraseña BCrypt válida.
No habilitar `update` para resolver errores de esquema durante esta comprobación.
No ejecutar con la configuración remota por defecto.

### Reporting Service

- Clase: `com.frigora.frigoraplatform.ReportingServiceApplication`.
- Classpath: `reporting-service`; working directory: `services/reporting-service`.
- Puerto existente: 8085.
- Variables: `REPORTING_DB_HOST=localhost`, `REPORTING_DB_PORT=3306`,
  `REPORTING_DB_USERNAME`, `REPORTING_DB_PASSWORD`, `REPORTING_JWT_SECRET`.
- Usar su base independiente `frigora_reporting_db` y la misma clave HMAC de pruebas
  del monolito. Flyway prepara solo esa base según la etapa 3.1.

### Gateway

- Clase: `com.frigora.gateway.ApiGatewayApplication`.
- Classpath: `api-gateway`; working directory: `services/api-gateway`.
- Puerto: 8080; variables explícitas recomendadas:

```text
MONOLITH_URL=http://localhost:8081
REPORTING_SERVICE_URL=http://localhost:8085
ANGULAR_ORIGIN=http://localhost:4200
```

Antes del arranque comprobar que los puertos están libres; después deben pertenecer
a tres procesos Java distintos:

```powershell
Get-NetTCPConnection -State Listen |
    Where-Object LocalPort -In 8080,8081,8085 |
    Select-Object LocalAddress,LocalPort,OwningProcess
```

Arrancar primero monolito y Reporting; confirmar sus logs sin publicar secretos.
Después iniciar Gateway. No ejecutar dos instancias de la misma configuración.

## Comprobación manual pendiente (solo datos de prueba autorizados)

1. POST de login al monolito directo en `http://localhost:8081/api/v1/authentication/sign-in`
   y a través de `http://localhost:8080/api/v1/authentication/sign-in`, usando el mismo
   JSON real `{ "username": "...", "password": "..." }` de un usuario de pruebas.
   Comprobar el contrato `id`, `username`, `token`, `role`; no exigir igualdad literal
   de los JWT emitidos ni registrarlos en consola/archivos.
2. Con el token obtenido, comparar GET directo y vía Gateway para `/api/v1/users/{id}`
   y los listados de cada contexto de la tabla. Usar únicamente IDs de fixtures conocidos.
   Comparar status y JSON. Validar filtros con IDs reales de prueba.
3. Repetir sin Authorization y con token inválido: comprobar el 401 real del monolito
   y su propagación. Un token correcto para un ID inexistente puede dar 404 según el controller.
4. Probar CORS desde el origen permitido y uno no permitido. El origen rechazado debe
   dar 403 en el Gateway; distinguirlo de un 403 emitido por el monolito. No hay una
   regla general por rol en su SecurityFilterChain: no asumir que un rol diferente da 403.
5. GET de reportes: comparar 8085 directo frente a 8080, con tokens nuevos que tengan
   `userId`. Comprobar aislamiento y 404 para reportes ajenos en la base independiente.
   Un token antiguo sin `userId` debe seguir autenticando el monolito, pero Reporting devuelve 401.
6. No ejecutar POST de negocio, PUT, PATCH ni DELETE contra datos existentes para este
   ensayo. Su transporte se verifica automáticamente; un ensayo real de escrituras
   requiere fixtures y una autorización específica sobre una base desechable.

Login no crea datos de negocio, pero requiere credenciales existentes autorizadas.
No crear cuentas ni modificar las bases para fabricar un resultado de integración.

## Seguridad y CORS observados

El monolito conserva CSRF deshabilitado y sesiones stateless. Login/registro son públicos;
las otras rutas requieren autenticación. El filtro valida el JWT, carga al usuario por
username y conserva la identidad neutral. Esta etapa no cambia ese mecanismo.

Su CORS permite `http://localhost:4200` y `https://frigora-frontend.vercel.app`,
GET/POST/PUT/PATCH/DELETE/OPTIONS, cualquier encabezado y credenciales. El Gateway
actual admite únicamente el origen local configurado, y Authorization/Content-Type/Accept.
La deduplicación evita repetir los encabezados CORS de ambos procesos.
Si Angular usa un origen distinto, ajustar y coordinar las configuraciones antes de conectarlo.

## Pruebas reproducibles sin MySQL

Desde la raíz, con `JAVA_HOME` configurado:

```powershell
./mvnw.cmd -f services/api-gateway/pom.xml package
./mvnw.cmd '-Dtest=ReportAuthorizationTests,TokenServiceImplTests,AuthenticatedIdentityTests' test
```

No ejecutar la suite completa del monolito como sustituto de estos selectores:
`contextLoads` utiliza su configuración principal, y existen pruebas Testcontainers
de etapas anteriores que no forman parte de esta etapa.

Pendiente: disponer de configuración MySQL local autorizada, arrancar las aplicaciones
y realizar el ensayo real antes de cambiar la URL base de Angular.
