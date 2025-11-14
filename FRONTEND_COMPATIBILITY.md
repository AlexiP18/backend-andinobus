# Integración con FrontEnd (FrontAndinaBus)

Este documento resume cómo alinear el backend `backend-smartcode` con el proyecto frontend `FrontAndinaBus` (Next.js) —en particular las carpetas `app/components`, `app/dashboard`, `app/login`, `app/register`— para poder probar flujos de autenticación, dashboard, búsqueda de rutas y ventas.

Repositorio frontend: https://github.com/xLexus/FrontAndinaBus

Nota: La revisión se centra en los componentes de Login y Register, y en las páginas funcionales mencionadas. El frontend usa componentes React/Next.js y formularios (p. ej. LoginForm.tsx y RegisterForm.tsx).

---

## 1) Autenticación (app/components/LoginForm.tsx y app/login)

- Campos esperados típicos en el formulario: `email`, `password`.
- Backend alineado:
  - POST /auth/login
    - Request (JSON): { "email": string, "password": string }
    - Response (JSON, stub): { token, userId, email, rol, nombres, apellidos }
    - Compatibilidad extra: además de los campos anteriores, la respuesta expone alias JSON para posibles expectativas del front: { id, role, firstName, lastName, name, username } mapeados a { userId, rol, nombres, apellidos, "nombres apellidos", email }.
  - GET /users/me
    - Header: Authorization: Bearer <token> (o X-Demo-Token)
    - Response (JSON, stub): { userId, email, rol, nombres, apellidos } con los mismos alias mencionados arriba.
- Tokens demo:
  - Si el email del login contiene la palabra "admin" → rol ADMIN y token "demo-token-admin".
  - Caso contrario → rol CLIENTE y token "demo-token-client".
- Recomendación para el frontend:
  - Definir variable de entorno: `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`.
  - Enviar el token guardado en `Authorization: Bearer <token>` para llamadas que requieran contexto de usuario (p. ej. dashboard).

Ejemplo (frontend):
```
await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
```

---

## 2) Registro (app/components/RegisterForm.tsx y app/register)

- Campos esperados típicos: `email`, `password`, `nombres`, `apellidos`.
- Backend alineado:
  - POST /auth/register
    - Request (JSON): { email, password, nombres, apellidos }
    - Response (JSON, stub): { token, userId, email, rol: "CLIENTE", nombres, apellidos }
- Comportamiento actual: crea usuario en memoria (stub) y retorna token demo de cliente.

---

## 3) Dashboard (app/dashboard)

El Dashboard suele requerir el usuario autenticado y datos agregados (próximos viajes, reservas, etc.). En este backend hay stubs pensados para integrarse de forma incremental:

- GET /users/me
  - Obtiene el usuario actual en base al token demo.
- GET /viajes?fecha=YYYY-MM-DD
  - Lista mock de viajes (a partir de frecuencias, como demostración).
- GET /viajes/{id}/disponibilidad
  - Disponibilidad mock por tipo de asiento.
- GET /viajes/{id}/bus
  - Ficha técnica mock del bus.
- Flujos de ventas (reserva → pago → emisión de boleto):
  - POST /reservas → crea reserva "pendiente".
  - POST /pagos/transferencia (multipart/form-data) → marca reserva como "pagado" (stub).
  - POST /boletos/emitir → emite boleto con `codigo`.
  - GET /boletos/{codigo} → consulta boleto.
- Embarque (móvil):
  - POST /embarque/scan → valida/marca uso de boleto (in-memory) y ahora registra bitácora si hay DB (perfil dev).

Sugerencia de uso en el Dashboard (en desarrollo):
- Mostrar datos del usuario vía `/users/me`.
- Consultar viajes por fecha con `/viajes?fecha=YYYY-MM-DD`.
- Enlazar flujo de compra con "Reservar" → `/reservas` → pago → `/boletos/emitir`.

---

## 4) Búsqueda de rutas (app/components y navegación)

El frontend puede presentar buscadores de rutas (origen/destino/fecha). Este backend ofrece stubs compatibles:
- GET /rutas/buscar?origen=&destino=&fecha=&cooperativa=&tipoAsiento=&tipoViaje=
  - Devuelve un item de ejemplo con asientos por tipo y datos básicos.

---

## 5) Consideraciones de CORS (para ejecutar frontend en localhost:3000)

- CORS ya está habilitado globalmente para desarrollo mediante una configuración WebMvcConfigurer (CorsConfig).
- Orígenes permitidos por defecto: http://localhost:3000. Puedes ajustar con la propiedad `app.cors.allowed-origins` (separar por comas para múltiples).
- No es necesario para Postman.

---

## 6) Base de datos y perfil `dev`

- Por defecto (sin perfil), el backend levanta sin DataSource (solo endpoints que no requieren DB).
- Con perfil `dev` se habilitan DataSource, JPA y Flyway:
  - `V1__init_catalogos.sql`: catálogos básicos (cooperativa, bus, frecuencia, parada).
  - `V2__usuarios_ventas_operacion_embarque.sql`: usuarios, operación (hoja_ruta, viaje), ventas (reserva, boleto) y embarque (embarque_scan_log).
- En `application-dev.properties` define tu conexión PostgreSQL local.

---

## 7) Postman (para probar backend rápidamente)

- Colecciones incluidas:
  - `postman/BackendSmartcode.postman_collection.json` (catálogos + planificaciones CSV)
  - `postman/BackendSmartcode-OperacionesVentas.postman_collection.json` (operación, rutas, usuarios, ventas y embarque)
- Environment: `postman/Local.postman_environment.json`
- Orden sugerido para Ventas:
  1) GET /viajes?fecha=YYYY-MM-DD (guardar `viajeId`)
  2) POST /reservas (guardar `reservaId`)
  3) POST /pagos/transferencia (marca pagado)
  4) POST /boletos/emitir (guardar `boletoCodigo`)
  5) POST /embarque/scan (usar `boletoCodigo`)

---

## 8) Resumen de mapeo Front → Back

- LoginForm / login page → POST /auth/login → guardar token → GET /users/me
- RegisterForm / register page → POST /auth/register → guardar token
- Dashboard → GET /users/me, GET /viajes, GET /viajes/{id}/disponibilidad, GET /viajes/{id}/bus
- Búsqueda de rutas → GET /rutas/buscar
- Flujo de compra → POST /reservas → POST /pagos/transferencia → POST /boletos/emitir → GET /boletos/{codigo}
- Embarque (escaneo) → POST /embarque/scan

Con esto, el FrontAndinaBus puede conectarse al backend (con stubs) y realizar pruebas de extremo a extremo en desarrollo.