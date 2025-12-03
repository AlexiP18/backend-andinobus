-- V39: Agregar campo cooperativa_id a frecuencia_viaje
-- Este campo permite asociar una frecuencia directamente con una cooperativa

ALTER TABLE frecuencia_viaje ADD COLUMN IF NOT EXISTS cooperativa_id BIGINT;

-- Actualizar las frecuencias existentes con el cooperativa_id del bus
UPDATE frecuencia_viaje f
SET cooperativa_id = (SELECT b.cooperativa_id FROM bus b WHERE b.id = f.bus_id)
WHERE f.cooperativa_id IS NULL;

-- Agregar foreign key
ALTER TABLE frecuencia_viaje ADD CONSTRAINT fk_frecuencia_cooperativa 
    FOREIGN KEY (cooperativa_id) REFERENCES cooperativa(id);

-- Crear índice para mejorar búsquedas por cooperativa
CREATE INDEX IF NOT EXISTS idx_frecuencia_cooperativa ON frecuencia_viaje(cooperativa_id);
