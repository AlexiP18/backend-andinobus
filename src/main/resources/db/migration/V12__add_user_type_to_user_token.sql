-- V12: Agregar campo user_type a user_token
-- Fecha: 2025-11-16
-- Descripción: Permite diferenciar entre CLIENTE, COOPERATIVA y ADMIN al validar tokens

ALTER TABLE user_token ADD COLUMN IF NOT EXISTS user_type VARCHAR(20) DEFAULT 'CLIENTE';

-- Índice para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_user_token_user_type ON user_token(user_type);

COMMENT ON COLUMN user_token.user_type IS 'Tipo de usuario: CLIENTE, COOPERATIVA, ADMIN';
