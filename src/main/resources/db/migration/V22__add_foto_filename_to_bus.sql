-- Agregar columna foto_filename a la tabla bus
-- Esta columna almacenará el nombre del archivo de la foto del bus en el sistema de archivos

ALTER TABLE bus
    ADD COLUMN foto_filename VARCHAR(255);

COMMENT ON COLUMN bus.foto_filename IS 'Nombre del archivo almacenado en el sistema de archivos (ej: bus_123_1234567890.jpg)';
