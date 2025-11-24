-- V19__create_calificaciones_viaje.sql

-- Tabla de calificaciones de viaje
CREATE TABLE IF NOT EXISTS calificaciones_viaje (
    id BIGSERIAL PRIMARY KEY,
    viaje_id BIGINT NOT NULL,
    cliente_email VARCHAR(100) NOT NULL,
    puntuacion INTEGER NOT NULL CHECK (puntuacion >= 1 AND puntuacion <= 5),
    comentario TEXT,
    fecha_calificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_calificacion_viaje
        FOREIGN KEY (viaje_id)
        REFERENCES viaje(id)
        ON DELETE CASCADE,
    
    CONSTRAINT unique_calificacion_por_cliente
        UNIQUE (viaje_id, cliente_email)
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_calificaciones_viaje_id ON calificaciones_viaje(viaje_id);
CREATE INDEX idx_calificaciones_cliente_email ON calificaciones_viaje(cliente_email);
CREATE INDEX idx_calificaciones_fecha ON calificaciones_viaje(fecha_calificacion DESC);
CREATE INDEX idx_calificaciones_activa ON calificaciones_viaje(activa);

-- Comentarios de documentación
COMMENT ON TABLE calificaciones_viaje IS 'Almacena las calificaciones y comentarios que los clientes dejan sobre los viajes';
COMMENT ON COLUMN calificaciones_viaje.puntuacion IS 'Calificación de 1 a 5 estrellas';
COMMENT ON COLUMN calificaciones_viaje.comentario IS 'Comentario opcional del cliente sobre el viaje';
COMMENT ON COLUMN calificaciones_viaje.activa IS 'Indica si la calificación está activa (no eliminada)';
