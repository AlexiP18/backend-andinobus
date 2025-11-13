# API (borrador para Iteración 1)

Este documento enumera los endpoints iniciales para los catálogos y algunos stubs. Se complementa con ARCHITECTURE.md y PLAN_DE_TRABAJO.md.

Base URL: http://localhost:8080

## Convenciones
- Versionado de ruta pendiente (v1). Por ahora, sin prefijo.
- Respuestas en JSON. DTOs para entrada/salida.
- Manejo de errores uniforme (ver common.errors.GlobalExceptionHandler).

## Cooperativas
- POST /cooperativas
  - Crea una cooperativa.
  - Request (ejemplo):
    {
      "nombre": "Cooperativa Amazonas",
      "ruc": "1790012345001",
      "logoUrl": null,
      "activo": true
    }
  - Responses:
    - 201 Created: objeto CooperativaDTO con id
    - 400 Bad Request: validaciones

- GET /cooperativas?search=&page=0&size=20
  - Lista cooperativas (paginado).

- GET /cooperativas/{id}
  - Obtiene detalle.

- PUT /cooperativas/{id}
  - Actualiza datos.

- DELETE /cooperativas/{id}
  - Borrado lógico (activo=false).

## Buses
- POST /cooperativas/{cooperativaId}/buses
  - Crea bus asociado a cooperativa.

- GET /cooperativas/{cooperativaId}/buses
  - Lista buses de la cooperativa (paginado).

- GET /buses/{id}
  - Detalle de bus.

## Frecuencias y Paradas
- POST /cooperativas/{cooperativaId}/frecuencias
  - Crea una frecuencia para una cooperativa (origen, destino, horaSalida, diasOperacion, activa, duracionEstimadaMin opcional).
  - Request (ejemplo):
    {
      "origen": "Quito",
      "destino": "Loja",
      "horaSalida": "18:00",
      "duracionEstimadaMin": 720,
      "diasOperacion": "LUN,MAR,MIER,JUE,VIER,SAB,DOM",
      "activa": true
    }

- GET /cooperativas/{cooperativaId}/frecuencias?search=&page=0&size=20
  - Lista frecuencias activas de la cooperativa. Permite filtrar por texto (busca en origen o destino).

- GET /frecuencias/{id}
  - Obtiene detalle de una frecuencia.

- PUT /frecuencias/{id}
  - Actualiza los campos enviados (parcial). Ejemplo de request:
    {
      "horaSalida": "19:30",
      "activa": true
    }

- DELETE /frecuencias/{id}
  - Borrado lógico (activa=false).

- Paradas
  - POST /frecuencias/{frecuenciaId}/paradas
    - Crea parada intermedia (ciudad, orden, horaEstimada opcional).
  - GET /frecuencias/{frecuenciaId}/paradas
    - Lista paradas de la frecuencia ordenadas por 'orden'.
  - GET /paradas/{id}
    - Detalle de parada.
  - PUT /paradas/{id}
    - Actualiza ciudad/orden/horaEstimada.
  - DELETE /paradas/{id}
    - Elimina la parada.

## Búsqueda de rutas y disponibilidad (stub)
- GET /rutas/buscar?origen=&destino=&fecha=&cooperativa=&tipoAsiento=&tipoViaje=
  - Busca rutas disponibles. Respuesta mock con un item de ejemplo.
- GET /viajes/{id}/disponibilidad
  - Devuelve asientos disponibles por tipo (mock).
- GET /viajes/{id}/bus
  - Devuelve ficha técnica del bus del viaje (mock).

## Viajes (stub)
- GET /viajes?fecha=YYYY-MM-DD
  - Devuelve lista mock de viajes generados a partir de frecuencias para la fecha indicada (implementación de demostración).

## Autenticación y usuarios (stub)
- POST /auth/login
  - Retorna token mock según el email (si contiene "admin" → rol ADMIN; caso contrario CLIENTE).
- POST /auth/register (cliente)
  - Crea un usuario cliente (stub) y retorna token mock.
- GET /users/me
  - Retorna el usuario actual en base a un token demo. Puedes enviar:
    - Header Authorization: Bearer demo-token-admin | demo-token-client
    - o Header X-Demo-Token: demo-token-admin | demo-token-client

## Ventas y pagos (stubs)
- POST /reservas
  - Crea una reserva en estado "pendiente" con expiración de 15 minutos (in-memory).
- GET /reservas/{id}
  - Devuelve el detalle de la reserva (mock) o 400 si no existe.
- POST /pagos/transferencia (multipart/form-data)
  - Campos: reservaId (requerido), monto (opcional), referencia (opcional), comprobante (archivo opcional).
  - Marca la reserva como "pagado" en memoria y responde 202 Accepted.
- POST /pagos/paypal/webhook
  - Endpoint receptor de eventos Webhook/IPN de PayPal (mock). Devuelve 200.
- POST /boletos/emitir
  - Si la reserva está pagada, emite boleto (mock) y retorna 201 con un código generado.
- GET /boletos/{codigo}
  - Devuelve el boleto emitido; 400 si no existe.

## Móvil Personal de bus (stub)
- POST /embarque/scan
  - Valida y marca uso de boleto por código/QR (in-memory). Si se vuelve a escanear, devuelve estado "usado".
  - Alias disponible: /embarcque/scan

## Configuración de app (stub)
- GET /configuracion
  - Devuelve configuración en memoria (logo, colores, redes, soporte).
- PUT /configuracion
  - Actualiza configuración en memoria.

## Errores
- Formato general sugerido:
  {
    "timestamp": "2025-11-12T14:00:01Z",
    "status": 400,
    "code": "VALIDATION_ERROR",
    "message": "Request inválido",
    "details": [
      { "field": "nombre", "message": "no debe estar vacío" }
    ],
    "path": "/cooperativas"
  }


## Planificaciones (CSV)
- POST /api/planificaciones/preview (multipart/form-data)
  - Carga un archivo CSV para previsualizar la planificación parseada sin guardar en base de datos.
  - Requisitos de encabezados: ORIGEN, DESTINO, HORA DE SALIDA. Opcional: HORA DE LLEGADA.
  - Sinónimos aceptados (insensibles a mayúsculas/acentos y con variaciones de espacios/guiones):
    - ORIGEN: "Origen", "Ciudad Origen", "Desde"
    - DESTINO: "Destino", "Ciudad Destino", "Hasta"
    - HORA DE SALIDA: "Hora de Salida", "Salida", "Hora_Salida"
    - HORA DE LLEGADA (opcional): "Hora de Llegada", "Llegada", "Hora_Llegada"
  - Formatos de hora soportados: "07:30", "7:30", "07:30:00", "14h30", "17.45", "730" (→ 07:30), "7" (→ 07:00).
  - Respuesta: JSON con items parseados, avisos y errores por fila.
  - Ejemplo (PowerShell):
    curl -Method POST -Uri http://localhost:8080/api/planificaciones/preview -Form @{ file = Get-Item .\"Horas de Trabajo RUTAS 1.csv" }

- GET /api/planificaciones/examples
  - Intenta leer los archivos de ejemplo del repositorio:
    - "Horas de Trabajo RUTAS 1.csv"
    - "Horas de Trabajo RUTAS 2.csv"
  - Búsqueda en orden: classpath (si se mueven a src/main/resources) y luego carpeta de trabajo (raíz del proyecto en desarrollo).
  - Devuelve una lista con dos resultados (los que existan), cada uno con su sourceName, items, y warnings.
  - Ejemplo:
    GET http://localhost:8080/api/planificaciones/examples

- Respuesta (ejemplo simplificado):
  [
    {
      "sourceName": "Horas de Trabajo RUTAS 1.csv",
      "items": [
        {
          "origen": "Ambato",
          "destino": "Quito",
          "horaSalida": "07:30:00",
          "horaLlegada": null,
          "rowNumber": 2
        }
      ],
      "warnings": []
    }
  ]



---

## Colección Postman
- Archivos incluidos en el repositorio:
  - Colección: postman/BackendSmartcode.postman_collection.json
  - Environment local: postman/Local.postman_environment.json
- Uso:
  1) Importa ambos archivos en Postman.
  2) Selecciona el environment "Local" (baseUrl = http://localhost:8080).
  3) Arranca la aplicación:
     - Perfil por defecto (solo Planificaciones CSV): mvn spring-boot:run
     - Perfil dev (Cooperativas, Buses y Frecuencias): mvn spring-boot:run -Dspring-boot.run.profiles=dev
  4) Ejecuta las requests en el orden sugerido dentro de la colección. Las pruebas (tests) de Postman guardan automáticamente variables como coopId, busId y frecuenciaId para usarlas en las siguientes llamadas.
- Nota: Los endpoints de catálogos se exponen solo con el perfil dev activo.
