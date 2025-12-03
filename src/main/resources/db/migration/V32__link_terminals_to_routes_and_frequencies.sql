-- V32: Vincular terminales con frecuencias y rutas
-- Agregar referencias a terminales de origen y destino

-- Agregar columnas de terminal a frecuencia_viaje
ALTER TABLE frecuencia_viaje 
ADD COLUMN terminal_origen_id BIGINT REFERENCES terminal(id),
ADD COLUMN terminal_destino_id BIGINT REFERENCES terminal(id);

-- Agregar columnas de terminal a ruta
ALTER TABLE ruta
ADD COLUMN terminal_origen_id BIGINT REFERENCES terminal(id),
ADD COLUMN terminal_destino_id BIGINT REFERENCES terminal(id);

-- Crear índices para las nuevas columnas
CREATE INDEX idx_frecuencia_viaje_terminal_origen ON frecuencia_viaje(terminal_origen_id);
CREATE INDEX idx_frecuencia_viaje_terminal_destino ON frecuencia_viaje(terminal_destino_id);
CREATE INDEX idx_ruta_terminal_origen ON ruta(terminal_origen_id);
CREATE INDEX idx_ruta_terminal_destino ON ruta(terminal_destino_id);

-- Crear tabla para tracking de ocupación de frecuencias por terminal
CREATE TABLE ocupacion_terminal (
    id BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL REFERENCES terminal(id),
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    frecuencias_asignadas INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ocupacion_terminal_fecha_hora UNIQUE (terminal_id, fecha, hora)
);

CREATE INDEX idx_ocupacion_terminal_fecha ON ocupacion_terminal(terminal_id, fecha);

COMMENT ON TABLE ocupacion_terminal IS 'Registro de ocupación de frecuencias por terminal y hora';
COMMENT ON COLUMN ocupacion_terminal.frecuencias_asignadas IS 'Número de frecuencias asignadas para esta hora en este terminal';

-- Vincular terminales existentes con rutas basándose en origen/destino
-- Quito - Guayaquil (Ruta 1)
UPDATE ruta SET terminal_origen_id = (SELECT id FROM terminal WHERE nombre = 'Quitumbe' LIMIT 1),
                terminal_destino_id = (SELECT id FROM terminal WHERE nombre = 'Jaime Roldós Aguilera' LIMIT 1)
WHERE nombre LIKE '%Quito%Guayaquil%' OR (origen LIKE '%Quito%' AND destino LIKE '%Guayaquil%');

-- Quito - Cuenca (Ruta 2) 
UPDATE ruta SET terminal_origen_id = (SELECT id FROM terminal WHERE nombre = 'Quitumbe' LIMIT 1),
                terminal_destino_id = (SELECT id FROM terminal WHERE nombre = 'Cuenca' LIMIT 1)
WHERE nombre LIKE '%Quito%Cuenca%' OR (origen LIKE '%Quito%' AND destino LIKE '%Cuenca%');

-- Guayaquil - Machala (Ruta 3)
UPDATE ruta SET terminal_origen_id = (SELECT id FROM terminal WHERE nombre = 'Jaime Roldós Aguilera' LIMIT 1),
                terminal_destino_id = (SELECT id FROM terminal WHERE nombre = 'Machala' LIMIT 1)
WHERE nombre LIKE '%Guayaquil%Machala%' OR (origen LIKE '%Guayaquil%' AND destino LIKE '%Machala%');
