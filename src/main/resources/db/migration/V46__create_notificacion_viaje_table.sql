-- =====================================================
-- V46: Crear tabla notificacion_viaje
-- =====================================================
-- Esta tabla almacena las notificaciones que se envían
-- a las cooperativas cuando los choferes inician o 
-- finalizan viajes.
-- =====================================================

CREATE TABLE IF NOT EXISTS notificacion_viaje (
    id BIGSERIAL PRIMARY KEY,
    viaje_id BIGINT NOT NULL,
    cooperativa_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT,
    detalle_viaje TEXT,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_notificacion_viaje FOREIGN KEY (viaje_id) REFERENCES viaje(id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_cooperativa FOREIGN KEY (cooperativa_id) REFERENCES cooperativa(id) ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento de consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_notificacion_cooperativa_id ON notificacion_viaje(cooperativa_id);
CREATE INDEX IF NOT EXISTS idx_notificacion_viaje_id ON notificacion_viaje(viaje_id);
CREATE INDEX IF NOT EXISTS idx_notificacion_leida ON notificacion_viaje(leida);
CREATE INDEX IF NOT EXISTS idx_notificacion_fecha ON notificacion_viaje(fecha_creacion);

-- Comentarios de la tabla
COMMENT ON TABLE notificacion_viaje IS 'Notificaciones de eventos de viajes para cooperativas';
COMMENT ON COLUMN notificacion_viaje.tipo IS 'Tipo: VIAJE_INICIADO, VIAJE_FINALIZADO, VIAJE_CANCELADO, ALERTA_RETRASO';
COMMENT ON COLUMN notificacion_viaje.leida IS 'Indica si la notificación ha sido leída por la cooperativa';
