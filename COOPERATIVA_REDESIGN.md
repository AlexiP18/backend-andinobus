# Reestructuración del Sistema - Gestión de Cooperativa

## Cambios en Roles del Sistema

### Roles Actualizados:

1. **ADMIN** (anteriormente COOPERATIVA)
   - Gestión de todas las cooperativas del sistema
   - Configuración global del sistema
   - Administración de usuarios del sistema

2. **COOPERATIVA** (nuevo enfoque)
   - Gestión de sus propios buses
   - Asignación de buses a frecuencias
   - Control de días de parada
   - Generación de hojas de trabajo
   - Visualización de disponibilidad

3. **OFICINISTA**
   - Venta de boletos
   - Gestión de reservas
   - Atención al cliente

4. **CLIENTE**
   - Búsqueda de rutas
   - Compra de boletos
   - Visualización de sus boletos

## Modelo de Datos

### Entidades Principales:

#### 1. **Bus**
```
- id
- cooperativa_id (FK)
- numero_interno
- placa
- chasis_marca
- carroceria_marca
- capacidad_asientos (default: 40)
- estado (DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA)
- foto_url
- activo
```

#### 2. **Frecuencia**
```
- id
- cooperativa_id (FK)
- origen
- destino
- hora_salida
- duracion_estimada_min
- dias_operacion
- activa
```

#### 3. **ParadaIntermedia** (NUEVA)
```
- id
- frecuencia_id (FK)
- ciudad
- orden_parada (1, 2, 3...)
- minutos_desde_origen
- precio_adicional
- activo
```

**Ejemplo:**
- Ruta: Quito → Loja
- Paradas intermedias:
  1. Latacunga (orden: 1, minutos: 90, precio: $2)
  2. Riobamba (orden: 2, minutos: 180, precio: $3)
  3. Cuenca (orden: 3, minutos: 360, precio: $5)

#### 4. **AsignacionBusFrecuencia** (NUEVA)
```
- id
- bus_id (FK)
- frecuencia_id (FK)
- fecha_inicio
- fecha_fin (nullable)
- estado (ACTIVA | SUSPENDIDA | FINALIZADA)
- observaciones
```

**Lógica:**
- Una frecuencia puede tener múltiples buses asignados
- Un bus solo puede estar asignado a una frecuencia activa a la vez
- Si hay más buses que frecuencias activas → Exceso de buses → Días de parada automáticos

#### 5. **DiaParadaBus** (NUEVA)
```
- id
- bus_id (FK)
- fecha
- motivo (MANTENIMIENTO | EXCESO_CAPACIDAD | OTRO)
- observaciones
```

**Propósito:**
- Registrar días en que un bus no estará operativo
- Gestionar mantenimientos programados
- Administrar exceso de capacidad

#### 6. **HojaRuta** (ya existe)
```
- id
- fecha
- cooperativa_id (FK)
- estado (GENERADA | PUBLICADA | CERRADA)
```

**Propósito:**
- Documento de trabajo diario
- Contiene todos los viajes programados para el día
- Se genera automáticamente basándose en las asignaciones activas

## Flujo de Operación de la Cooperativa

### 1. Gestión de Buses
```
Cooperativa registra sus buses
↓
Define capacidad y características
↓
Estado inicial: DISPONIBLE
```

### 2. Creación de Frecuencias
```
Cooperativa crea frecuencia (ej: Quito-Loja, 08:00)
↓
Agrega paradas intermedias (Latacunga, Riobamba, Cuenca)
↓
Define días de operación (L-V, S-D)
↓
Estado: ACTIVA/INACTIVA
```

### 3. Asignación de Buses
```
Cooperativa asigna Bus #1 → Frecuencia Quito-Loja 08:00
↓
Bus #1 estado: EN_SERVICIO
↓
Asignación válida desde fecha_inicio hasta fecha_fin
```

### 4. Gestión de Exceso
```
Cooperativa tiene: 10 buses
Frecuencias activas necesitan: 7 buses
↓
Exceso: 3 buses
↓
Sistema sugiere/registra días de parada para buses sin asignación
```

### 5. Generación de Hoja de Trabajo
```
Sistema genera automáticamente (diario o semanal)
↓
Basándose en:
  - Asignaciones activas
  - Frecuencias activas
  - Buses disponibles
  - Días de parada registrados
↓
Resultado: Hoja de trabajo con viajes programados
```

## Endpoints API - Dashboard Cooperativa

### Gestión de Buses
```
GET    /api/cooperativa/buses?cooperativaId={id}
GET    /api/cooperativa/buses/disponibles?cooperativaId={id}&fecha={fecha}
PATCH  /api/cooperativa/buses/{busId}/estado
```

### Gestión de Asignaciones
```
POST   /api/cooperativa/asignaciones
       Body: { busId, frecuenciaId, fechaInicio, fechaFin, observaciones }

GET    /api/cooperativa/asignaciones?cooperativaId={id}
PATCH  /api/cooperativa/asignaciones/{id}/finalizar
```

### Gestión de Días de Parada
```
POST   /api/cooperativa/dias-parada
       Body: { busId, fecha, motivo, observaciones }

GET    /api/cooperativa/dias-parada?cooperativaId={id}&fechaInicio={f1}&fechaFin={f2}
DELETE /api/cooperativa/dias-parada/{id}
```

### Resumen y Reportes
```
GET    /api/cooperativa/resumen-disponibilidad?cooperativaId={id}&fecha={fecha}
       Response: {
         totalBuses,
         busesDisponibles,
         busesEnServicio,
         busesMantenimiento,
         busesParada,
         frecuenciasActivas,
         excesoBuses
       }
```

## Frontend - CooperativaDashboard

### Secciones Principales:

#### 1. **Panel de Control** (Dashboard Home)
- Resumen de disponibilidad
- Buses en servicio vs disponibles
- Frecuencias activas
- Alertas de exceso de buses

#### 2. **Gestión de Buses**
- Lista de todos los buses
- Filtros por estado
- Acciones: Ver detalles, cambiar estado, asignar a frecuencia

#### 3. **Gestión de Frecuencias**
- Lista de frecuencias activas/inactivas
- Crear nueva frecuencia
- Agregar/editar paradas intermedias
- Ver buses asignados a cada frecuencia

#### 4. **Asignaciones**
- Calendario de asignaciones
- Asignar bus a frecuencia
- Finalizar asignaciones
- Ver historial

#### 5. **Días de Parada**
- Calendario con días de parada marcados
- Registrar nuevo día de parada
- Ver motivos y observaciones
- Gestionar días de parada automáticos por exceso

#### 6. **Hoja de Trabajo**
- Generar hoja de trabajo para fecha específica
- Visualizar viajes programados
- Exportar a PDF
- Estado: Generada → Publicada → Cerrada

## Reglas de Negocio

### Asignaciones:
1. Un bus solo puede tener UNA asignación ACTIVA a la vez
2. Las asignaciones pueden solaparnse en el tiempo, pero no estar activas simultáneamente
3. Al finalizar una asignación, el bus vuelve a estado DISPONIBLE
4. Al crear una asignación, el bus pasa a estado EN_SERVICIO

### Días de Parada:
1. No se puede asignar un bus a una frecuencia si tiene día de parada registrado
2. El sistema puede sugerir automáticamente días de parada para buses en exceso
3. Los días de parada por MANTENIMIENTO tienen prioridad

### Frecuencias:
1. Una frecuencia INACTIVA no genera viajes
2. Las paradas intermedias se ordenan por orden_parada
3. El precio total del viaje puede incluir precio_adicional de paradas

### Hoja de Trabajo:
1. Se genera automáticamente para las próximas 24-48 horas
2. Solo incluye viajes de asignaciones ACTIVAS en frecuencias ACTIVAS
3. Excluye buses con días de parada registrados

## Migración de Datos

Ver archivo: `V7__gestion_cooperativa.sql`

Se crearon:
- Tabla `parada_intermedia`
- Tabla `asignacion_bus_frecuencia`
- Tabla `dia_parada_bus`
- Campos nuevos en `bus`: `capacidad_asientos`, `estado`

## Próximos Pasos

1. ✅ Actualizar migraciones de base de datos
2. ✅ Crear entidades JPA
3. ✅ Crear repositorios
4. ✅ Crear servicios de negocio
5. ✅ Crear controladores REST
6. ✅ Crear DTOs
7. ⏳ Actualizar frontend CooperativaDashboard
8. ⏳ Implementar generación automática de hojas de trabajo
9. ⏳ Agregar validaciones de negocio
10. ⏳ Testing end-to-end
