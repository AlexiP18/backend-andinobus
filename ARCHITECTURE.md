# Arquitectura y estructura del proyecto

Este documento guía cómo estructurar el backend y qué componentes crearemos. Está alineado con el análisis previo (Analisis_Contexto_Proyecto_DAS.md) y con un enfoque de monolito modular por dominios (bounded contexts) y capas.

## 1. Estilo arquitectónico
- Monolito modular con límites claros por dominio.
- Capas por cada dominio:
  - api: Controladores HTTP y DTOs de entrada/salida.
  - application: Casos de uso/servicios de aplicación (regras de orquestación y transacciones).
  - domain: Entidades, agregados, repositorios (interfaces) y lógica de dominio.
  - infrastructure: Adaptadores a tecnologías (JPA Repositories, mapeadores, almacenamiento, e integraciones externas).

## 2. Mapeo de paquetes
Base package: `com.andinobus.backendsmartcode`

Dominios iniciales:
- catalogos: Cooperativas, Buses, Frecuencias, Paradas, Hojas de ruta (operación).
- ventas: Disponibilidad, Reservas, Pagos, Boletos.
- usuarios: Clientes, Usuarios del sistema y Roles.
- notificaciones: Email/Push (más adelante).
- archivos: Comprobantes e imágenes (más adelante).
- integraciones: Pagos (PayPal/IPN) u otros adaptadores.

Estructura de paquetes por dominio (ejemplo con catalogos):
```
com.andinobus.backendsmartcode.catalogos
 ├─ api
 │   ├─ controllers
 │   ├─ dto
 │   └─ mappers
 ├─ application
 │   └─ services
 ├─ domain
 │   ├─ entities
 │   ├─ repositories   (interfaces)
 │   └─ services       (lógica de dominio si aplica)
 └─ infrastructure
     ├─ repositories   (implementaciones JPA)
     ├─ mappers
     └─ config
```

Paquetes transversales:
- `common`: utilidades, excepciones, respuesta de errores, paginación, Result wrappers.
- `config`: configuración general (CORS, Jackson, i18n). Si en el futuro se usa Spring Security, crear `security` bajo `config`.

## 3. Convenciones
- Controladores retornan DTOs, nunca entidades JPA.
- Validaciones con Jakarta Validation (anotaciones en DTOs) y reglas de negocio en servicios de dominio/aplicación.
- Mapeo DTO↔Entidad con MapStruct (o manual inicialmente si se prefiere reducir dependencias).
- Repositorios: interfaces en `domain.repositories`, implementaciones JPA en `infrastructure.repositories`.
- Manejo de errores: `@ControllerAdvice` global en `common.errors` con formato consistente (timestamp, path, code, message, details).
- Transacciones: `@Transactional` en casos de uso a nivel de servicio de aplicación.
- Borrado lógico donde aplique (ej.: activo=true/false) en entidades críticas.

## 4. Seguridad (fase siguiente)
- Roles: CLIENTE, OFICINISTA, COOPERATIVA, ADMIN.
- Autenticación con JWT (Spring Security). Endpoints públicos para `/auth/**` y lectura pública limitada si aplica.

## 5. Persistencia (sugerido)
- PostgreSQL.
- Migraciones con Flyway (`src/main/resources/db/migration`).
- Índices por claves de búsqueda (placa, frecuencia, cooperativaId, etc.).

## 6. Módulos MVP Iteración 1 (back-end)
- CRUD Catalogos: Cooperativa, Bus, Frecuencia y Parada.
- Generación de Viajes desde Frecuencia por fecha (stub inicial).
- Esqueleto de autenticación (login/register de cliente) — opcional en esta iteración si se prioriza CRUD.

## 7. Endpoint Sketch (Catálogos)
- `POST /cooperativas` | `GET /cooperativas` | `GET /cooperativas/{id}` | `PUT /cooperativas/{id}` | `DELETE /cooperativas/{id}` (borrado lógico)
- `POST /cooperativas/{id}/buses` | `GET /cooperativas/{id}/buses`
- `POST /frecuencias` | `GET /frecuencias` | `POST /frecuencias/{id}/paradas`
- `GET /viajes?fecha=YYYY-MM-DD` (generación/consulta stub)

## 8. Actividades de implementación (resumen)
Ver también PLAN_DE_TRABAJO.md para el detalle y checklist. Resumen:
1) Crear entidades JPA y repositorios para catálogos.
2) Crear DTOs, mapeos y controladores con validaciones.
3) Añadir manejo de errores global y convenciones de respuesta.
4) Crear migraciones Flyway iniciales y configuración de DB con perfiles (dev/test).
5) Pruebas unitarias/integración básicas.

## 9. Observabilidad y calidad
- Logs estructurados con contexto (requestId).
- Pruebas con Spring Boot Test y Testcontainers (opcional) para PostgreSQL.
- Integrar SonarQube/Quality Gate (en CI) en fases siguientes.

## 10. Cómo extender
- Añadir dominios (ventas, integraciones) repitiendo el mismo patrón de capas.
- Extraer módulos si el dominio crece (Gradle/Maven multi-módulo) en el futuro.
