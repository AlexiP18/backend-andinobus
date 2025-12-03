-- Agregar campos para soportar buses de dos niveles
-- Fecha: 2025-11-25

-- Nota: En este proyecto las tablas están en el esquema por defecto (public).
-- Por eso referimos a la tabla "bus" sin prefijo de esquema.
ALTER TABLE bus
ADD COLUMN IF NOT EXISTS tiene_dos_niveles BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS capacidad_piso_1 INTEGER,
ADD COLUMN IF NOT EXISTS capacidad_piso_2 INTEGER;

-- Actualizar buses existentes: toda la capacidad va al piso 1
-- Solo para filas antiguas donde aún no se han establecido las nuevas columnas
UPDATE bus
SET tiene_dos_niveles = FALSE,
    capacidad_piso_1 = capacidad_asientos,
    capacidad_piso_2 = 0
WHERE capacidad_piso_1 IS NULL AND capacidad_piso_2 IS NULL;

-- Comentarios
COMMENT ON COLUMN bus.tiene_dos_niveles IS 'Indica si el bus tiene dos niveles/pisos';
COMMENT ON COLUMN bus.capacidad_piso_1 IS 'Capacidad de asientos en el primer piso';
COMMENT ON COLUMN bus.capacidad_piso_2 IS 'Capacidad de asientos en el segundo piso';
