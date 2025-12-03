-- Migración V37: Mejoras al sistema de frecuencias
-- Agregamos campos para soporte de terminales de cooperativa, choferes y validaciones

-- Agregar campo chofer a frecuencia_viaje (el chofer principal asignado a esta frecuencia)
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS chofer_id BIGINT;
ALTER TABLE frecuencia_viaje ADD CONSTRAINT fk_frecuencia_chofer 
    FOREIGN KEY (chofer_id) REFERENCES usuario_cooperativa(id);

-- Agregar campos para control de operación
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS duracion_estimada_minutos INTEGER;
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS kilometros_ruta DOUBLE PRECISION;
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS costo_combustible_estimado DOUBLE PRECISION;
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS tiempo_parada_minutos INTEGER DEFAULT 15; -- Tiempo de parada al llegar a destino

-- Estado de la frecuencia
ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'ACTIVA';
-- Estados: ACTIVA, PAUSADA, CANCELADA, EN_MANTENIMIENTO

-- Agregar referencia a parada_frecuencia con terminal (para que las paradas sean en terminales de la cooperativa)
ALTER TABLE parada_frecuencia ADD COLUMN IF NOT EXISTS terminal_id BIGINT;
ALTER TABLE parada_frecuencia ADD CONSTRAINT fk_parada_terminal 
    FOREIGN KEY (terminal_id) REFERENCES terminal(id);

-- Crear tabla de configuración de frecuencias automáticas por cooperativa
CREATE TABLE IF NOT EXISTS frecuencia_config_cooperativa (
    id BIGSERIAL PRIMARY KEY,
    cooperativa_id BIGINT NOT NULL UNIQUE,
    
    -- Configuración de precios
    precio_base_por_km DOUBLE PRECISION DEFAULT 0.02, -- Precio base por kilómetro
    factor_diesel_por_km DOUBLE PRECISION DEFAULT 0.12, -- Litros de diesel por km
    precio_diesel DOUBLE PRECISION DEFAULT 1.80, -- Precio actual del diesel
    margen_ganancia_porcentaje DOUBLE PRECISION DEFAULT 30.0, -- Porcentaje de ganancia sobre costo
    
    -- Configuración de choferes
    max_horas_diarias_chofer INTEGER DEFAULT 8, -- Horas máximas por día para un chofer
    max_horas_excepcionales INTEGER DEFAULT 10, -- Horas máximas excepcionales
    max_dias_excepcionales_semana INTEGER DEFAULT 2, -- Días a la semana que puede trabajar horas excepcionales
    tiempo_descanso_entre_viajes_minutos INTEGER DEFAULT 30, -- Tiempo mínimo entre viajes para un chofer
    
    -- Configuración de buses
    tiempo_minimo_parada_bus_minutos INTEGER DEFAULT 15, -- Tiempo mínimo de parada del bus entre viajes
    horas_operacion_max_bus INTEGER DEFAULT 24, -- Horas de operación máxima del bus por día
    
    -- Configuración de generación automática
    intervalo_minimo_frecuencias_minutos INTEGER DEFAULT 30, -- Intervalo mínimo entre frecuencias de la misma ruta
    hora_inicio_operacion TIME DEFAULT '05:00', -- Hora de inicio de operaciones
    hora_fin_operacion TIME DEFAULT '23:00', -- Hora de fin de operaciones
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_config_cooperativa FOREIGN KEY (cooperativa_id) 
        REFERENCES cooperativa(id) ON DELETE CASCADE
);

-- Crear tabla para registro de horas trabajadas de choferes
CREATE TABLE IF NOT EXISTS chofer_horas_trabajadas (
    id BIGSERIAL PRIMARY KEY,
    chofer_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    horas_trabajadas DOUBLE PRECISION DEFAULT 0,
    horas_excepcionales BOOLEAN DEFAULT FALSE,
    frecuencias_realizadas INTEGER DEFAULT 0,
    km_recorridos DOUBLE PRECISION DEFAULT 0,
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_horas_chofer FOREIGN KEY (chofer_id) 
        REFERENCES usuario_cooperativa(id) ON DELETE CASCADE,
    CONSTRAINT uk_chofer_fecha UNIQUE (chofer_id, fecha)
);

-- Crear tabla para registro de operación de buses
CREATE TABLE IF NOT EXISTS bus_operacion_diaria (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    horas_operacion DOUBLE PRECISION DEFAULT 0,
    frecuencias_realizadas INTEGER DEFAULT 0,
    km_recorridos DOUBLE PRECISION DEFAULT 0,
    estado_bus VARCHAR(32) DEFAULT 'DISPONIBLE', -- DISPONIBLE, EN_RUTA, EN_MANTENIMIENTO, PARADA
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_operacion_bus FOREIGN KEY (bus_id) 
        REFERENCES bus(id) ON DELETE CASCADE,
    CONSTRAINT uk_bus_fecha UNIQUE (bus_id, fecha)
);

-- Crear tabla de plantilla de frecuencias (para generación automática basada en CSV)
CREATE TABLE IF NOT EXISTS frecuencia_plantilla (
    id BIGSERIAL PRIMARY KEY,
    cooperativa_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    
    -- Patrón de rotación
    dia_ciclo INTEGER NOT NULL, -- Día 1, 2, 3... del ciclo
    hora_salida TIME NOT NULL,
    terminal_origen_id BIGINT NOT NULL,
    terminal_destino_id BIGINT NOT NULL,
    
    -- Opcionales
    duracion_estimada_minutos INTEGER,
    precio_base_sugerido DOUBLE PRECISION,
    
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_plantilla_cooperativa FOREIGN KEY (cooperativa_id) 
        REFERENCES cooperativa(id) ON DELETE CASCADE,
    CONSTRAINT fk_plantilla_origen FOREIGN KEY (terminal_origen_id) 
        REFERENCES terminal(id),
    CONSTRAINT fk_plantilla_destino FOREIGN KEY (terminal_destino_id) 
        REFERENCES terminal(id)
);

-- Crear tabla de asignación de buses a plantillas
CREATE TABLE IF NOT EXISTS frecuencia_plantilla_bus (
    id BIGSERIAL PRIMARY KEY,
    plantilla_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    orden_rotacion INTEGER DEFAULT 1, -- Orden en la rotación (basado en los números del CSV)
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_plantilla_bus_plantilla FOREIGN KEY (plantilla_id) 
        REFERENCES frecuencia_plantilla(id) ON DELETE CASCADE,
    CONSTRAINT fk_plantilla_bus_bus FOREIGN KEY (bus_id) 
        REFERENCES bus(id) ON DELETE CASCADE,
    CONSTRAINT uk_plantilla_bus UNIQUE (plantilla_id, bus_id)
);

-- Índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_frecuencia_chofer ON frecuencia_viaje(chofer_id);
CREATE INDEX IF NOT EXISTS idx_frecuencia_estado ON frecuencia_viaje(estado);
CREATE INDEX IF NOT EXISTS idx_parada_terminal ON parada_frecuencia(terminal_id);
CREATE INDEX IF NOT EXISTS idx_horas_chofer_fecha ON chofer_horas_trabajadas(chofer_id, fecha);
CREATE INDEX IF NOT EXISTS idx_bus_operacion_fecha ON bus_operacion_diaria(bus_id, fecha);
CREATE INDEX IF NOT EXISTS idx_plantilla_cooperativa ON frecuencia_plantilla(cooperativa_id);

-- Insertar configuración por defecto para cooperativas existentes
INSERT INTO frecuencia_config_cooperativa (cooperativa_id)
SELECT id FROM cooperativa 
WHERE id NOT IN (SELECT cooperativa_id FROM frecuencia_config_cooperativa)
ON CONFLICT DO NOTHING;

-- Comentarios
COMMENT ON TABLE frecuencia_config_cooperativa IS 'Configuración de parámetros para generación de frecuencias por cooperativa';
COMMENT ON TABLE chofer_horas_trabajadas IS 'Registro diario de horas trabajadas por chofer para control de jornada laboral';
COMMENT ON TABLE bus_operacion_diaria IS 'Registro diario de operación de buses';
COMMENT ON TABLE frecuencia_plantilla IS 'Plantillas de frecuencias para generación automática basada en patrones de rotación';
COMMENT ON COLUMN frecuencia_config_cooperativa.factor_diesel_por_km IS 'Consumo estimado de diesel por kilómetro recorrido';
