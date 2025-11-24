-- V5: Cambiar numero_asiento de INTEGER a VARCHAR para soportar formato alfanumérico (1A, 2B, 3C, etc.)

-- 1. Alterar la columna numero_asiento de INTEGER a VARCHAR
ALTER TABLE viaje_asiento ALTER COLUMN numero_asiento TYPE VARCHAR(10) USING numero_asiento::VARCHAR;

-- 2. Actualizar datos existentes para agregar letra (convertir 1 -> 1A, 2 -> 2A, etc.)
-- Solo si hay datos, agregar sufijo 'A' a los números existentes
UPDATE viaje_asiento 
SET numero_asiento = numero_asiento || 'A' 
WHERE numero_asiento ~ '^[0-9]+$';

-- 3. Eliminar y recrear el constraint único para que funcione con VARCHAR
DROP INDEX IF EXISTS uq_viaje_asiento_numero;
CREATE UNIQUE INDEX uq_viaje_asiento_numero ON viaje_asiento (viaje_id, numero_asiento);
