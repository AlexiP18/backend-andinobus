-- Crear tabla de paradas geolocalizadas por camino
CREATE TABLE IF NOT EXISTS parada_camino (
    id BIGSERIAL PRIMARY KEY,
    camino_id BIGINT NOT NULL REFERENCES camino(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255),
    latitud DECIMAL(10,8) NOT NULL CHECK (latitud >= -90 AND latitud <= 90),
    longitud DECIMAL(11,8) NOT NULL CHECK (longitud >= -180 AND longitud <= 180),
    orden INTEGER NOT NULL, -- Secuencia en el camino
    tiempo_estimado_minutos INTEGER, -- Tiempo desde el origen
    permite_abordaje BOOLEAN NOT NULL DEFAULT true,
    permite_descenso BOOLEAN NOT NULL DEFAULT true,
    precio_desde_origen DECIMAL(10,2),
    activa BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (camino_id, orden)
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_parada_camino_camino_orden ON parada_camino(camino_id, orden);
CREATE INDEX IF NOT EXISTS idx_parada_camino_coordenadas ON parada_camino(latitud, longitud);
CREATE INDEX IF NOT EXISTS idx_parada_camino_activa ON parada_camino(activa);

-- Comentarios
COMMENT ON TABLE parada_camino IS 'Almacena las paradas geolocalizadas de cada camino';
COMMENT ON COLUMN parada_camino.latitud IS 'Coordenada GPS de latitud (rango: -90 a +90)';
COMMENT ON COLUMN parada_camino.longitud IS 'Coordenada GPS de longitud (rango: -180 a +180)';
COMMENT ON COLUMN parada_camino.orden IS 'Orden secuencial de la parada en el camino';
COMMENT ON COLUMN parada_camino.permite_abordaje IS 'Indica si se puede abordar el bus en esta parada';
COMMENT ON COLUMN parada_camino.permite_descenso IS 'Indica si se puede descender del bus en esta parada';
