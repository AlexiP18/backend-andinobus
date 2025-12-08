-- V47: Crear tabla confirmacion_token para verificación de email de clientes
-- También agregar campo email_confirmado a app_user

-- Tabla para tokens de confirmación de email
CREATE TABLE IF NOT EXISTS confirmacion_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(180) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    
    CONSTRAINT fk_confirmacion_token_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Índices para búsquedas eficientes
CREATE INDEX IF NOT EXISTS idx_confirmacion_token_token ON confirmacion_token(token);
CREATE INDEX IF NOT EXISTS idx_confirmacion_token_user_id ON confirmacion_token(user_id);
CREATE INDEX IF NOT EXISTS idx_confirmacion_token_expires_at ON confirmacion_token(expires_at);

-- Agregar campo email_confirmado a app_user
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS email_confirmado BOOLEAN DEFAULT FALSE;

-- Los usuarios existentes se marcan como confirmados (para no afectar cuentas existentes)
UPDATE app_user SET email_confirmado = TRUE WHERE email_confirmado IS NULL OR email_confirmado = FALSE;

COMMENT ON TABLE confirmacion_token IS 'Tokens para confirmación de email de clientes';
COMMENT ON COLUMN confirmacion_token.token IS 'Token único UUID para confirmación';
COMMENT ON COLUMN confirmacion_token.user_id IS 'ID del usuario (app_user) asociado';
COMMENT ON COLUMN confirmacion_token.expires_at IS 'Fecha de expiración del token (24 horas por defecto)';
COMMENT ON COLUMN confirmacion_token.confirmed_at IS 'Fecha en que se confirmó el email (NULL si no confirmado)';
COMMENT ON COLUMN app_user.email_confirmado IS 'Indica si el usuario ha confirmado su email';
