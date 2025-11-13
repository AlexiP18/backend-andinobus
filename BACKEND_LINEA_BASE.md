# Línea base del Backend

Este documento consolida el análisis y establece la línea base del backend del proyecto, alineado con:
- Analisis_Contexto_Proyecto_DAS.md
- ARCHITECTURE.md
- PLAN_DE_TRABAJO.md
- API.md

Su objetivo es dejar claro el stack, la estructura, convenciones, entregables del MVP e hitos inmediatos para continuar la implementación.

## 1) Stack y dependencias
- Java 21 + Spring Boot 3.5.x
- Starters actuales: Web, Validation, Devtools, Test, Lombok
- Próximos starters al habilitar base de datos: Data JPA y Flyway
- Base de datos objetivo: PostgreSQL 16 (dev)
- Herramientas: JUnit 5, MockMvc; (opcional) MapStruct; (futuro) Testcontainers y SonarQube

## 2) Dominios y límites (bounded contexts)
- Catálogos y Operaciones: Cooperativas, Buses, Frecuencias, Paradas, Hojas de ruta
- Ventas y Boletos: Disponibilidad, Reservas, Pagos, Boletos, Asientos, Tarifas
- Usuarios y Acceso: Clientes, Usuarios del sistema, Roles
- Notificaciones: Email/Push (futuro)
- Archivos/Media: Comprobantes e imágenes (futuro)
- Integraciones: Pagos (PayPal, transferencia)

## 3) Estructura por capas (por dominio)
- api: controladores HTTP y DTOs
- application: servicios/casos de uso (coordinación, transacciones)
- domain: entidades/agregados y repositorios (interfaces)
- infrastructure: adaptadores (JPA, mappers, integraciones)

Paquetes transversales:
- common: utilidades y errores
- config: configuración general (CORS, Jackson, i18n; seguridad más adelante)

## 4) Convenciones clave
- Los controladores exponen y consumen DTOs; no devuelven entidades JPA
- Validaciones mediante Jakarta Validation en DTOs
- Borrado lógico con campo activo (cuando aplique)
- Respuestas de error uniformes con GlobalExceptionHandler y ErrorResponse
- Paginación en listados
- Rutas HTTP limpias, versionado a futuro (por ahora sin prefijo)

## 5) Implementaciones ya incluidas
- Planificaciones (CSV): endpoints para previsualizar importación de ORIGEN/DESTINO/HORA SALIDA/LLEGADA (sin persistencia)
- Manejo de errores global:
  - common.errors.ErrorResponse
  - common.errors.GlobalExceptionHandler
  - common.errors.NotFoundException
- Documentación operativa: ARCHITECTURE.md, PLAN_DE_TRABAJO.md, API.md y análisis contextual

## 6) Próximas dependencias a añadir (cuando se configure la BD)
- spring-boot-starter-data-jpa
- org.flywaydb:flyway-core
- (opcional) MapStruct y su processor

## 7) Configuración de entornos
Archivos previstos en src/main/resources:
- application.properties (común)
- application-dev.properties (desarrollo)
- application-test.properties (pruebas)

Se incluye una plantilla de desarrollo: src/main/resources/application-dev.properties.example con los parámetros de conexión a PostgreSQL y Flyway. Al habilitar la BD:
- Copiar/renombrar a application-dev.properties
- Ajustar credenciales y URL
- Añadir las dependencias Data JPA y Flyway en pom.xml

## 8) Migraciones (Flyway)
Directorio: src/main/resources/db/migration/
- V1__init_catalogos.sql (propuesto): cooperativa, bus, frecuencia, parada; índices, FKs y restricciones (UNIQUE placa)
- Política: spring.jpa.hibernate.ddl-auto=validate; el esquema es gestionado por migraciones

## 9) Entidades del MVP (Iteración 1)
- Cooperativa(id, nombre, ruc, logoUrl, activo, createdAt, updatedAt)
- Bus(id, cooperativaId, numeroInterno, placa UNIQUE, chasisMarca, carroceriaMarca, fotoUrl, activo, createdAt, updatedAt)
- Frecuencia(id, cooperativaId, origen, destino, horaSalida, duracionEstimadaMin, diasOperacion, activa)
- Parada(id, frecuenciaId, ciudad, orden, horaEstimada)

## 10) API del MVP (Iteración 1)
- Cooperativas: POST/GET/GET{id}/PUT/DELETE (borrado lógico)
- Buses: POST /cooperativas/{id}/buses, GET /cooperativas/{id}/buses, GET /buses/{id}
- Frecuencias: POST /frecuencias, GET /frecuencias
- Paradas: POST /frecuencias/{id}/paradas
- Viajes (stub): GET /viajes?fecha=YYYY-MM-DD
- Planificaciones (CSV): ya disponible en /api/planificaciones

Ver API.md para ejemplos de requests y errores.

## 11) Seguridad (próxima iteración)
- JWT + roles (CLIENTE, OFICINISTA, COOPERATIVA, ADMIN)
- Endpoints públicos: /auth/** y los de lectura pública que se definan
- Filtros por cooperativaId (multi-tenant lógico)

## 12) Calidad y pruebas
- Unitarias: servicios de aplicación
- DataJpaTest: repositorios y restricciones
- Integración: MockMvc para al menos CooperativaController
- Observabilidad: logs estructurados; trazabilidad de pagos/boletos a futuro

## 13) Plan de implementación incremental
1) Añadir Data JPA + Flyway y configurar application-dev.properties (desde la plantilla)
2) Crear V1__init_catalogos.sql
3) Implementar entidades JPA + repositorios (catálogos)
4) DTOs + mappers + servicios + controladores (Cooperativa, luego Bus)
5) Pruebas de repositorios y controladores
6) Actualizar API.md con ejemplos reales y preparar colección Postman

## 14) Criterios de aceptación (MVP Iteración 1)
- CRUD Cooperativa y Bus operativos con validaciones y borrado lógico
- Migración V1 aplicada automáticamente con Flyway
- Pruebas básicas pasando (repos, controllers)
- Documentación (ARCHITECTURE, PLAN, API y esta línea base) actualizada

## 15) Notas de alineación con Planificaciones (CSV)
- El parser actual se conectará con Frecuencia/Hoja de Ruta en una siguiente iteración para generar frecuencias o viajes con base en ORIGEN/DESTINO/HORAS
- Se validarán reglas: preparación previa si hay hora de llegada previa a salida por motivos operativos

— Fin —
