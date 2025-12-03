-- V41: Sistema de rotación de buses y tipos de frecuencia
-- Fecha: 2025-11-30

-- 0. Agregar campo terminal_base a bus
ALTER TABLE bus 
ADD COLUMN IF NOT EXISTS terminal_base_id BIGINT REFERENCES terminal(id);

COMMENT ON COLUMN bus.terminal_base_id IS 'Terminal base donde normalmente inicia operaciones el bus';

-- 1. Agregar campo tipo_frecuencia a frecuencia_viaje
ALTER TABLE frecuencia_viaje 
ADD COLUMN IF NOT EXISTS tipo_frecuencia VARCHAR(32) DEFAULT 'INTERPROVINCIAL';

-- 2. Agregar campos de control de rotación
ALTER TABLE frecuencia_viaje 
ADD COLUMN IF NOT EXISTS orden_dia INTEGER;

ALTER TABLE frecuencia_viaje 
ADD COLUMN IF NOT EXISTS requiere_bus_en_terminal BOOLEAN DEFAULT false;

ALTER TABLE frecuencia_viaje 
ADD COLUMN IF NOT EXISTS tiempo_minimo_espera_minutos INTEGER DEFAULT 30;

-- 3. Crear tabla de disponibilidad de buses
CREATE TABLE IF NOT EXISTS disponibilidad_bus (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL REFERENCES bus(id),
    cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id),
    terminal_id BIGINT NOT NULL REFERENCES terminal(id),
    fecha DATE NOT NULL,
    hora_llegada TIME,
    hora_disponible TIME NOT NULL,
    frecuencia_origen_id BIGINT REFERENCES frecuencia_viaje(id),
    frecuencia_siguiente_id BIGINT REFERENCES frecuencia_viaje(id),
    estado VARCHAR(32) NOT NULL DEFAULT 'PENDIENTE',
    tiempo_descanso_minutos INTEGER DEFAULT 45,
    en_rotacion BOOLEAN DEFAULT false,
    dia_rotacion INTEGER,
    observaciones TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 4. Índices para disponibilidad_bus
CREATE INDEX IF NOT EXISTS idx_disponibilidad_bus_fecha ON disponibilidad_bus(bus_id, fecha);
CREATE INDEX IF NOT EXISTS idx_disponibilidad_terminal_fecha ON disponibilidad_bus(terminal_id, fecha, hora_disponible);
CREATE INDEX IF NOT EXISTS idx_disponibilidad_estado ON disponibilidad_bus(estado, fecha);

-- 5. Crear tabla de ciclos de rotación (para importar desde CSV)
CREATE TABLE IF NOT EXISTS ciclo_rotacion (
    id BIGSERIAL PRIMARY KEY,
    cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    dias_ciclo INTEGER NOT NULL DEFAULT 23, -- Número de días del ciclo completo
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 6. Crear tabla de asignaciones de rotación (qué bus trabaja qué día del ciclo)
CREATE TABLE IF NOT EXISTS asignacion_rotacion (
    id BIGSERIAL PRIMARY KEY,
    ciclo_id BIGINT NOT NULL REFERENCES ciclo_rotacion(id),
    dia_ciclo INTEGER NOT NULL, -- 1, 2, 3... hasta dias_ciclo
    bus_id BIGINT NOT NULL REFERENCES bus(id),
    frecuencia_viaje_id BIGINT REFERENCES frecuencia_viaje(id),
    orden INTEGER NOT NULL DEFAULT 1, -- Orden dentro del día
    hora_salida TIME,
    terminal_origen_id BIGINT REFERENCES terminal(id),
    terminal_destino_id BIGINT REFERENCES terminal(id),
    observaciones TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ciclo_dia_orden UNIQUE (ciclo_id, dia_ciclo, bus_id, orden)
);

-- 7. Índices para rotación
CREATE INDEX IF NOT EXISTS idx_asignacion_ciclo_dia ON asignacion_rotacion(ciclo_id, dia_ciclo);
CREATE INDEX IF NOT EXISTS idx_asignacion_bus ON asignacion_rotacion(bus_id);

-- 8. Comentarios
COMMENT ON TABLE disponibilidad_bus IS 'Registro de disponibilidad de buses en terminales por fecha/hora';
COMMENT ON TABLE ciclo_rotacion IS 'Define ciclos de rotación de buses (ej: ciclo de 23 días)';
COMMENT ON TABLE asignacion_rotacion IS 'Asignaciones de buses a días específicos del ciclo';

COMMENT ON COLUMN frecuencia_viaje.tipo_frecuencia IS 'INTERPROVINCIAL o INTRAPROVINCIAL';
COMMENT ON COLUMN frecuencia_viaje.orden_dia IS 'Orden de la frecuencia en el día (para rotación)';
COMMENT ON COLUMN frecuencia_viaje.requiere_bus_en_terminal IS 'Si requiere que el bus esté físicamente en la terminal';
COMMENT ON COLUMN frecuencia_viaje.tiempo_minimo_espera_minutos IS 'Tiempo mínimo de espera antes de poder asignar otra frecuencia';
