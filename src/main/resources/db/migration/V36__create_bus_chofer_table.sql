-- Migración V36: Crear tabla de relación bus-chofer
-- Permite asignar hasta 3 choferes por bus

CREATE TABLE IF NOT EXISTS bus_chofer (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL,
    chofer_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'ALTERNO', -- PRINCIPAL, ALTERNO
    orden INTEGER NOT NULL DEFAULT 1, -- 1, 2, 3 para determinar prioridad
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_bus_chofer_bus FOREIGN KEY (bus_id) 
        REFERENCES bus(id) ON DELETE CASCADE,
    CONSTRAINT fk_bus_chofer_usuario FOREIGN KEY (chofer_id) 
        REFERENCES usuario_cooperativa(id) ON DELETE CASCADE,
    
    -- Un chofer solo puede estar asignado una vez a un bus
    CONSTRAINT uk_bus_chofer UNIQUE (bus_id, chofer_id)
);

-- Crear índice único parcial para garantizar un solo chofer PRINCIPAL por bus
CREATE UNIQUE INDEX IF NOT EXISTS idx_bus_chofer_principal_unico 
    ON bus_chofer(bus_id) WHERE tipo = 'PRINCIPAL' AND activo = TRUE;

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_bus_chofer_bus ON bus_chofer(bus_id);
CREATE INDEX IF NOT EXISTS idx_bus_chofer_chofer ON bus_chofer(chofer_id);
CREATE INDEX IF NOT EXISTS idx_bus_chofer_activo ON bus_chofer(activo);

-- Comentarios
COMMENT ON TABLE bus_chofer IS 'Relación entre buses y choferes. Máximo 3 choferes por bus.';
COMMENT ON COLUMN bus_chofer.tipo IS 'Tipo de asignación: PRINCIPAL (1 por bus) o ALTERNO';
COMMENT ON COLUMN bus_chofer.orden IS 'Orden de prioridad del chofer: 1=principal, 2=primer alterno, 3=segundo alterno';
