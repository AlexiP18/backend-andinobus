-- Migración V21: Agregar campos de foto de perfil a usuario_cooperativa
-- Fecha: 2025-11-21
-- Descripción: Agrega columnas para almacenar la URL y nombre de archivo de la foto de perfil

-- Agregar columnas para foto de perfil
ALTER TABLE usuario_cooperativa
    ADD COLUMN foto_url VARCHAR(500),
    ADD COLUMN foto_filename VARCHAR(255);

-- Comentarios para documentación
COMMENT ON COLUMN usuario_cooperativa.foto_url IS 'URL pública de la foto de perfil del usuario';
COMMENT ON COLUMN usuario_cooperativa.foto_filename IS 'Nombre del archivo de la foto almacenado en el servidor';
