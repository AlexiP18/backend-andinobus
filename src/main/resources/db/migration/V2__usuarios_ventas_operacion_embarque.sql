-- V2: Esquema mínimo para Usuarios, Ventas, Operación y Embarque
-- Nota: usa PostgreSQL. Ejecutado por Flyway con perfil 'dev'.

-- Usuarios básicos (para futuras integraciones; los endpoints actuales son stubs)
CREATE TABLE IF NOT EXISTS app_user (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(180) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    nombres         VARCHAR(120),
    apellidos       VARCHAR(120),
    rol             VARCHAR(32) NOT NULL DEFAULT 'CLIENTE', -- ADMIN | CLIENTE
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Operación: hoja de ruta y viajes programados
CREATE TABLE IF NOT EXISTS hoja_ruta (
    id              BIGSERIAL PRIMARY KEY,
    fecha           DATE NOT NULL,
    cooperativa_id  BIGINT REFERENCES cooperativa(id),
    estado          VARCHAR(32) NOT NULL DEFAULT 'GENERADA', -- GENERADA | PUBLICADA | CERRADA
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_hoja_ruta_fecha ON hoja_ruta (fecha);
CREATE INDEX IF NOT EXISTS ix_hoja_ruta_coop ON hoja_ruta (cooperativa_id);

CREATE TABLE IF NOT EXISTS viaje (
    id              BIGSERIAL PRIMARY KEY,
    hoja_ruta_id    BIGINT REFERENCES hoja_ruta(id) ON DELETE SET NULL,
    frecuencia_id   BIGINT NOT NULL REFERENCES frecuencia(id) ON DELETE RESTRICT,
    bus_id          BIGINT REFERENCES bus(id) ON DELETE SET NULL,
    fecha           DATE NOT NULL,
    hora_salida     TIME NOT NULL,
    estado          VARCHAR(32) NOT NULL DEFAULT 'PROGRAMADO', -- PROGRAMADO | EN_CURSO | FINALIZADO | CANCELADO
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_viaje_fecha ON viaje (fecha);
CREATE INDEX IF NOT EXISTS ix_viaje_frecuencia ON viaje (frecuencia_id);

-- Ventas: reservas y boletos
CREATE TABLE IF NOT EXISTS reserva (
    id              BIGSERIAL PRIMARY KEY,
    viaje_id        BIGINT NOT NULL REFERENCES viaje(id) ON DELETE CASCADE,
    cliente_email   VARCHAR(180),
    asientos        INTEGER NOT NULL CHECK (asientos > 0),
    estado          VARCHAR(32) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE | PAGADO | CANCELADO | EXPIRADO
    monto           NUMERIC(12,2),
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_reserva_viaje ON reserva (viaje_id);
CREATE INDEX IF NOT EXISTS ix_reserva_estado ON reserva (estado);

CREATE TABLE IF NOT EXISTS boleto (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(32) NOT NULL UNIQUE,
    reserva_id      BIGINT NOT NULL REFERENCES reserva(id) ON DELETE CASCADE,
    estado          VARCHAR(32) NOT NULL DEFAULT 'EMITIDO', -- EMITIDO | ANULADO | USADO
    qr_data         VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_boleto_reserva ON boleto (reserva_id);

-- Embarque: bitácora de escaneos
CREATE TABLE IF NOT EXISTS embarque_scan_log (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(32) NOT NULL,
    resultado       VARCHAR(16) NOT NULL, -- valido|invalido|usado
    message         VARCHAR(255),
    scanned_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_embarque_codigo ON embarque_scan_log (codigo);

-- Datos mínimos de ejemplo (opcionales)
INSERT INTO app_user (email, password_hash, nombres, apellidos, rol)
VALUES
    ('admin@example.com', null, 'Admin', 'Demo', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (email, password_hash, nombres, apellidos, rol)
VALUES
    ('cliente@example.com', null, 'Cliente', 'Demo', 'CLIENTE')
ON CONFLICT (email) DO NOTHING;
