-- V25__update_unique_constraint_with_piso.sql
-- Actualiza la restricción de unicidad para incluir el campo 'piso'
-- Esto permite que diferentes pisos tengan asientos en las mismas coordenadas (fila, columna)
-- Nota: Las tablas están en el esquema por defecto (public), por eso no se usa prefijo de esquema.

-- Eliminar la restricción antigua que solo considera bus_id, fila, columna
ALTER TABLE asiento_layout
DROP CONSTRAINT IF EXISTS uq_bus_fila_columna;

-- Asegurar que no exista previamente la restricción nueva (por intentos previos)
ALTER TABLE asiento_layout
DROP CONSTRAINT IF EXISTS uq_bus_piso_fila_columna;

-- Crear nueva restricción que incluye el piso
ALTER TABLE asiento_layout
ADD CONSTRAINT uq_bus_piso_fila_columna UNIQUE (bus_id, piso, fila, columna);

COMMENT ON CONSTRAINT uq_bus_piso_fila_columna ON asiento_layout IS 
'Garantiza que no haya asientos duplicados en la misma posición (fila, columna) dentro del mismo piso de un bus';
