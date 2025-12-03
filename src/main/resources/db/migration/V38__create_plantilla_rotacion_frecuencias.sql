-- V38: Crear tabla para plantillas de rotación de frecuencias
-- Permite guardar patrones de rotación importados desde CSV

CREATE TABLE IF NOT EXISTS plantilla_rotacion_frecuencias (
    id BIGSERIAL PRIMARY KEY,
    cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id),
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    total_turnos INTEGER NOT NULL,
    turnos_json TEXT, -- JSON con la definición de turnos
    activa BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT uk_plantilla_cooperativa_nombre UNIQUE (cooperativa_id, nombre)
);

-- Índices
-- Nota: nombres de índices únicos para evitar colisión con V37
CREATE INDEX IF NOT EXISTS idx_prf_cooperativa ON plantilla_rotacion_frecuencias(cooperativa_id);
CREATE INDEX IF NOT EXISTS idx_prf_coop_activa ON plantilla_rotacion_frecuencias(cooperativa_id, activa);

COMMENT ON TABLE plantilla_rotacion_frecuencias IS 'Plantillas de rotación de frecuencias importadas desde CSV';
COMMENT ON COLUMN plantilla_rotacion_frecuencias.total_turnos IS 'Número total de turnos/días en el ciclo de rotación';
COMMENT ON COLUMN plantilla_rotacion_frecuencias.turnos_json IS 'JSON con estructura de turnos: [{numeroDia, horaSalida, origen, destino, esParada, subTurnos}]';
