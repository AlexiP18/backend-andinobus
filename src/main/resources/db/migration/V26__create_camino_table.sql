-- Crear tabla camino para almacenar rutas físicas específicas
CREATE TABLE IF NOT EXISTS camino (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES ruta(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    distancia_km DECIMAL(10,2),
    duracion_minutos INTEGER,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('RAPIDO', 'NORMAL', 'TURISTICO', 'ECONOMICO')),
    polyline TEXT, -- Polyline codificado (Google Maps format)
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_camino_ruta ON camino(ruta_id);
CREATE INDEX IF NOT EXISTS idx_camino_activo ON camino(activo);

-- Comentarios
COMMENT ON TABLE camino IS 'Almacena los caminos físicos específicos de cada ruta';
COMMENT ON COLUMN camino.polyline IS 'Trayecto GPS codificado en formato Google Maps Polyline';
COMMENT ON COLUMN camino.tipo IS 'Tipo de camino: RAPIDO, NORMAL, TURISTICO, ECONOMICO';
