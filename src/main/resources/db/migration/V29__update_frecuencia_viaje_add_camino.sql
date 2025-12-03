-- Agregar campo camino_id a frecuencia_viaje
ALTER TABLE frecuencia_viaje 
ADD COLUMN IF NOT EXISTS camino_id BIGINT REFERENCES camino(id) ON DELETE SET NULL;

-- Crear índice para mejorar consultas
CREATE INDEX IF NOT EXISTS idx_frecuencia_viaje_camino ON frecuencia_viaje(camino_id);

-- Comentario
COMMENT ON COLUMN frecuencia_viaje.camino_id IS 'Camino específico que sigue esta frecuencia (puede ser NULL para compatibilidad con datos antiguos)';
