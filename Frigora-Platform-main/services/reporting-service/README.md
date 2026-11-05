# Reporting Service

Proyecto Maven independiente: Spring Boot 4.0.6, Java 21, puerto 8085.
El monolito permanece en la raíz del repositorio, con su POM y rutas originales.
No hay POM agregador ni dependencia entre los dos proyectos.

```text
Frigora-Platform-main/
├── pom.xml, mvnw, mvnw.cmd, src/   # monolito sin mover
└── services/reporting-service/
    ├── pom.xml
    ├── README.md
    └── src/
        ├── main/java/com/frigora/frigoraplatform/
        │   ├── ReportingServiceApplication.java
        │   ├── reporting/         # copia inicial del dominio y API
        │   └── shared/infrastructure/
        │       ├── security/      # identidad neutral y seguridad local
        │       └── persistence/   # estrategia física copiada
        ├── main/resources/application.properties
        └── test/java/com/frigora/frigoraplatform/
            ├── ReportingServiceApplicationTests.java
            └── reporting/interfaces/rest/ReportAuthorizationTests.java
```

## Compilar y probar

Desde la raíz del repositorio, con `JAVA_HOME` apuntando al JDK:

```powershell
./mvnw.cmd -f services/reporting-service/pom.xml package
```

El wrapper existente solo ejecuta Maven; el servicio no hereda del POM monolítico.
También puede ejecutarse `mvn package` desde esta carpeta.
Se genera `target/reporting-service-0.0.1-SNAPSHOT.jar`.

Las pruebas arrancan la aplicación con servidor HTTP en puerto aleatorio y H2
en memoria, sin credenciales externas. Usan JWT firmados, JPA real y auditoría.
Las pruebas heredadas con MockMvc comprueban adicionalmente los contratos y dos propietarios.
H2 no valida compatibilidad con MySQL. No se utiliza Docker ni Testcontainers.
Las credenciales y el secreto ficticios de pruebas no son configuración de ejecución.

## Configuración en IntelliJ

1. Agregar este `pom.xml` como proyecto Maven adicional, sin sustituir el POM de la raíz.
2. Seleccionar JDK 21 y el módulo `reporting-service` como classpath de ejecución.
3. Crear una configuración Application para
   `com.frigora.frigoraplatform.ReportingServiceApplication`.
4. Usar esta carpeta como working directory y configurar las variables indicadas abajo.
5. Ejecutar solo después de crear la base independiente vacía y asignar sus permisos.

| Variable | Uso |
| --- | --- |
| `REPORTING_DB_HOST` | Host de MySQL, predeterminado `localhost` |
| `REPORTING_DB_PORT` | Puerto de MySQL, predeterminado `3306` |
| `REPORTING_DB_USERNAME` | Usuario exclusivo de la base independiente, obligatorio |
| `REPORTING_DB_PASSWORD` | Contraseña externa, obligatoria |
| `REPORTING_JWT_SECRET` | Clave HMAC del emisor IAM, suministrada externamente, obligatoria |

La base es **`frigora_reporting_db`**. Debe crearse vacía antes del primer arranque.
Flyway aplica `src/main/resources/db/migration/V1__create_reports.sql` y registra
la versión en `flyway_schema_history`; después Hibernate verifica el mapeo con
`ddl-auto=validate`. No hay copia ni migración de datos del monolito implementada.
Campos esperados: `id` entero autoincremental, `tenant_id`, `equipment_id`, `type`,
`title`, `status`, `summary`, `content` TEXT, `url`, `created_at`, `updated_at`.
Los enums se persisten por nombre Java, no por etiqueta REST.
La estrategia física conserva los nombres del monolito.

Usar un usuario MySQL cuyos permisos estén restringidos a `frigora_reporting_db`.
No reutilizar el esquema, credenciales ni la URL del monolito. No sobrescribir
`spring.datasource.url` para apuntar al almacenamiento antiguo: las dos aplicaciones
deben tener datos separados durante esta etapa. El monolito sigue siendo el escritor
de los datos actuales; el nuevo servicio solo puede escribir en su base independiente.

## Seguridad y contrato

Todas las rutas `/api/v1/reports` requieren `Authorization: Bearer <JWT>`.
El parser JJWT 0.12.6 verifica la firma HMAC con los bytes UTF-8 de la clave,
exactamente como IAM: **no se decodifica la clave como Base64**.
Se exige `exp` no vencido, `sub` no vacío y `userId` numérico entero positivo
compatible con `Integer`. Tokens antiguos sin `userId`, expirados, alterados o
con claims inválidos reciben 401. No se registran tokens ni claves.

No existen consultas de usuarios, contraseñas, `UserRepository`, `UserDetailsService`
ni referencias al código IAM. Se desactiva la autoconfiguración de usuarios locales.
La identidad implementa la interfaz neutral copiada en este proyecto; no hay enlaces
a archivos fuente del monolito. Las autoridades están vacías porque los tokens
actuales no contienen roles; la autorización implementada es por propietario.

Los DTOs, enums JSON y rutas se mantienen: POST 201, GET/PUT/DELETE 200;
recursos ausentes o ajenos 404. Los filtros por equipo también incluyen propietario.
Una actualización no puede cambiar `tenantId`.

La auditoría se activa únicamente en esta copia mediante `AuditingEntityListener`
y un `DateTimeProvider` UTC que devuelve `OffsetDateTime`, sin cambiar tipos SQL.

## Pendientes

- Ejecutar y validar la migración V1 contra MySQL local real y comparar el DDL original.
- Planificar copia de datos, corte de escrituras y reversión antes de redirigir tráfico real.
- Compartir la clave por un canal de secretos y planificar su rotación; HMAC mantiene
  una clave de firma compartida con el emisor hasta una futura migración a claves públicas.
- Definir enrutamiento del gateway, CORS y documentación OpenAPI de ejecución.
- Renovar sesiones antiguas que no tengan `userId`.

La creación de este proyecto no cambia la ruta de tráfico ni despliega un servicio.

## Preparar MySQL local (Etapa 3.1)

Ejecutar manualmente con una cuenta administradora de la instancia MySQL local
autorizada. Sustituir el marcador de contraseña; no guardar credenciales reales
en el repositorio. No ejecutar sobre la base del monolito.

```sql
CREATE DATABASE frigora_reporting_db CHARACTER SET utf8mb4;
CREATE USER 'reporting_app'@'localhost' IDENTIFIED BY '<contraseña-local-elegida>';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
    ON frigora_reporting_db.* TO 'reporting_app'@'localhost';
```

No es necesario crear `reports` manualmente. En IntelliJ, usar las variables de
la tabla anterior y ejecutar `ReportingServiceApplication` en el puerto 8085.
Alternativamente, desde la raíz, con las mismas variables externas:

```powershell
./mvnw.cmd -f services/reporting-service/pom.xml spring-boot:run
```

La dependencia `spring-boot-starter-flyway` integra la ejecución antes de JPA;
`flyway-mysql` añade soporte específico de MySQL. Las versiones se gestionan por
Spring Boot. No se necesitan URL ni credenciales separadas para Flyway: utiliza
el datasource exclusivo del servicio. Una cuenta de migraciones separada podrá
introducirse después si se quieren retirar permisos DDL a la cuenta de ejecución.

`baseline-on-migrate=false` impide adoptar automáticamente un esquema no vacío
sin historial; `clean-disabled=true` deshabilita la limpieza. Ante un esquema
inesperado, detenerse y revisar: no activar baseline, clean, repair ni borrar tablas
para eludir un fallo. Las migraciones aplicadas no se editan; cambios futuros usan V2, V3, etc.

V1 conserva `INT` con signo para los identificadores, `VARCHAR(255)` para textos
sin longitud explícita, `TEXT` para contenido y `DATETIME(6)` para auditoría UTC.
Los campos salvo la PK admiten NULL como el mapeo actual. Los ENUM contienen los
nombres Java de todos los valores actuales. Se añade un índice `(tenant_id, equipment_id)`
para listados por propietario y equipo; las consultas por ID utilizan la PK.
No hay claves foráneas hacia usuarios o equipos de otros servicios.
Las fechas las asigna Spring Data, sin defaults ni triggers SQL.

No se inspeccionó el DDL real del monolito. Quedan por contrastar versión MySQL,
tipos/longitudes/nulabilidad efectivos, valores y orden de ENUM, precisión temporal,
charset/collation, índices, defaults y restricciones existentes. Esta migración es
para una base nueva, no para modificar ni adoptar la tabla antigua.

Las pruebas HTTP H2 existentes mantienen Flyway desactivado y crean su esquema
en memoria. `ReportMigrationTests` ejecuta V1 con Flyway sobre otra H2 en modo MySQL,
comprueba historial, repetición sin cambios y rechazo de baseline implícito.
Es una comprobación local del script, no una validación de MySQL ni del DDL original.
