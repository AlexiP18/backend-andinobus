# 📋 Lógica de Negocio: Terminales, Rutas, Caminos y Frecuencias

Este documento establece las reglas de negocio para la gestión de terminales, rutas, caminos, paradas y frecuencias en el sistema AndinoBus.

---

## 📊 Índice

1. [Terminales](#1-terminales)
2. [Rutas](#2-rutas)
3. [Caminos](#3-caminos)
4. [Paradas Intermedias](#4-paradas-intermedias)
5. [Frecuencias](#5-frecuencias)
6. [Asignación de Buses y Choferes](#6-asignación-de-buses-y-choferes)
7. [Algoritmo de Distribución de Frecuencias](#7-algoritmo-de-distribución-de-frecuencias)
8. [Reglas de Negocio Consolidadas](#8-reglas-de-negocio-consolidadas)

---

## 1. Terminales

### 1.1 Definición
Las terminales son los puntos físicos donde los buses pueden:
- Iniciar un viaje (origen)
- Finalizar un viaje (destino)
- Realizar paradas intermedias para embarque/desembarque

### 1.2 Ubicación
Cada terminal está asociada a:
- **Provincia**: División administrativa mayor
- **Cantón**: División administrativa donde se ubica físicamente
- **Nombre**: Identificador único del terminal

### 1.3 Tipología de Terminales
Las terminales se clasifican según su capacidad e infraestructura:

| Tipología | Descripción | Ejemplos |
|-----------|-------------|----------|
| **T1** | Terminal básico/satélite | Pujilí, Satélite La Concordia |
| **T2** | Terminal pequeño | Paute, Sigsig, Chambo, Cayambe |
| **T3** | Terminal mediano | Riobamba, Latacunga, Ibarra, Loja |
| **T4** | Terminal grande | Cañar, Binacional Santa Rosa, Macas |
| **T5** | Terminal principal/hub | Cuenca, Machala, Guayaquil, Quito, Santo Domingo |

### 1.4 Terminales por Provincia (Resumen)

```
PROVINCIA           | TERMINALES | TIPOLOGÍAS
--------------------|------------|------------------
Azuay               | 5          | T5, T3, T3, T2, T2
Guayas              | 4          | T5, T3, T3, T3
Pichincha           | 3          | T5, T3, T2
Tungurahua          | 5          | T4, T3, T3, T3, T2
Manabí              | 10         | T5, T5, T3, T3, T3, T3, T3, T2, T2, T2
...
```

### 1.5 Capacidad de Frecuencias por Terminal

Cada terminal tiene una capacidad máxima de frecuencias basada en:
- **Número de andenes**: Espacios físicos para buses
- **Frecuencias máximas por andén**: 96 frecuencias/día (1 cada 15 min × 24 horas)
- **Máximo frecuencias por terminal**: andenes × 96

**Ejemplos de Capacidad:**

| Terminal | Andenes | Máx. Frecuencias/Día |
|----------|---------|----------------------|
| Jaime Roldós (Guayaquil) | 112 | 10,752 |
| Quitumbe (Quito) | 66 | 6,336 |
| Cuenca | 43 | 4,128 |
| Machala | 49 | 4,704 |
| Santo Domingo | 36 | 3,456 |
| Riobamba | 8 | 768 |
| Pujilí | 1 | 96 |

---

## 2. Rutas

### 2.1 Definición
Una **ruta** es la conexión lógica entre dos terminales (origen y destino), aprobada por la ANT (Agencia Nacional de Tránsito).

### 2.2 Características de una Ruta
- **Origen**: Terminal de partida
- **Destino**: Terminal de llegada
- **Distancia**: Kilómetros totales
- **Duración estimada**: Tiempo aproximado del recorrido
- **Estado ANT**: Aprobada/Pendiente
- **Resolución ANT**: Número de documento de aprobación
- **Vigencia**: Fecha hasta la cual está autorizada

### 2.3 Rutas Autorizadas vs Realizadas

Según los datos de rutas_frecuencias.csv:

| Cantón | Rutas Autorizadas | Rutas Realizadas | % Uso |
|--------|-------------------|------------------|-------|
| Quito | 406 | 335 | 82.5% |
| Santo Domingo | 382 | 87 | 22.8% |
| Ambato | 272 | 121 | 44.5% |
| Guayaquil | 225 | 0 | 0% |
| Riobamba | 180 | 71 | 39.4% |
| Loja | 171 | 98 | 57.3% |
| Cuenca | 146 | 0 | 0% |

### 2.4 Reglas de Negocio para Rutas

1. **RN-RUTA-001**: Toda ruta debe tener aprobación ANT antes de operar
2. **RN-RUTA-002**: Una ruta puede tener múltiples caminos alternativos
3. **RN-RUTA-003**: La ruta define origen-destino, el camino define el trayecto específico
4. **RN-RUTA-004**: Las rutas se asignan a cooperativas específicas

---

## 3. Caminos

### 3.1 Definición
Un **camino** es el trayecto físico específico que sigue un bus para completar una ruta. Una ruta puede tener varios caminos alternativos.

### 3.2 Ejemplo
**Ruta**: Quito → Guayaquil

**Caminos posibles**:
1. Quito → Latacunga → Ambato → Riobamba → Guayaquil (Vía Sierra)
2. Quito → Santo Domingo → Guayaquil (Vía Costa)
3. Quito → Alóag → Guayaquil (Vía Panamericana directa)

### 3.3 Componentes de un Camino
- **Paradas ordenadas**: Secuencia de terminales/puntos por donde pasa
- **Coordenadas GPS**: Para tracking en tiempo real
- **Distancia real**: Kilómetros del trayecto específico
- **Tiempo estimado**: Duración considerando el camino específico

### 3.4 Reglas de Negocio para Caminos

1. **RN-CAMINO-001**: Cada camino pertenece a una ruta específica
2. **RN-CAMINO-002**: El camino define las paradas intermedias disponibles
3. **RN-CAMINO-003**: Las cooperativas eligen qué camino usar para sus frecuencias
4. **RN-CAMINO-004**: El sistema puede sugerir caminos automáticamente basado en:
   - Terminales existentes en el trayecto
   - Distancia óptima
   - Tiempo estimado
   - Demanda histórica

---

## 4. Paradas Intermedias

### 4.1 Definición
Las **paradas intermedias** son terminales habilitadas en el trayecto de un camino donde:
- Pasajeros pueden **bajar** (desembarque)
- Pasajeros pueden **subir** (embarque) ocupando asientos liberados

### 4.2 Gestión de Capacidad en Paradas

```
Ejemplo: Frecuencia Quito → Guayaquil con parada en Riobamba

Salida Quito:     40 pasajeros (capacidad total)
Llegada Riobamba: 35 pasajeros (5 bajaron)
Salida Riobamba:  40 pasajeros (5 nuevos subieron)
Llegada Guayaquil: 40 pasajeros
```

### 4.3 Reglas de Negocio para Paradas

1. **RN-PARADA-001**: Solo se pueden embarcar pasajeros si hay asientos disponibles
2. **RN-PARADA-002**: Los asientos se liberan cuando un pasajero baja
3. **RN-PARADA-003**: El precio del boleto es proporcional al tramo:
   - Tramo completo (Quito→Guayaquil): 100%
   - Tramo parcial (Riobamba→Guayaquil): ~60%
4. **RN-PARADA-004**: Las paradas intermedias deben ser terminales habilitadas
5. **RN-PARADA-005**: Tiempo de parada: 10-20 minutos según terminal

---

## 5. Frecuencias

### 5.1 Definición
Una **frecuencia** es una salida programada de un bus en una ruta específica, con:
- Hora de salida definida
- Ruta y camino asignados
- Bus y chofer designados

### 5.2 Capacidad de Frecuencias

**Por Terminal (ejemplos):**

| Terminal | Frecuencias Autorizadas | Frecuencias Realizadas | % Ocupación |
|----------|------------------------|------------------------|-------------|
| Quito | 1,943 | 2,055 | 105.8% ⚠️ |
| Guayaquil | 2,684 | 2,251 | 83.9% |
| Ambato | 1,687 | 1,382 | 81.9% |
| Santo Domingo | 1,794 | 712 | 39.7% |
| Riobamba | 1,122 | 1,151 | 102.6% ⚠️ |

### 5.3 Estructura de Frecuencias (Basado en documentos de ejemplo)

Analizando "Horas de Trabajo RUTAS 1.csv" y "Horas de Trabajo RUTAS 2.csv":

**Patrón de Frecuencias Diarias:**

```
DÍA | HORA SALIDA | ORIGEN    | DESTINO     | HORA LLEGADA
----|-------------|-----------|-------------|-------------
1   | 06:00       | AMBATO    | QUITO       | ~09:00
    | 18:00       | QUITO     | LOJA        | 16:15 (día+1)
    | 20:15       | AMBATO    | LOJA        | ~05:00 (día+1)
2   | 21:45       | LOJA      | QUITO       | ~08:00 (día+1)
3   | 20:40       | QUITO     | LOJA        | 19:15 (día+1)
...
```

### 5.4 Ciclo de Trabajo de Buses

Según los documentos, un bus sigue un ciclo de trabajo de **23-36 días** donde:
- Realiza múltiples rutas en secuencia
- Alterna entre diferentes destinos
- Incluye días de **PARADA** (mantenimiento/descanso)

**Ejemplo de Ciclo (36 días):**
```
Días 1-3:   AMBATO → QUITO → LOJA → QUITO
Días 4-7:   QUITO → AMBATO → TULCÁN → AMBATO
Días 8-11:  CUENCA → QUITO → CUENCA
Días 12-15: QUITO → MACHALA → QUITO
Días 16-20: LOJA → QUITO → GUAYAQUIL
...
Día 8/30:   PARADA (descanso/mantenimiento)
```

### 5.5 Reglas de Negocio para Frecuencias

1. **RN-FREQ-001**: No exceder el máximo de frecuencias por terminal
2. **RN-FREQ-002**: Distribución horaria equilibrada (evitar congestión)
3. **RN-FREQ-003**: Mínimo 15 minutos entre frecuencias del mismo destino
4. **RN-FREQ-004**: Considerar demanda por horario:
   - Alta demanda: 06:00-09:00, 17:00-21:00
   - Media demanda: 09:00-17:00
   - Baja demanda: 21:00-06:00

---

## 6. Asignación de Buses y Choferes

### 6.1 Reglas de Jornada Laboral del Chofer

| Regla | Descripción |
|-------|-------------|
| **RN-CHOFER-001** | Jornada regular: máximo 8 horas diarias |
| **RN-CHOFER-002** | Jornada extendida: máximo 10 horas (con restricciones) |
| **RN-CHOFER-003** | Jornada extendida permitida máximo 2 veces por semana |
| **RN-CHOFER-004** | Descanso mínimo entre jornadas: 8 horas |
| **RN-CHOFER-005** | Si chofer excede horas, reasignar a otro chofer disponible |

### 6.2 Asignación Bus-Chofer

```
Bus #67:
  - Chofer Principal: Juan Pérez
  - Choferes Alternos: Carlos López, María García
  
Lunes:
  06:00 AMBATO→QUITO (3h) - Juan Pérez
  10:00 QUITO→GUAYAQUIL (5h) - Juan Pérez (Total: 8h) ✅
  
Martes:
  06:00 GUAYAQUIL→QUITO (5h) - Juan Pérez
  12:00 QUITO→LOJA (8h) - Carlos López (Juan ya cumplió 5h, total sería 13h) ⚠️
```

### 6.3 Algoritmo de Asignación de Chofer

```pseudocode
función asignarChofer(bus, frecuencia):
    choferPrincipal = bus.choferPrincipal
    horasTrabajadas = obtenerHorasTrabajadas(choferPrincipal, hoy)
    duracionViaje = frecuencia.duracionEstimada
    
    si horasTrabajadas + duracionViaje <= 8:
        return choferPrincipal
    
    si horasTrabajadas + duracionViaje <= 10:
        jornadasExtendidas = contarJornadasExtendidas(choferPrincipal, estaSemana)
        si jornadasExtendidas < 2:
            return choferPrincipal
    
    // Buscar chofer alterno disponible
    para cada choferAlterno en bus.choferesAlternos:
        horasAlterno = obtenerHorasTrabajadas(choferAlterno, hoy)
        si horasAlterno + duracionViaje <= 8:
            return choferAlterno
    
    // Si no hay chofer disponible, la frecuencia no puede asignarse
    return null
```

---

## 7. Algoritmo de Distribución de Frecuencias

### 7.1 Objetivo
Distribuir las frecuencias de manera óptima considerando:
- Capacidad máxima de terminales
- Demanda por horario
- Disponibilidad de buses y choferes
- Evitar congestión

### 7.2 Algoritmo de Asignación Automática

```pseudocode
función generarFrecuencias(cooperativa, ruta, camino, fechaInicio, fechaFin):
    frecuenciasAutorizadas = obtenerFrecuenciasAutorizadas(cooperativa, ruta)
    busesDisponibles = obtenerBusesDisponibles(cooperativa)
    
    para cada día en rango(fechaInicio, fechaFin):
        frecuenciasDelDia = []
        
        // Distribuir frecuencias por franja horaria
        para cada franja en FRANJAS_HORARIAS:
            maxFrecuenciasFranja = calcularMaxPorFranja(franja, ruta)
            frecuenciasActuales = contarFrecuenciasExistentes(ruta, día, franja)
            espaciosDisponibles = maxFrecuenciasFranja - frecuenciasActuales
            
            para i en rango(min(espaciosDisponibles, frecuenciasAutorizadas)):
                horaSalida = calcularHoraSalidaOptima(franja, frecuenciasDelDia)
                bus = seleccionarBusDisponible(busesDisponibles, horaSalida)
                
                si bus != null:
                    chofer = asignarChofer(bus, duracionViaje)
                    si chofer != null:
                        frecuencia = crearFrecuencia(ruta, camino, día, horaSalida, bus, chofer)
                        frecuenciasDelDia.agregar(frecuencia)
                        actualizarDisponibilidad(bus, chofer, frecuencia)
        
        guardarFrecuencias(frecuenciasDelDia)
```

### 7.3 Franjas Horarias

```
FRANJAS_HORARIAS = [
    { nombre: "Madrugada",    inicio: "00:00", fin: "05:59", peso: 0.5 },
    { nombre: "Mañana Alta",  inicio: "06:00", fin: "08:59", peso: 1.5 },
    { nombre: "Mañana",       inicio: "09:00", fin: "11:59", peso: 1.0 },
    { nombre: "Mediodía",     inicio: "12:00", fin: "14:59", peso: 1.0 },
    { nombre: "Tarde",        inicio: "15:00", fin: "17:59", peso: 1.2 },
    { nombre: "Noche Alta",   inicio: "18:00", fin: "20:59", peso: 1.5 },
    { nombre: "Noche",        inicio: "21:00", fin: "23:59", peso: 0.8 }
]
```

### 7.4 Validaciones Pre-Asignación

```pseudocode
función validarFrecuencia(frecuencia):
    validaciones = []
    
    // Validar capacidad de terminal origen
    si excedeFrecuenciasTerminal(frecuencia.origen, frecuencia.horaSalida):
        validaciones.agregar("Terminal origen al límite de capacidad")
    
    // Validar capacidad de terminal destino
    si excedeFrecuenciasTerminal(frecuencia.destino, frecuencia.horaLlegada):
        validaciones.agregar("Terminal destino al límite de capacidad")
    
    // Validar separación mínima entre frecuencias
    frecuenciaCercana = buscarFrecuenciaCercana(frecuencia)
    si frecuenciaCercana != null AND diferencia < 15 minutos:
        validaciones.agregar("Muy cerca de otra frecuencia")
    
    // Validar disponibilidad del bus
    si busNoDisponible(frecuencia.bus, frecuencia.horaSalida):
        validaciones.agregar("Bus no disponible en ese horario")
    
    // Validar jornada del chofer
    si choferExcedeJornada(frecuencia.chofer, frecuencia):
        validaciones.agregar("Chofer excede jornada laboral")
    
    return validaciones
```

---

## 8. Reglas de Negocio Consolidadas

### 8.1 Terminales

| Código | Regla |
|--------|-------|
| RN-TERM-001 | Cada terminal pertenece a un cantón específico |
| RN-TERM-002 | La tipología determina servicios e infraestructura |
| RN-TERM-003 | Frecuencias máximas = andenes × 96 |
| RN-TERM-004 | No se pueden exceder las frecuencias máximas |

### 8.2 Rutas

| Código | Regla |
|--------|-------|
| RN-RUTA-001 | Requiere aprobación ANT para operar |
| RN-RUTA-002 | Define origen y destino únicamente |
| RN-RUTA-003 | Puede tener múltiples caminos |
| RN-RUTA-004 | Se asigna a cooperativas específicas |

### 8.3 Caminos

| Código | Regla |
|--------|-------|
| RN-CAM-001 | Cada camino pertenece a una ruta |
| RN-CAM-002 | Define el trayecto físico específico |
| RN-CAM-003 | Incluye paradas intermedias opcionales |
| RN-CAM-004 | Sistema puede sugerir caminos automáticamente |

### 8.4 Paradas

| Código | Regla |
|--------|-------|
| RN-PAR-001 | Solo en terminales habilitadas del camino |
| RN-PAR-002 | Embarque limitado a asientos disponibles |
| RN-PAR-003 | Precio proporcional al tramo |
| RN-PAR-004 | Tiempo de parada: 10-20 minutos |

### 8.5 Frecuencias

| Código | Regla |
|--------|-------|
| RN-FRQ-001 | No exceder máximo por terminal |
| RN-FRQ-002 | Mínimo 15 min entre frecuencias mismo destino |
| RN-FRQ-003 | Distribución equilibrada por franjas horarias |
| RN-FRQ-004 | Ciclos de trabajo de 23-36 días por bus |

### 8.6 Choferes

| Código | Regla |
|--------|-------|
| RN-CHO-001 | Jornada regular: máximo 8 horas/día |
| RN-CHO-002 | Jornada extendida: máximo 10 horas |
| RN-CHO-003 | Extensión permitida 2 veces/semana máximo |
| RN-CHO-004 | Descanso mínimo: 8 horas entre jornadas |
| RN-CHO-005 | Reasignación automática si excede límites |

---

## 9. Diagrama de Relaciones

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SISTEMA ANDINOBUS                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐
│   PROVINCIA  │
│  (24 total)  │
└──────┬───────┘
       │ 1:N
       ▼
┌──────────────┐
│    CANTÓN    │
│ (221 total)  │
└──────┬───────┘
       │ 1:N
       ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   TERMINAL   │◄──────│    CAMINO    │──────►│    RUTA      │
│  (74 total)  │       │  (Trayecto)  │       │ (Origen-Dest)│
│              │       │              │       │              │
│ - Tipología  │       │ - Paradas[]  │       │ - ANT Aprob. │
│ - Andenes    │       │ - Distancia  │       │ - Resolución │
│ - Max Freq   │       │ - Duración   │       │ - Vigencia   │
└──────────────┘       └──────┬───────┘       └──────────────┘
       ▲                      │
       │                      │ 1:N
       │                      ▼
       │               ┌──────────────┐
       │               │  FRECUENCIA  │
       │               │              │
       └───────────────│ - Hora Sal.  │
         (paradas)     │ - Hora Lleg. │
                       │ - Estado     │
                       └──────┬───────┘
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
             ┌──────────────┐    ┌──────────────┐
             │     BUS      │    │    CHOFER    │
             │              │    │              │
             │ - Placa      │    │ - Licencia   │
             │ - Capacidad  │    │ - Horas Trab │
             │ - Cooperat.  │    │ - Max 8h/día │
             └──────────────┘    └──────────────┘
                    │
                    ▼
             ┌──────────────┐
             │ COOPERATIVA  │
             │              │
             │ - Rutas Asig │
             │ - Freq. Aut. │
             │ - Buses      │
             └──────────────┘
```

---

## 10. Flujo de Operación

```
1. CONFIGURACIÓN INICIAL
   │
   ├─► Registrar Terminales (por cantón, con tipología)
   │
   ├─► Definir Rutas (origen → destino, con aprobación ANT)
   │
   └─► Crear Caminos (trayectos específicos con paradas)

2. ASIGNACIÓN A COOPERATIVA
   │
   ├─► Cooperativa solicita autorización de ruta
   │
   ├─► ANT aprueba y asigna frecuencias máximas
   │
   └─► Cooperativa selecciona caminos para operar

3. PROGRAMACIÓN DE FRECUENCIAS
   │
   ├─► Sistema calcula disponibilidad por terminal/horario
   │
   ├─► Sistema sugiere distribución óptima de frecuencias
   │
   ├─► Sistema asigna buses según disponibilidad
   │
   └─► Sistema asigna choferes respetando jornadas

4. OPERACIÓN DIARIA
   │
   ├─► Tracking GPS en tiempo real
   │
   ├─► Gestión de paradas intermedias
   │
   ├─► Control de embarque/desembarque
   │
   └─► Reasignación de choferes si es necesario

5. MONITOREO Y AJUSTES
   │
   ├─► Dashboard de ocupación de terminales
   │
   ├─► Alertas de exceso de frecuencias
   │
   ├─► Reportes de cumplimiento
   │
   └─► Optimización automática de horarios
```

---

## 11. Datos de Referencia

### 11.1 Terminales Principales (T5)
- Cuenca (Azuay) - 43 andenes
- Machala (El Oro) - 49 andenes
- Jaime Roldós Aguilera, Guayaquil (Guayas) - 112 andenes
- Manta (Manabí) - 49 andenes
- Portoviejo (Manabí) - 40 andenes
- Quitumbe, Quito (Pichincha) - 66 andenes
- Santo Domingo (Santo Domingo) - 36 andenes
- Quevedo (Los Ríos) - 39 andenes

### 11.2 Frecuencias Críticas (> 100% ocupación)
- **Quito**: 2,055 realizadas / 1,943 autorizadas (105.8%)
- **Riobamba**: 1,151 realizadas / 1,122 autorizadas (102.6%)
- **Santa Rosa**: 535 realizadas / 54 autorizadas (990.7%) ⚠️
- **Quevedo**: 1,987 realizadas / 1,326 autorizadas (149.8%)

### 11.3 Terminales Subutilizadas (< 50% ocupación)
- **Cuenca**: 623 / 836 (74.5%)
- **Santo Domingo**: 712 / 1,794 (39.7%)
- **La Concordia**: 12 / 247 (4.9%)
- **Sucumbíos**: 140 / 797 (17.6%)

---

**Documento generado para el Sistema AndinoBus**
**Versión**: 1.0
**Fecha**: 28 de Noviembre, 2025
**Basado en**: terminales.csv, rutas_frecuencias.csv, maximo_frecuencias.csv, Horas de Trabajo RUTAS 1.csv, Horas de Trabajo RUTAS 2.csv
