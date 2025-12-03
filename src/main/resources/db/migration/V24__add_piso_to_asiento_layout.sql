-- V24__add_piso_to_asiento_layout.sql
-- Agrega el campo 'piso' a la tabla asiento_layout para identificar el nivel del asiento
-- Nota: Las tablas están en el esquema por defecto (public), por eso no se usa prefijo de esquema.

ALTER TABLE asiento_layout
ADD COLUMN IF NOT EXISTS piso INTEGER NOT NULL DEFAULT 1;

COMMENT ON COLUMN asiento_layout.piso IS 'Número de piso del asiento (1 o 2). Solo relevante para buses con dos niveles';
