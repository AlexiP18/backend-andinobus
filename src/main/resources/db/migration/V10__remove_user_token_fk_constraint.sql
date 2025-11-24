-- V10: Eliminar restricción de clave foránea en user_token para permitir tokens del Super Admin
-- Fecha: 2025-11-16
-- Autor: Sistema

-- 1. Eliminar la restricción de clave foránea existente
ALTER TABLE user_token DROP CONSTRAINT IF EXISTS user_token_user_id_fkey;

-- 2. Comentario explicativo sobre la nueva estructura
COMMENT ON COLUMN user_token.user_id IS 'ID de usuario. Puede ser:
- ID positivo de app_user (rol CLIENTE)
- ID positivo de usuario_cooperativa (roles COOPERATIVA)
- -1 para Super Admin (usuario hardcodeado sin tabla)';

-- 3. Crear índice para optimizar búsquedas
CREATE INDEX IF NOT EXISTS ix_user_token_user_id ON user_token (user_id);
