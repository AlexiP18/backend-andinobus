# Análisis contextual del proyecto (DAS)

Este documento sintetiza y estructura el contexto del proyecto a partir del archivo ProyectoDAS.md, destacando objetivos, alcance, reglas del negocio, requerimientos funcionales, riesgos y una propuesta técnica inicial para el backend.

— Última actualización: 2025-11-12

## 1) Origen y objetivo
- Referencia: líneas 5–8.
- Objetivo: desarrollar una solución web y móvil aplicando principios de Desarrollo Asistido por Software, integrando herramientas que mejoren calidad, productividad y colaboración durante todo el ciclo de vida.

## 2) Indicaciones y metodología de trabajo
- Referencia: líneas 11–22.
- Equipo: designar líder, asignación equitativa, seguimiento y reuniones.
- Metodología ágil: Scrum o Kanban con evidencia de iteraciones, backlog y ceremonias.
- Herramientas sugeridas (y propósito):
  - Gestión: Jira, Trello, GitHub Projects.
  - Código: Git + GitHub/GitLab; PRs, code review.
  - Colaboración: Slack/Discord/Teams.
  - Documentación: Notion/Confluence/Google Docs.
  - Calidad/pruebas: Postman, Selenium, SonarQube.
- Evaluación: uso e integración efectiva de las herramientas; revisiones periódicas en clase.

## 3) Caso propuesto y alcance del producto
- Referencia: línea 26.
- Producto: sistema integral para gestión y venta de pasajes de transporte interprovincial en Ecuador, con:
  - Plataforma web (SaaS) para administración y ventas.
  - Aplicación móvil para usuarios (compra y gestión de boletos).
  - Aplicación móvil para personal de bus (control de acceso/embarque).

## 4) Requerimientos funcionales de alto nivel
- Web (SaaS): referencias líneas 34–43.
  - CRUD de cooperativas, frecuencias, buses, etc.
  - Roles: usuario de cooperativa, oficinista, usuario final.
  - Gestión y venta de boletos (oficinista y usuario final).
  - Configuración de marca (logo, colores, redes, soporte).
- Móviles: referencias líneas 46–56.
  - Usuarios: búsqueda de destinos/frecuencias, info del viaje, compra con código (QR/barcode/Wallet), historial.
  - Personal de bus: escaneo de código para registrar acceso.

## 5) Reglas de negocio clave
- Referencias: líneas 59–68.
- Múltiples cooperativas, cada una con frecuencias asignadas por ANT.
- Frecuencia = origen, destino, hora; activa/inactiva; puede tener paradas intermedias.
- Cooperativa selecciona qué frecuencias usa en su hoja de ruta; relación con cantidad de buses; días de parada.
- Hoja de trabajo se genera manual o automática según frecuencias habilitadas.
- Configuraciones de asientos por bus (ej.: Normal, VIP).
- Venta debe considerar tipo y cantidad de asientos, descuentos (menor, discapacidad, tercera edad). En móvil, validar por cédula.
- Permitir compra entre paradas intermedias.

## 6) Funcionalidades de la venta y filtros
- Referencias: líneas 70–94.
- Búsqueda/selección de rutas; filtros por cooperativa, tipo de asiento, marca de chasis/carrocería, tipo de viaje.
- Ver ficha técnica del bus (cooperativa, número, placa, chasis, carrocería, foto).
- Selección de asientos por disponibilidad y tipo.
- Pagos: depósito/transferencia (con carga de comprobante para validación manual), PayPal; opcional tarjeta/cripto.
- Generación/descarga visible del boleto en cualquier momento; opción de comprar otro al finalizar.
- Notificaciones por email o in-app en pasos relevantes.

## 7) Integraciones externas y referencias
- Referencias PayPal: líneas 98–105.
- IPN/WEBHOOKS de PayPal Sandbox para validar pagos.
- Información referencial de buses: https://www.facebook.com/ecuabus

## 8) Propuesta técnica (backend)
- Stack sugerido (acorde al repo actual):
  - Java 21 + Spring Boot 3.5.x (REST, Security, Validation, Data JPA).
  - Base de datos: PostgreSQL 16 (o MySQL/MariaDB si preferido).
  - Mensajería/colas (opcional en etapas avanzadas): RabbitMQ para eventos de pago y notificaciones.
  - Storage: S3/GCS o almacenamiento local para comprobantes y fotos de buses.
  - Email: SMTP (SendGrid/Mailgun) o servidor institucional.
  - Generación de QR: librería ZXing.
- Estilo de arquitectura:
  - Monolito modular con módulos/límites (bounded contexts) para fase inicial; posible evolución a microservicios.
  - Capas: API (controllers) → Aplicación (use-cases) → Dominio (entidades/servicios) → Infraestructura (repositorios/adapters).
- Bounded contexts sugeridos:
  1) Catálogos y Operaciones: Cooperativas, Buses, Frecuencias, Paradas, Hojas de ruta.
  2) Ventas y Boletos: Disponibilidad, Reservas, Pagos, Boletos, Asientos, Precios y descuentos.
  3) Usuarios y Acceso: Clientes, Oficinistas, Usuarios de Cooperativa, Roles/Permisos.
  4) Notificaciones: Email/push.
  5) Archivos: comprobantes, imágenes.
  6) Integración Pagos: PayPal (IPN/webhook), Depósitos manuales.

## 9) Modelo de dominio (borrador)
- Entidades principales y relaciones:
  - Cooperativa(id, nombre, ruc, logoUrl, configuraciónMarca, estado)
  - Bus(id, cooperativaId, numeroInterno, placa, chasisMarca, carroceriaMarca, fotoUrl, estado)
  - ConfiguracionAsientos(id, busId, tipoAsiento[Normal/VIP], filas, columnas, mapaAsientos)
  - Frecuencia(id, cooperativaId, origen, destino, horaSalida, duracionEstimada, diasOperacion, activa)
  - Parada(id, frecuenciaId, ciudad, orden, horaEstimada)
  - HojaRuta(id, fecha, cooperativaId, generacion[manual/auto]) y HojaRutaItem(id, hojaRutaId, frecuenciaId, busId)
  - Viaje(id, fechaSalida, frecuenciaId, busId, estado[programado/en_curso/finalizado/cancelado])
  - Asiento(id, busId, numero, tipo, estado)
  - Tarifa(id, frecuenciaId o tramoId, tipoAsiento, precioBase, moneda)
  - Cliente(id, nombres, apellidos, cedula, email, telefono)
  - Reserva(id, viajeId, clienteId, tramoOrigen, tramoDestino, asientosSeleccionados, estado[pendiente/pagado/caducado/cancelado], fechaExpira)
  - Pago(id, reservaId, metodo[transferencia/paypal/tarjeta], estado, monto, moneda, comprobanteUrl, referenciaExterna, fecha)
  - Boleto(id, reservaId, codigo, qrUrl, fechaEmision, estado[emitido/usado/anulado])
  - UsuarioSistema(id, email, passwordHash, rol[COOPERATIVA, OFICINISTA, CLIENTE], cooperativaId?, enabled)

- Notas de negocio:
  - Tramos intermedios: representar tramos (Parada→Parada) para disponibilidad y tarifas intermedias.
  - Disponibilidad de asientos: calcular por viaje y tipo de asiento, evitando sobreventa.
  - Descuentos: reglas por categoría (menor, discapacidad, tercera edad) con validación por cédula (API de validación futura o heurística local para demo).

## 10) API REST (borrador de endpoints)
- Autenticación y usuarios
  - POST /auth/login
  - POST /auth/register (cliente)
  - GET /users/me
- Cooperativas y buses
  - CRUD /cooperativas
  - CRUD /cooperativas/{id}/buses
  - GET /buses/{id}
- Frecuencias y paradas
  - CRUD /frecuencias
  - CRUD /frecuencias/{id}/paradas
  - POST /hojas-ruta/generar (manual/auto)
- Búsqueda de rutas y disponibilidad
  - GET /rutas/buscar?origen=&destino=&fecha=&cooperativa=&tipoAsiento=&tipoViaje=
  - GET /viajes/{id}/disponibilidad
  - GET /viajes/{id}/bus
- Ventas y pagos
  - POST /reservas (selección de asientos, tramos)
  - POST /pagos/transferencia (subir comprobante)
  - POST /pagos/paypal/webhook (IPN/Webhook)
  - GET /reservas/{id}
  - POST /boletos/emitir (tras pago validado)
  - GET /boletos/{codigo} (descarga/visualización)
- Móvil Personal de bus
  - POST /embarcque/scan (validar y marcar uso de boleto por QR/código)
- Configuración de app
  - GET/PUT /configuracion (logo, colores, redes, soporte)

## 11) Seguridad y roles
- RBAC con Spring Security + JWT; OAuth2/OIDC opcional para SSO futuro.
- Roles: CLIENTE (usuarios finales), OFICINISTA (ventas y validaciones), COOPERATIVA (gestión de recursos propios), ADMIN (global, opcional para SaaS multi-inquilino).
- Multi-inquilino (multi-tenant) a nivel lógico por cooperativaId en entidades; filtros por rol y cooperativa.

## 12) Persistencia y esquema (sugerencia)
- Tablas principales alineadas a entidades del punto 9.
- Índices: por (cooperativaId), (frecuenciaId, fecha), (viajeId), (placa), (cedula), (codigoBoleto).
- Integridad: FK con borrado lógico para entidades críticas.

## 13) Flujos críticos
- Venta y emisión de boleto:
  1) Búsqueda → 2) Selección de viaje/asientos → 3) Reserva (timeout) → 4) Pago (transferencia o PayPal) → 5) Validación (manual/automática) → 6) Emisión de boleto + QR → 7) Notificación.
- Embarque:
  - Escaneo QR → validación de vigencia/uso → marca de uso → registro de auditoría (usuario del bus, hora, geolocalización opcional).
- Compra entre paradas:
  - Calcular tramo Parada A → Parada B dentro de la ruta; bloquear asientos para ese subtramo.

## 14) Calidad, pruebas y observabilidad
- Pruebas: unitarias (JUnit), integraciones (Spring Boot Test), e2e (Postman/Newman, Selenium para web).
- SonarQube para análisis estático y cobertura.
- Logs estructurados (JSON) y trazabilidad de pagos/boletos.

## 15) Backlog inicial (MVP)
- Iteración 1
  - Autenticación (login/registro cliente) y RBAC básico.
  - CRUD cooperativas, buses, frecuencias y paradas.
  - Generación de viajes desde frecuencias (por fecha).
- Iteración 2
  - Búsqueda de rutas, disponibilidad y selección de asientos.
  - Reservas con expiración.
  - Flujo de pago por transferencia (subida de comprobante) + validación manual.
- Iteración 3
  - Integración PayPal (sandbox) con webhook.
  - Emisión de boletos + generación QR.
  - App móvil de embarque (endpoint de escaneo) y validación de uso.
- Iteración 4
  - Configuración de marca por cooperativa.
  - Notificaciones por email.
  - Historial de compras para cliente.

## 16) Riesgos y mitigaciones
- Disponibilidad y sobreventa: manejar locks/optimistic locking al reservar asientos.
- Pagos: reconciliación y manejo de reintentos de webhook/IPN.
- Multi-tenant: filtrado y aislamiento por cooperativa desde el inicio.
- Validación de descuentos: fuente confiable para cédula; si no disponible, políticas de auditoría.
- UX móvil sin conexión: cache/cola local para escaneo en zonas sin cobertura (futuro).

## 17) Próximos pasos operativos
- Acordar y documentar definición de listo/terminado.
- Configurar repositorio con pipeline CI (build + pruebas + análisis Sonar).
- Definir esquema inicial en JPA y migraciones con Flyway/Liquibase.
- Crear endpoints básicos de autenticación y CRUDs de catálogos.
- Preparar colección Postman con casos críticos.

— Fin del documento —
