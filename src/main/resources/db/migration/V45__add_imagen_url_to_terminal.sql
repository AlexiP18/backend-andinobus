-- V45: Agregar columna imagen_url a la tabla terminal
-- Permite almacenar una URL o base64 de la imagen del terminal

ALTER TABLE terminal 
ADD COLUMN IF NOT EXISTS imagen_url TEXT;

-- Comentario descriptivo
COMMENT ON COLUMN terminal.imagen_url IS 'URL de la imagen del terminal (puede ser URL externa o base64)';

-- Notificación
DO $$
BEGIN
    RAISE NOTICE 'Columna imagen_url agregada a la tabla terminal';
END $$;
