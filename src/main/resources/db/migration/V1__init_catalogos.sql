-- Esquema inicial de catálogos
CREATE TABLE IF NOT EXISTS cooperativa (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    ruc             VARCHAR(13),
    logo_url        VARCHAR(500),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_cooperativa_ruc ON cooperativa (ruc) WHERE ruc IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_cooperativa_activo ON cooperativa (activo);

CREATE TABLE IF NOT EXISTS bus (
    id                BIGSERIAL PRIMARY KEY,
    cooperativa_id    BIGINT NOT NULL REFERENCES cooperativa(id),
    numero_interno    VARCHAR(50),
    placa             VARCHAR(20) NOT NULL,
    chasis_marca      VARCHAR(100),
    carroceria_marca  VARCHAR(100),
    foto_url          VARCHAR(500),
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_bus_placa ON bus (placa);
CREATE INDEX IF NOT EXISTS ix_bus_cooperativa ON bus (cooperativa_id);
CREATE INDEX IF NOT EXISTS ix_bus_activo ON bus (activo);

CREATE TABLE IF NOT EXISTS frecuencia (
    id                       BIGSERIAL PRIMARY KEY,
    cooperativa_id           BIGINT NOT NULL REFERENCES cooperativa(id),
    origen                   VARCHAR(120) NOT NULL,
    destino                  VARCHAR(120) NOT NULL,
    hora_salida              TIME NOT NULL,
    duracion_estimada_min    INTEGER,
    dias_operacion           VARCHAR(32), -- ej: LUN,MAR,MIÉ,JUE,VIER,SAB,DOM o máscara binaria posterior
    activa                   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS ix_frecuencia_cooperativa ON frecuencia (cooperativa_id);
CREATE INDEX IF NOT EXISTS ix_frecuencia_origen_destino ON frecuencia (origen, destino);

CREATE TABLE IF NOT EXISTS parada (
    id            BIGSERIAL PRIMARY KEY,
    frecuencia_id BIGINT NOT NULL REFERENCES frecuencia(id) ON DELETE CASCADE,
    ciudad        VARCHAR(120) NOT NULL,
    orden         INTEGER NOT NULL,
    hora_estimada TIME
);

CREATE INDEX IF NOT EXISTS ix_parada_frecuencia ON parada (frecuencia_id);
