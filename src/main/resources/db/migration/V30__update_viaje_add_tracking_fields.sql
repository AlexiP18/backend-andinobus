-- Agregar campos de tracking y estado a la tabla viaje
ALTER TABLE viaje 
ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'PROGRAMADO',
ADD COLUMN IF NOT EXISTS hora_inicio_real TIMESTAMP,
ADD COLUMN IF NOT EXISTS hora_fin_real TIMESTAMP,
ADD COLUMN IF NOT EXISTS latitud_actual DECIMAL(10,8),
ADD COLUMN IF NOT EXISTS longitud_actual DECIMAL(11,8),
ADD COLUMN IF NOT EXISTS ultima_actualizacion TIMESTAMP;

-- Constraint para validar estados válidos (alineado con la entidad Viaje)
ALTER TABLE viaje 
ADD CONSTRAINT chk_viaje_estado CHECK (estado IN ('PROGRAMADO','EN_TERMINAL','EN_RUTA','EN_CURSO','FINALIZADO','CANCELADO','COMPLETADO'));

-- Constraint para validar coordenadas GPS válidas
ALTER TABLE viaje 
ADD CONSTRAINT chk_viaje_latitud_valida CHECK (latitud_actual IS NULL OR (latitud_actual >= -90 AND latitud_actual <= 90));

ALTER TABLE viaje 
ADD CONSTRAINT chk_viaje_longitud_valida CHECK (longitud_actual IS NULL OR (longitud_actual >= -180 AND longitud_actual <= 180));

-- Índices para mejorar consultas de viajes activos
CREATE INDEX IF NOT EXISTS idx_viaje_estado ON viaje(estado);
CREATE INDEX IF NOT EXISTS idx_viaje_tracking ON viaje(estado, ultima_actualizacion DESC) WHERE estado = 'EN_CURSO';

-- Comentarios
COMMENT ON COLUMN viaje.estado IS 'Estado del viaje: PROGRAMADO, EN_TERMINAL, EN_RUTA, EN_CURSO, FINALIZADO, CANCELADO, COMPLETADO';
COMMENT ON COLUMN viaje.hora_inicio_real IS 'Hora real de inicio del viaje (puede diferir de la programada)';
COMMENT ON COLUMN viaje.hora_fin_real IS 'Hora real de finalización del viaje';
COMMENT ON COLUMN viaje.latitud_actual IS 'Última latitud GPS conocida del bus';
COMMENT ON COLUMN viaje.longitud_actual IS 'Última longitud GPS conocida del bus';
COMMENT ON COLUMN viaje.ultima_actualizacion IS 'Timestamp de la última actualización de posición GPS';
