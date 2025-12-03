-- V35: Relaciones entre Cooperativas/Usuarios y Terminales
-- Permite establecer en qué terminales opera cada cooperativa y trabaja cada oficinista

-- =====================================================
-- TABLA: cooperativa_terminal
-- Relación muchos a muchos entre cooperativas y terminales
-- =====================================================
CREATE TABLE IF NOT EXISTS cooperativa_terminal (
    id BIGSERIAL PRIMARY KEY,
    cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id) ON DELETE CASCADE,
    terminal_id BIGINT NOT NULL REFERENCES terminal(id) ON DELETE CASCADE,
    -- Información adicional
    es_sede_principal BOOLEAN DEFAULT FALSE,
    numero_andenes_asignados INTEGER DEFAULT 0,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Evitar duplicados
    CONSTRAINT uq_cooperativa_terminal UNIQUE (cooperativa_id, terminal_id)
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_cooperativa_terminal_cooperativa ON cooperativa_terminal(cooperativa_id);
CREATE INDEX IF NOT EXISTS idx_cooperativa_terminal_terminal ON cooperativa_terminal(terminal_id);
CREATE INDEX IF NOT EXISTS idx_cooperativa_terminal_activo ON cooperativa_terminal(activo);

COMMENT ON TABLE cooperativa_terminal IS 'Relación entre cooperativas y terminales donde operan';
COMMENT ON COLUMN cooperativa_terminal.es_sede_principal IS 'Indica si este terminal es la sede principal de la cooperativa';
COMMENT ON COLUMN cooperativa_terminal.numero_andenes_asignados IS 'Número de andenes asignados a la cooperativa en este terminal';

-- =====================================================
-- TABLA: usuario_terminal
-- Relación muchos a muchos entre usuarios (oficinistas) y terminales
-- =====================================================
CREATE TABLE IF NOT EXISTS usuario_terminal (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    terminal_id BIGINT NOT NULL REFERENCES terminal(id) ON DELETE CASCADE,
    cooperativa_id BIGINT REFERENCES cooperativa(id) ON DELETE SET NULL,
    -- Información del puesto
    cargo VARCHAR(100) DEFAULT 'Oficinista',
    turno VARCHAR(50), -- 'MAÑANA', 'TARDE', 'NOCHE', 'COMPLETO'
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Evitar duplicados
    CONSTRAINT uq_usuario_terminal UNIQUE (usuario_id, terminal_id)
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_usuario_terminal_usuario ON usuario_terminal(usuario_id);
CREATE INDEX IF NOT EXISTS idx_usuario_terminal_terminal ON usuario_terminal(terminal_id);
CREATE INDEX IF NOT EXISTS idx_usuario_terminal_cooperativa ON usuario_terminal(cooperativa_id);
CREATE INDEX IF NOT EXISTS idx_usuario_terminal_activo ON usuario_terminal(activo);

COMMENT ON TABLE usuario_terminal IS 'Relación entre usuarios (oficinistas) y terminales donde trabajan';
COMMENT ON COLUMN usuario_terminal.cargo IS 'Cargo del usuario en el terminal';
COMMENT ON COLUMN usuario_terminal.turno IS 'Turno de trabajo: MAÑANA, TARDE, NOCHE, COMPLETO';

-- =====================================================
-- Función para actualizar updated_at automáticamente
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para actualizar updated_at
DROP TRIGGER IF EXISTS update_cooperativa_terminal_updated_at ON cooperativa_terminal;
CREATE TRIGGER update_cooperativa_terminal_updated_at
    BEFORE UPDATE ON cooperativa_terminal
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_usuario_terminal_updated_at ON usuario_terminal;
CREATE TRIGGER update_usuario_terminal_updated_at
    BEFORE UPDATE ON usuario_terminal
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Estadísticas
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'TABLAS DE RELACIÓN CREADAS EXITOSAMENTE';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'cooperativa_terminal: Cooperativas <-> Terminales';
    RAISE NOTICE 'usuario_terminal: Oficinistas <-> Terminales';
    RAISE NOTICE '==========================================';
END $$;
