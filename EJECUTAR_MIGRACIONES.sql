-- =====================================================
-- EJECUTAR ESTE SCRIPT EN LA BASE DE DATOS das_dev
-- para agregar las columnas faltantes
-- =====================================================

-- V40: Campos de capacidad operativa
ALTER TABLE frecuencia_config_cooperativa ADD COLUMN IF NOT EXISTS descanso_interprovincial_minutos INTEGER DEFAULT 120;
ALTER TABLE frecuencia_config_cooperativa ADD COLUMN IF NOT EXISTS descanso_intraprovincial_minutos INTEGER DEFAULT 45;
ALTER TABLE frecuencia_config_cooperativa ADD COLUMN IF NOT EXISTS umbral_interprovincial_km DECIMAL(10,2) DEFAULT 100.0;
ALTER TABLE frecuencia_config_cooperativa ADD COLUMN IF NOT EXISTS semanas_planificacion_defecto INTEGER DEFAULT 1;
ALTER TABLE frecuencia_config_cooperativa ADD COLUMN IF NOT EXISTS semanas_planificacion_max INTEGER DEFAULT 4;

-- V41: Sistema de rotación de buses y tipos de frecuencia

-- 0. Agregar campo terminal_base a bus
ALTER TABLE bus ADD COLUMN IF NOT EXISTS terminal_base_id BIGINT REFERENCES terminal(id);

-- 1. Agregar campo tipo_frecuencia a frecuencia_viaje
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS tipo_frecuencia VARCHAR(32) DEFAULT 'INTERPROVINCIAL';

-- 2. Agregar campos de control de rotación
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS orden_dia INTEGER;
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS requiere_bus_en_terminal BOOLEAN DEFAULT false;
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS tiempo_minimo_espera_minutos INTEGER DEFAULT 30;
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'ACTIVA';
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS cooperativa_id BIGINT REFERENCES cooperativa(id);
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS terminal_origen_id BIGINT REFERENCES terminal(id);
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS terminal_destino_id BIGINT REFERENCES terminal(id);
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS camino_id BIGINT;

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

-- 5. Crear tabla de ciclos de rotación
CREATE TABLE IF NOT EXISTS ciclo_rotacion (
    id BIGSERIAL PRIMARY KEY,
    cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    dias_ciclo INTEGER NOT NULL DEFAULT 23,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 6. Crear tabla de asignaciones de rotación
CREATE TABLE IF NOT EXISTS asignacion_rotacion (
    id BIGSERIAL PRIMARY KEY,
    ciclo_id BIGINT NOT NULL REFERENCES ciclo_rotacion(id),
    dia_ciclo INTEGER NOT NULL,
    bus_id BIGINT NOT NULL REFERENCES bus(id),
    frecuencia_viaje_id BIGINT REFERENCES frecuencia_viaje(id),
    orden INTEGER NOT NULL DEFAULT 1,
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

-- 8. Actualizar registro de Flyway para V40 y V41
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT 
    (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
    '40',
    'add capacidad operativa fields',
    'SQL',
    'V40__add_capacidad_operativa_fields.sql',
    NULL,
    'postgres',
    NOW(),
    100,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '40');

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT 
    (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
    '41',
    'add rotacion buses sistema',
    'SQL',
    'V41__add_rotacion_buses_sistema.sql',
    NULL,
    'postgres',
    NOW(),
    100,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '41');

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
SELECT 'Migraciones aplicadas correctamente' AS resultado;
