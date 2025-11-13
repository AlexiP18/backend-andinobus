# Plan de trabajo inmediato (Estructura, componentes y actividades)

Este plan responde a: “¿cómo debo estructurarlo, qué componentes y actividades ya debo hacer?”
Se basa en el análisis previo y en la guía de arquitectura del archivo ARCHITECTURE.md.

## 1) Estructura del proyecto (qué crear ahora)
- Paquete base: `com.andinobus.backendsmartcode`
- Dominios iniciales (módulos lógicos):
  - `catalogos`: Cooperativas, Buses, Frecuencias, Paradas.
  - `usuarios`: Clientes y Usuarios del sistema (roles/permiso). [esqueleto]
  - `ventas`: Reservas/Boletos/Pagos. [esqueleto]
- Capas por dominio:
  - `api.controllers` (REST Controllers)
  - `api.dto` (DTOs request/response + validaciones)
  - `application.services` (casos de uso)
  - `domain.entities` (entidades JPA)
  - `domain.repositories` (interfaces)
  - `infrastructure.repositories` (JpaRepository)
- Transversal:
  - `common.errors` (manejo de errores global y modelos de error)
  - `config` (CORS, Jackson, profiles)

Ver ejemplos y convenciones en ARCHITECTURE.md.

## 2) Componentes mínimos del MVP (Iteración 1)
- Catálogos (CRUD):
  - Cooperativa
  - Bus
  - Frecuencia
  - Parada
- Generación de viajes desde frecuencia por fecha (stub de endpoint, sin lógica avanzada).
- Seguridad básica (opcional en esta iteración): estructura de roles y endpoints públicos/privados (puede quedar un stub para /auth).

## 3) Dependencias recomendadas (pom.xml)
- Ya presentes: spring-boot-starter-web, postgresql, lombok, spring-boot-starter-test, devtools.
- Agregar cuando se implementen las capas:
  - `spring-boot-starter-data-jpa` (repositorios JPA)
  - `spring-boot-starter-validation` (validaciones jakarta en DTOs)
  - `org.flywaydb:flyway-core` (migraciones de BD)
  - (Opcional) MapStruct para mapeo DTO↔Entidad: `org.mapstruct:mapstruct` + `mapstruct-processor`

## 4) Configuración (application-*.properties)
- `application.properties` (común)
- Crear `application-dev.properties` y `application-test.properties`.
  - Ejemplo dev:
    - `spring.datasource.url=jdbc:postgresql://localhost:5432/das_dev`
    - `spring.datasource.username=postgres`
    - `spring.datasource.password=postgres`
    - `spring.jpa.hibernate.ddl-auto=validate` (usar Flyway)
    - `spring.flyway.enabled=true`

## 5) Migraciones (Flyway)
- Crear carpeta: `src/main/resources/db/migration/`
- Archivo inicial: `V1__init_catalogos.sql` con tablas:
  - cooperativa(id, nombre, ruc, logo_url, activo, created_at, updated_at)
  - bus(id, cooperativa_id, numero_interno, placa UNIQUE, chasis_marca, carroceria_marca, foto_url, activo, created_at, updated_at)
  - frecuencia(id, cooperativa_id, origen, destino, hora_salida, duracion_estimada_min, dias_operacion, activa)
  - parada(id, frecuencia_id, ciudad, orden, hora_estimada)
  - índices básicos y FKs.

## 6) Entidades y repositorios (JPA)
- Entidades bajo `catalogos.domain.entities`:
  - Cooperativa, Bus, Frecuencia, Parada.
  - Atributos alineados a la migración.
  - Borrado lógico: `activo` (boolean) cuando aplique.
- Repositorios bajo `catalogos.domain.repositories` (interfaces) y `catalogos.infrastructure.repositories` (JpaRepository).

## 7) API (DTOs + Controllers)
- Rutas iniciales:
  - `POST /cooperativas` | `GET /cooperativas` | `GET /cooperativas/{id}` | `PUT /cooperativas/{id}` | `DELETE /cooperativas/{id}`
  - `POST /cooperativas/{id}/buses` | `GET /cooperativas/{id}/buses`
  - `POST /frecuencias` | `GET /frecuencias`
  - `POST /frecuencias/{id}/paradas`
- DTOs con validaciones (por ejemplo: `@NotBlank`, `@Size`, `@Email`).
- Respuestas paginadas cuando aplique.

## 8) Manejo de errores
- `@ControllerAdvice` global en `common.errors`.
- Estructura sugerida de error: timestamp, status, errorCode, message, details, path.

## 9) Pruebas
- Unitarias de servicios (application) y de repositorios (con DataJpaTest).
- Integración de controllers (SpringBootTest + MockMvc) para al menos un CRUD (Cooperativa).

## 10) Documentación operativa
- Mantener actualizados:
  - `ARCHITECTURE.md` (decisiones y estructura)
  - `API.md` (endpoints con ejemplos de request/response)
  - Este `PLAN_DE_TRABAJO.md` (checklist y avance)

## 11) Pipeline CI (cuando el CRUD básico funcione)
- GitHub Actions con jobs: build + test (y opcional SonarQube).
- Quality gate: compila y pasan pruebas.

## 12) Checklist por orden de ejecución
1. Ajustar `pom.xml` con `data-jpa`, `validation`, `flyway`. [ ]
2. Crear `application-dev.properties` con datasource y activar Flyway. [ ]
3. Crear `db/migration/V1__init_catalogos.sql`. [ ]
4. Modelar entidades JPA de catálogos. [ ]
5. Definir repositorios JPA. [ ]
6. Implementar DTOs + mappers (manual o MapStruct). [ ]
7. Implementar servicios de aplicación para CRUD. [ ]
8. Implementar controladores REST con validaciones. [ ]
9. Añadir `@ControllerAdvice` global de errores. [ ]
10. Pruebas: repositorios, servicios y controllers de Cooperativa. [ ]
11. Documentar `API.md` con ejemplos. [ ]
12. Configurar pipeline CI (build + test). [ ]

## 13) Criterios de aceptación (Iteración 1)
- CRUDs de Cooperativa y Bus operativos con validaciones, incluidos listados y búsqueda simple.
- Base de datos gestionada por Flyway con esquema inicial aplicado.
- Pruebas básicas pasando en CI local.
- Documentación actualizada (ARCHITECTURE, PLAN y API).