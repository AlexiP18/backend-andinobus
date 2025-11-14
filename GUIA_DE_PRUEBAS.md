# Guía de pruebas (rápidas) del backend

Esta guía te explica, paso a paso, cómo probar lo que ya está implementado en el proyecto, tanto sin base de datos (perfil por defecto) como con base de datos (perfil dev). RECOMENDADO: usar la colección de Postman incluida. Los comandos PowerShell/`curl` quedan como alternativa opcional.

Base URL local: http://localhost:8080

---

## 1) Requisitos previos
- Java 21 instalado (verifica con: `java -version`).
- Maven 3.9+ (verifica con: `mvn -version`).
- (Opcional para perfil dev) PostgreSQL 16 o compatible en local.

Comprobación rápida del build:
- `mvn -q -DskipTests=false test` (debería pasar las pruebas incluidas)

---

## 2) Arrancar sin base de datos (perfil por defecto)
Este modo deja operativos los endpoints de Planificaciones (CSV) sin exigir datasource.

1. Inicia la aplicación:
   - `mvn spring-boot:run`
   - Espera a ver en consola: `Started BackendSmartcodeApplication` y que el puerto sea 8080.

2. Probar endpoint de ejemplos CSV:
   - Navegador: http://localhost:8080/api/planificaciones/examples
   - PowerShell:
     - `iwr http://localhost:8080/api/planificaciones/examples`
   - `curl` estándar:
     - `curl http://localhost:8080/api/planificaciones/examples`

3. Subir un CSV propio (o los incluidos en la raíz del repo):
   - Archivos de ejemplo: `Horas de Trabajo RUTAS 1.csv` y `Horas de Trabajo RUTAS 2.csv`.
   - PowerShell (alias `curl` de Invoke-WebRequest):
     - `curl -Method POST -Uri http://localhost:8080/api/planificaciones/preview -Form @{ file = Get-Item ."Horas de Trabajo RUTAS 1.csv" }`
   - `curl` estándar:
     - `curl -X POST -F "file=@Horas de Trabajo RUTAS 1.csv" http://localhost:8080/api/planificaciones/preview`

Deberías obtener un JSON con `items`, `warnings` y `sourceName`.

Para detener la app: `Ctrl + C` en la consola donde se ejecuta.

---

## 3) Arrancar con base de datos (perfil dev) — requiere PostgreSQL
Este modo habilita JPA y Flyway, y expone los endpoints de catálogos (Cooperativas y Buses).

1. Configura PostgreSQL:
   - Crea una base de datos (por defecto usamos `das_dev`). Puedes usar pgAdmin o `psql`:
     - `psql -U postgres -c "CREATE DATABASE das_dev;"` (opcional si ya existe)
   - Verifica credenciales en `src/main/resources/application-dev.properties` (ya incluido). Valores por defecto:
     - `spring.datasource.url=jdbc:postgresql://localhost:5432/das_dev`
     - `spring.datasource.username=postgres`
     - `spring.datasource.password=postgres`
   - Si necesitas cambiarlos, edita ese archivo.

2. Inicia la app en perfil `dev`:
   - `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
   - Flyway aplicará automáticamente `V1__init_catalogos.sql`.

3. Probar endpoints de Cooperativas (JSON). Ejemplos:
   - Crear una cooperativa (PowerShell):
     - `$body = @{ nombre = "Cooperativa Amazonas"; ruc = "1790012345001"; logoUrl = $null; activo = $true } | ConvertTo-Json`
     - `iwr -Method POST -Uri http://localhost:8080/cooperativas -ContentType "application/json" -Body $body`
   - Crear (curl estándar):
     - `curl -X POST http://localhost:8080/cooperativas -H "Content-Type: application/json" -d '{"nombre":"Cooperativa Amazonas","ruc":"1790012345001","logoUrl":null,"activo":true}'`
   - Listar cooperativas (paginado):
     - `curl "http://localhost:8080/cooperativas?search=&page=0&size=20"`
   - Obtener por id (ejemplo id=1):
     - `curl http://localhost:8080/cooperativas/1`
   - Actualizar (PUT):
     - `curl -X PUT http://localhost:8080/cooperativas/1 -H "Content-Type: application/json" -d '{"nombre":"Coop Amazonas Actualizada","ruc":"1790012345001","logoUrl":null,"activo":true}'`
   - Borrado lógico (DELETE):
     - `curl -X DELETE http://localhost:8080/cooperativas/1`

4. Probar endpoints de Buses:
   - Crear bus para la cooperativa 1:
     - `curl -X POST http://localhost:8080/cooperativas/1/buses -H "Content-Type: application/json" -d '{"numeroInterno":"10","placa":"ABC-1234","chasisMarca":"Volvo","carroceriaMarca":"Marcopolo","fotoUrl":null,"activo":true}'`
   - Listar buses de la cooperativa 1 (paginado):
     - `curl "http://localhost:8080/cooperativas/1/buses?page=0&size=20"`
   - Obtener bus por id (ejemplo id=1):
     - `curl http://localhost:8080/buses/1`

5. Probar endpoints de Frecuencias:
   - Crear frecuencia para la cooperativa 1:
     - `curl -X POST http://localhost:8080/cooperativas/1/frecuencias -H "Content-Type: application/json" -d '{"origen":"Quito","destino":"Loja","horaSalida":"18:00","duracionEstimadaMin":720,"diasOperacion":"LUN,MAR,MIER,JUE,VIER,SAB,DOM","activa":true}'`
   - Listar frecuencias de la cooperativa 1 (paginado y búsqueda opcional):
     - `curl "http://localhost:8080/cooperativas/1/frecuencias?search=&page=0&size=20"`
   - Obtener frecuencia por id (ejemplo id=1):
     - `curl http://localhost:8080/frecuencias/1`
   - Actualizar frecuencia (cambiar hora de salida):
     - `curl -X PUT http://localhost:8080/frecuencias/1 -H "Content-Type: application/json" -d '{"horaSalida":"19:30"}'`
   - Borrado lógico de frecuencia:
     - `curl -X DELETE http://localhost:8080/frecuencias/1`

Notas:
- Si algún endpoint devuelve 404 y estás en perfil por defecto, recuerda que los endpoints de catálogos están activos en perfil `dev`.
- El manejo global de errores devuelve estructura uniforme (ver `API.md`). Ejemplo de error de validación:
```
{
  "timestamp": "2025-11-12T14:00:01Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request inválido",
  "details": [ { "field": "nombre", "message": "no debe estar vacío" } ],
  "path": "/cooperativas"
}
```

---

## 4) Ejecutar las pruebas incluidas
- `mvn -q -DskipTests=false test`
- Debe correr `CsvPlanificacionParserTest` y `BackendSmartcodeApplicationTests`.

---

## 5) Problemas comunes y soluciones
- Puerto en uso (8080):
  - Cambia el puerto en `application-dev.properties` con `server.port=8081` (u otro), o cierra el proceso que ocupa 8080.
- Error de conexión a base de datos:
  - Verifica URL/usuario/clave en `application-dev.properties`.
  - Asegura que PostgreSQL está iniciado y que la base `das_dev` existe.
- 404 en endpoints de catálogos:
  - Asegúrate de arrancar la app con `-Dspring-boot.run.profiles=dev`.

---

## 6) Herramientas sugeridas
- Postman/Insomnia para organizar las llamadas.
- pgAdmin para inspeccionar la base de datos.

---

## 7) Referencias
- `API.md`: listado de endpoints y ejemplos.
- `ARCHITECTURE.md`, `BACKEND_LINEA_BASE.md`, `PLAN_DE_TRABAJO.md` para contexto y siguientes pasos.


---

## 0) Probar con Postman (recomendado)
Sigue estos pasos para ejecutar todas las pruebas desde Postman en lugar de PowerShell/curl:

1. Importa en Postman los archivos del directorio `postman/` del repositorio:
   - Colección: `postman/BackendSmartcode.postman_collection.json`
   - Environment: `postman/Local.postman_environment.json`
2. Selecciona en Postman el environment "Local" (define `{{baseUrl}} = http://localhost:8080`).
3. Arranca la aplicación según lo que quieras probar:
   - Solo Planificaciones (CSV), sin base de datos: `mvn spring-boot:run`.
   - Catálogos (Cooperativas, Buses, Frecuencias), con base de datos: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
4. Ejecuta las requests de la colección en este orden sugerido:
   - Cooperativas → "POST crear cooperativa" (guarda `{{coopId}}` automáticamente).
   - Buses → "POST crear bus (coopId)" (usa `{{coopId}}`, guarda `{{busId}}`).
   - Frecuencias → "POST crear frecuencia (coopId)" (usa `{{coopId}}`, guarda `{{frecuenciaId}}`).
   - Luego puedes listar y obtener por id usando las variables guardadas.
   - Para Planificaciones (CSV):
     - Usa "GET ejemplos CSV" o
     - "POST previsualizar CSV" y selecciona un archivo en el campo `file` (por ejemplo, `Horas de Trabajo RUTAS 1.csv`).
5. Verifica los tests en Postman: la colección incluye pruebas simples de estado (201/204) y setea variables (`coopId`, `busId`, `frecuenciaId`) automáticamente a partir de las respuestas.

Notas y resolución de problemas:
- Si ves 404 en catálogos, asegúrate de estar corriendo el perfil `dev` y de que PostgreSQL esté configurado (ver `src/main/resources/application-dev.properties`).
- Si falla la conexión a BD, revisa URL/usuario/clave y que la base `das_dev` exista.
- Si tu PostgreSQL no usa el puerto 5432, ajusta `spring.datasource.url`.

---

## 2.5) Levantar PostgreSQL con Docker Compose (opción rápida)
Si no tienes PostgreSQL instalado localmente, puedes usar el archivo docker-compose.yml incluido en la raíz del proyecto.

1) Requisitos previos
- Docker Desktop instalado y en ejecución.

2) Arrancar PostgreSQL
- PowerShell (en la carpeta del proyecto):
  - docker compose up -d
- Esto levantará un contenedor postgres:16-alpine con:
  - DB: das_dev
  - Usuario: postgres
  - Clave: postgres
  - Puerto: 5432

3) Verificar estado (opcional)
- docker ps (deberías ver andinobus-postgres)
- docker logs -f andinobus-postgres (hasta ver que está listo para aceptar conexiones)

4) Detener o reiniciar
- Detener: docker compose down
- Reiniciar limpio (borra datos):
  - docker compose down -v
  - docker compose up -d

5) Siguiente paso
- Con el contenedor arriba, sigue con la sección "3) Arrancar con base de datos (perfil dev)" y ejecuta la app con:
  - mvn spring-boot:run -Dspring-boot.run.profiles=dev
- Flyway aplicará automáticamente V1__init_catalogos.sql y V2__usuarios_ventas_operacion_embarque.sql.
