-- V4: Configuración de asientos por bus y disponibilidad

-- Configuración de asientos por tipo de bus
CREATE TABLE IF NOT EXISTS bus_asiento_config (
    id              BIGSERIAL PRIMARY KEY,
    bus_id          BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE,
    tipo_asiento    VARCHAR(32) NOT NULL, -- Normal, VIP, Semi-cama, Cama
    cantidad        INTEGER NOT NULL CHECK (cantidad >= 0),
    precio_base     NUMERIC(10,2),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_bus_asiento_bus ON bus_asiento_config (bus_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_bus_asiento_tipo ON bus_asiento_config (bus_id, tipo_asiento);

-- Asientos ocupados por viaje (para rastrear disponibilidad)
CREATE TABLE IF NOT EXISTS viaje_asiento (
    id              BIGSERIAL PRIMARY KEY,
    viaje_id        BIGINT NOT NULL REFERENCES viaje(id) ON DELETE CASCADE,
    numero_asiento  INTEGER NOT NULL,
    tipo_asiento    VARCHAR(32) NOT NULL,
    estado          VARCHAR(32) NOT NULL DEFAULT 'DISPONIBLE', -- DISPONIBLE | RESERVADO | VENDIDO | BLOQUEADO
    reserva_id      BIGINT REFERENCES reserva(id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_viaje_asiento_viaje ON viaje_asiento (viaje_id);
CREATE INDEX IF NOT EXISTS ix_viaje_asiento_estado ON viaje_asiento (estado);
CREATE UNIQUE INDEX IF NOT EXISTS uq_viaje_asiento_numero ON viaje_asiento (viaje_id, numero_asiento);

-- Datos de ejemplo para cooperativa y buses
INSERT INTO cooperativa (nombre, ruc, activo) VALUES 
('Cooperativa Loja', '1790123456001', true),
('Cooperativa Panamericana', '1790234567001', true),
('Cooperativa Viajeros', '1790345678001', true)
ON CONFLICT DO NOTHING;

-- Buses de ejemplo
INSERT INTO bus (cooperativa_id, numero_interno, placa, chasis_marca, carroceria_marca, activo) 
SELECT c.id, '101', 'ABC-1234', 'Volvo', 'Marcopolo', true 
FROM cooperativa c WHERE c.nombre = 'Cooperativa Loja'
ON CONFLICT DO NOTHING;

INSERT INTO bus (cooperativa_id, numero_interno, placa, chasis_marca, carroceria_marca, activo) 
SELECT c.id, '102', 'DEF-5678', 'Mercedes-Benz', 'Irizar', true 
FROM cooperativa c WHERE c.nombre = 'Cooperativa Panamericana'
ON CONFLICT DO NOTHING;

-- Configuración de asientos para los buses
INSERT INTO bus_asiento_config (bus_id, tipo_asiento, cantidad, precio_base)
SELECT b.id, 'Normal', 32, 15.00
FROM bus b WHERE b.placa = 'ABC-1234'
ON CONFLICT DO NOTHING;

INSERT INTO bus_asiento_config (bus_id, tipo_asiento, cantidad, precio_base)
SELECT b.id, 'VIP', 8, 25.00
FROM bus b WHERE b.placa = 'ABC-1234'
ON CONFLICT DO NOTHING;

INSERT INTO bus_asiento_config (bus_id, tipo_asiento, cantidad, precio_base)
SELECT b.id, 'Semi-cama', 28, 22.00
FROM bus b WHERE b.placa = 'DEF-5678'
ON CONFLICT DO NOTHING;

INSERT INTO bus_asiento_config (bus_id, tipo_asiento, cantidad, precio_base)
SELECT b.id, 'Cama', 12, 35.00
FROM bus b WHERE b.placa = 'DEF-5678'
ON CONFLICT DO NOTHING;

-- Frecuencias de ejemplo
INSERT INTO frecuencia (cooperativa_id, origen, destino, hora_salida, duracion_estimada_min, dias_operacion, activa)
SELECT c.id, 'Quito', 'Guayaquil', '08:00:00', 480, 'LUN,MAR,MIE,JUE,VIE,SAB,DOM', true
FROM cooperativa c WHERE c.nombre = 'Cooperativa Loja'
ON CONFLICT DO NOTHING;

INSERT INTO frecuencia (cooperativa_id, origen, destino, hora_salida, duracion_estimada_min, dias_operacion, activa)
SELECT c.id, 'Quito', 'Cuenca', '14:00:00', 540, 'LUN,MAR,MIE,JUE,VIE,SAB,DOM', true
FROM cooperativa c WHERE c.nombre = 'Cooperativa Panamericana'
ON CONFLICT DO NOTHING;

INSERT INTO frecuencia (cooperativa_id, origen, destino, hora_salida, duracion_estimada_min, dias_operacion, activa)
SELECT c.id, 'Guayaquil', 'Loja', '18:00:00', 420, 'LUN,MAR,MIE,JUE,VIE,SAB,DOM', true
FROM cooperativa c WHERE c.nombre = 'Cooperativa Loja'
ON CONFLICT DO NOTHING;

INSERT INTO frecuencia (cooperativa_id, origen, destino, hora_salida, duracion_estimada_min, dias_operacion, activa)
SELECT c.id, 'Cuenca', 'Quito', '06:30:00', 540, 'LUN,MAR,MIE,JUE,VIE,SAB,DOM', true
FROM cooperativa c WHERE c.nombre = 'Cooperativa Viajeros'
ON CONFLICT DO NOTHING;
