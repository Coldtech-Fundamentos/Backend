# Pruebas JPA de Reporting con MySQL

Requisitos: JDK 21 o superior, Docker con motor Linux activo y acceso a la imagen
`mysql:8.4`. Desde la raíz del proyecto:

```powershell
./mvnw.cmd -Dtest=ReportMySqlIntegrationTests test
```

Si Java no está en PATH, configura `JAVA_HOME` hacia tu JDK antes de ejecutar.
La primera ejecución necesita descargar las dependencias de pruebas y la imagen.

La suite se omite si Docker no está disponible. Un resultado con pruebas omitidas
no valida MySQL. Si Docker está disponible pero falla el arranque de MySQL, la suite
debe fallar y se debe revisar el error; no se sustituye por otra base de datos.

## Aislamiento

- Solo utiliza un contenedor MySQL desechable y sus credenciales temporales.
- No carga `application.properties`, la aplicación principal ni repositorios de otros módulos.
- No admite una URL externa como alternativa ni consulta credenciales del proyecto.
- Hibernate crea y elimina el esquema exclusivamente dentro de ese contenedor.
- Cada prueba utiliza una transacción con rollback; fuerza flush y limpia el contexto
  para comprobar lecturas reales de la base, no solo entidades en memoria.
- Usa la estrategia física de nombres actual del proyecto, los adaptadores y los servicios reales.

## Alcance y limitaciones

Comprueba CRUD, las consultas por propietario y equipo, la inmutabilidad del propietario
durante actualizaciones y los nombres físicos/valores de enums mediante SQL nativo.
Los IDs del modelo y del repositorio de Reporting son `Integer`; los enums se guardan
como sus nombres Java (`INSPECTION`, `IN_PROGRESS`), no sus etiquetas JSON.

El esquema de pruebas se genera a partir del modelo. Esto no demuestra que una base
existente tenga ese mismo DDL: antes de migrar deben compararse columnas, nulabilidad,
índices y tipos con un volcado de esquema autorizado.

`ReportAudit` tiene anotaciones Spring Data, pero `Report` no declara
`AuditingEntityListener`. La prueba `documentsCurrentMissingAutomaticAuditListener`
documenta fechas automáticas ausentes; no presenta esa limitación como auditoría funcional.
Otra prueba comprueba el mapeo de fechas asignadas explícitamente y `created_at` no actualizable.
No se cambian entidades ni se corrige automáticamente esta limitación de auditoría.
