-- Migración V7: Gestión de Cooperativa - Paradas Intermedias, Asignaciones y Días de Parada

-- Agregar campos nuevos a la tabla Bus
ALTER TABLE bus ADD COLUMN IF NOT EXISTS capacidad_asientos INTEGER DEFAULT 40;
ALTER TABLE bus ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'DISPONIBLE';

-- Tabla de paradas intermedias en frecuencias
CREATE TABLE IF NOT EXISTS parada_intermedia (
    id BIGSERIAL PRIMARY KEY,
    frecuencia_id BIGINT NOT NULL,
    ciudad VARCHAR(120) NOT NULL,
    orden_parada INTEGER NOT NULL,
    minutos_desde_origen INTEGER NOT NULL,
    precio_adicional DECIMAL(10,2),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parada_frecuencia FOREIGN KEY (frecuencia_id) REFERENCES frecuencia(id) ON DELETE CASCADE
);

CREATE INDEX idx_parada_frecuencia ON parada_intermedia(frecuencia_id);
CREATE INDEX idx_parada_orden ON parada_intermedia(frecuencia_id, orden_parada);

-- Tabla de asignación de buses a frecuencias
CREATE TABLE IF NOT EXISTS asignacion_bus_frecuencia (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL,
    frecuencia_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(32) NOT NULL DEFAULT 'ACTIVA',
    observaciones VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asignacion_bus FOREIGN KEY (bus_id) REFERENCES bus(id) ON DELETE CASCADE,
    CONSTRAINT fk_asignacion_frecuencia FOREIGN KEY (frecuencia_id) REFERENCES frecuencia(id) ON DELETE CASCADE
);

CREATE INDEX idx_asignacion_bus ON asignacion_bus_frecuencia(bus_id);
CREATE INDEX idx_asignacion_frecuencia ON asignacion_bus_frecuencia(frecuencia_id);
CREATE INDEX idx_asignacion_estado ON asignacion_bus_frecuencia(estado);

-- Tabla de días de parada para buses
CREATE TABLE IF NOT EXISTS dia_parada_bus (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    motivo VARCHAR(32) NOT NULL,
    observaciones VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parada_bus FOREIGN KEY (bus_id) REFERENCES bus(id) ON DELETE CASCADE
);

CREATE INDEX idx_parada_bus_fecha ON dia_parada_bus(bus_id, fecha);

-- Actualizar estados de buses existentes
UPDATE bus SET estado = 'DISPONIBLE' WHERE estado IS NULL;
UPDATE bus SET capacidad_asientos = 40 WHERE capacidad_asientos IS NULL;

COMMENT ON TABLE parada_intermedia IS 'Paradas intermedias en frecuencias (ej: Quito-Loja pasa por Latacunga, Riobamba, Cuenca)';
COMMENT ON TABLE asignacion_bus_frecuencia IS 'Asignación de buses a frecuencias específicas';
COMMENT ON TABLE dia_parada_bus IS 'Días de parada programados para buses (mantenimiento, exceso capacidad, etc)';
COMMENT ON COLUMN bus.estado IS 'DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA';
COMMENT ON COLUMN asignacion_bus_frecuencia.estado IS 'ACTIVA | SUSPENDIDA | FINALIZADA';
COMMENT ON COLUMN dia_parada_bus.motivo IS 'MANTENIMIENTO | EXCESO_CAPACIDAD | OTRO';
