-- Crear tabla posicion_viaje para tracking GPS en tiempo real
CREATE TABLE IF NOT EXISTS posicion_viaje (
    id BIGSERIAL PRIMARY KEY,
    viaje_id BIGINT NOT NULL REFERENCES viaje(id) ON DELETE CASCADE,
    latitud DECIMAL(10,8) NOT NULL CHECK (latitud >= -90 AND latitud <= 90),
    longitud DECIMAL(11,8) NOT NULL CHECK (longitud >= -180 AND longitud <= 180),
    velocidad_kmh DECIMAL(6,2),
    precision DECIMAL(6,2), -- Precisión GPS en metros
    timestamp TIMESTAMP NOT NULL, -- Timestamp del dispositivo GPS
    provider VARCHAR(20), -- GPS, NETWORK, FUSED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento de consultas de tracking
CREATE INDEX IF NOT EXISTS idx_posicion_viaje_timestamp ON posicion_viaje(viaje_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_posicion_viaje_created ON posicion_viaje(created_at DESC);

-- Comentarios
COMMENT ON TABLE posicion_viaje IS 'Almacena el historial de posiciones GPS de cada viaje para tracking en tiempo real';
COMMENT ON COLUMN posicion_viaje.timestamp IS 'Timestamp del dispositivo GPS cuando se capturó la posición';
COMMENT ON COLUMN posicion_viaje.provider IS 'Proveedor de geolocalización: GPS, NETWORK, FUSED';
COMMENT ON COLUMN posicion_viaje.precision IS 'Precisión de la señal GPS en metros';
