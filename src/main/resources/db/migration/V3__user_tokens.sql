-- V3: Tabla para almacenar tokens de usuario
-- Ejecutado por Flyway con perfil 'dev'

CREATE TABLE IF NOT EXISTS user_token (
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(255) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_user_token_token ON user_token (token);
CREATE INDEX IF NOT EXISTS ix_user_token_user ON user_token (user_id);
CREATE INDEX IF NOT EXISTS ix_user_token_expires ON user_token (expires_at);
