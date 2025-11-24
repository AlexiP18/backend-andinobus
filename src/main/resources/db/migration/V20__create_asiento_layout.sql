-- V20: Tabla para configuración detallada de layout de asientos por bus

CREATE TABLE IF NOT EXISTS asiento_layout (
    id                  BIGSERIAL PRIMARY KEY,
    bus_id              BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE,
    numero_asiento      INTEGER NOT NULL CHECK (numero_asiento > 0),
    fila                INTEGER NOT NULL CHECK (fila >= 0),
    columna             INTEGER NOT NULL CHECK (columna >= 0),
    tipo_asiento        VARCHAR(32) NOT NULL DEFAULT 'NORMAL', -- NORMAL | VIP | ACONDICIONADO
    habilitado          BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uq_bus_numero_asiento UNIQUE (bus_id, numero_asiento),
    CONSTRAINT uq_bus_fila_columna UNIQUE (bus_id, fila, columna)
);

CREATE INDEX IF NOT EXISTS ix_asiento_layout_bus ON asiento_layout (bus_id);
CREATE INDEX IF NOT EXISTS ix_asiento_layout_habilitado ON asiento_layout (habilitado);
CREATE INDEX IF NOT EXISTS ix_asiento_layout_tipo ON asiento_layout (tipo_asiento);

COMMENT ON TABLE asiento_layout IS 'Configuración detallada del layout de asientos de cada bus';
COMMENT ON COLUMN asiento_layout.numero_asiento IS 'Número único del asiento en el bus (1, 2, 3, ...)';
COMMENT ON COLUMN asiento_layout.fila IS 'Fila del asiento en el layout (0-indexed)';
COMMENT ON COLUMN asiento_layout.columna IS 'Columna del asiento en el layout (0-indexed)';
COMMENT ON COLUMN asiento_layout.tipo_asiento IS 'NORMAL: asiento estándar, VIP: asiento premium, ACONDICIONADO: para personas con discapacidad o tercera edad';
COMMENT ON COLUMN asiento_layout.habilitado IS 'Si está habilitado para venta/reserva';
