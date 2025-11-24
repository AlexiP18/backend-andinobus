-- Migración V14: Frecuencias de Viaje y Paradas

-- Tabla para frecuencias de viaje
CREATE TABLE frecuencia_viaje (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL REFERENCES bus(id),
    ruta_id BIGINT NOT NULL REFERENCES ruta(id),
    hora_salida TIME NOT NULL,
    hora_llegada_estimada TIME,
    dias_operacion VARCHAR(100) NOT NULL DEFAULT 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO',
    precio_base NUMERIC(10, 2),
    asientos_disponibles INTEGER,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_frecuencia_bus_ruta_hora UNIQUE (bus_id, ruta_id, hora_salida)
);

-- Tabla para paradas de frecuencias
CREATE TABLE parada_frecuencia (
    id BIGSERIAL PRIMARY KEY,
    frecuencia_viaje_id BIGINT NOT NULL REFERENCES frecuencia_viaje(id) ON DELETE CASCADE,
    orden INTEGER NOT NULL,
    nombre_parada VARCHAR(200) NOT NULL,
    direccion VARCHAR(500),
    tiempo_llegada TIME,
    tiempo_espera_minutos INTEGER DEFAULT 5,
    precio_desde_origen NUMERIC(10, 2),
    observaciones TEXT,
    permite_abordaje BOOLEAN NOT NULL DEFAULT TRUE,
    permite_descenso BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_parada_frecuencia_orden UNIQUE (frecuencia_viaje_id, orden)
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_frecuencia_viaje_bus ON frecuencia_viaje(bus_id);
CREATE INDEX idx_frecuencia_viaje_ruta ON frecuencia_viaje(ruta_id);
CREATE INDEX idx_frecuencia_viaje_activo ON frecuencia_viaje(activo);
CREATE INDEX idx_frecuencia_viaje_hora_salida ON frecuencia_viaje(hora_salida);
CREATE INDEX idx_parada_frecuencia_viaje ON parada_frecuencia(frecuencia_viaje_id);
CREATE INDEX idx_parada_orden ON parada_frecuencia(frecuencia_viaje_id, orden);

-- Datos de ejemplo: Frecuencias para las rutas existentes
-- Asumiendo que existen buses con IDs 1, 2, 3, 4

-- Frecuencia 1: Quito - Guayaquil, Bus 1, Salida 06:00
INSERT INTO frecuencia_viaje (bus_id, ruta_id, hora_salida, hora_llegada_estimada, dias_operacion, precio_base, asientos_disponibles, observaciones)
VALUES (1, 1, '06:00:00', '14:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', 15.00, 40, 'Servicio diario con aire acondicionado');

-- Paradas para Frecuencia 1
INSERT INTO parada_frecuencia (frecuencia_viaje_id, orden, nombre_parada, direccion, tiempo_llegada, tiempo_espera_minutos, precio_desde_origen, permite_abordaje, permite_descenso)
VALUES 
(1, 1, 'Terminal Quitumbe', 'Av. Quitumbe Ñan, Quito', '06:00:00', 10, 0.00, TRUE, FALSE),
(1, 2, 'Machachi', 'Centro de Machachi', '07:00:00', 5, 3.00, TRUE, TRUE),
(1, 3, 'Latacunga', 'Terminal Terrestre Latacunga', '08:00:00', 10, 5.50, TRUE, TRUE),
(1, 4, 'Ambato', 'Terminal Terrestre Ambato', '09:15:00', 10, 7.50, TRUE, TRUE),
(1, 5, 'Riobamba', 'Terminal Terrestre Riobamba', '10:30:00', 10, 9.00, TRUE, TRUE),
(1, 6, 'Guaranda', 'Centro de Guaranda', '11:45:00', 5, 11.00, TRUE, TRUE),
(1, 7, 'Babahoyo', 'Terminal Babahoyo', '13:00:00', 5, 13.00, TRUE, TRUE),
(1, 8, 'Terminal Guayaquil', 'Terminal Terrestre Guayaquil', '14:00:00', 0, 15.00, TRUE, TRUE);

-- Frecuencia 2: Quito - Guayaquil, Bus 1, Salida 14:00
INSERT INTO frecuencia_viaje (bus_id, ruta_id, hora_salida, hora_llegada_estimada, dias_operacion, precio_base, asientos_disponibles, observaciones)
VALUES (1, 1, '14:00:00', '22:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', 15.00, 40, 'Servicio vespertino');

-- Paradas para Frecuencia 2 (mismas paradas pero horarios diferentes)
INSERT INTO parada_frecuencia (frecuencia_viaje_id, orden, nombre_parada, direccion, tiempo_llegada, tiempo_espera_minutos, precio_desde_origen, permite_abordaje, permite_descenso)
VALUES 
(2, 1, 'Terminal Quitumbe', 'Av. Quitumbe Ñan, Quito', '14:00:00', 10, 0.00, TRUE, FALSE),
(2, 2, 'Machachi', 'Centro de Machachi', '15:00:00', 5, 3.00, TRUE, TRUE),
(2, 3, 'Latacunga', 'Terminal Terrestre Latacunga', '16:00:00', 10, 5.50, TRUE, TRUE),
(2, 4, 'Ambato', 'Terminal Terrestre Ambato', '17:15:00', 10, 7.50, TRUE, TRUE),
(2, 5, 'Riobamba', 'Terminal Terrestre Riobamba', '18:30:00', 10, 9.00, TRUE, TRUE),
(2, 6, 'Guaranda', 'Centro de Guaranda', '19:45:00', 5, 11.00, TRUE, TRUE),
(2, 7, 'Babahoyo', 'Terminal Babahoyo', '21:00:00', 5, 13.00, TRUE, TRUE),
(2, 8, 'Terminal Guayaquil', 'Terminal Terrestre Guayaquil', '22:00:00', 0, 15.00, TRUE, TRUE);

-- Frecuencia 3: Quito - Cuenca, Bus 2, Salida 07:30
INSERT INTO frecuencia_viaje (bus_id, ruta_id, hora_salida, hora_llegada_estimada, dias_operacion, precio_base, asientos_disponibles, observaciones)
VALUES (2, 2, '07:30:00', '16:30:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', 18.00, 40, 'Ruta directa por Panamericana');

-- Paradas para Frecuencia 3
INSERT INTO parada_frecuencia (frecuencia_viaje_id, orden, nombre_parada, direccion, tiempo_llegada, tiempo_espera_minutos, precio_desde_origen, permite_abordaje, permite_descenso)
VALUES 
(3, 1, 'Terminal Quitumbe', 'Av. Quitumbe Ñan, Quito', '07:30:00', 10, 0.00, TRUE, FALSE),
(3, 2, 'Machachi', 'Centro de Machachi', '08:30:00', 5, 3.00, TRUE, TRUE),
(3, 3, 'Latacunga', 'Terminal Terrestre Latacunga', '09:30:00', 10, 5.50, TRUE, TRUE),
(3, 4, 'Ambato', 'Terminal Terrestre Ambato', '10:45:00', 10, 7.50, TRUE, TRUE),
(3, 5, 'Riobamba', 'Terminal Terrestre Riobamba', '12:00:00', 15, 9.00, TRUE, TRUE),
(3, 6, 'Alausí', 'Centro de Alausí', '13:15:00', 5, 11.50, TRUE, TRUE),
(3, 7, 'Cañar', 'Centro de Cañar', '14:30:00', 5, 14.00, TRUE, TRUE),
(3, 8, 'Azogues', 'Terminal Azogues', '15:30:00', 5, 16.00, TRUE, TRUE),
(3, 9, 'Terminal Cuenca', 'Terminal Terrestre Cuenca', '16:30:00', 0, 18.00, TRUE, TRUE);

-- Frecuencia 4: Guayaquil - Machala, Bus 3, Salida 08:00
INSERT INTO frecuencia_viaje (bus_id, ruta_id, hora_salida, hora_llegada_estimada, dias_operacion, precio_base, asientos_disponibles, observaciones)
VALUES (3, 3, '08:00:00', '12:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', 8.00, 40, 'Servicio directo costa');

-- Paradas para Frecuencia 4
INSERT INTO parada_frecuencia (frecuencia_viaje_id, orden, nombre_parada, direccion, tiempo_llegada, tiempo_espera_minutos, precio_desde_origen, permite_abordaje, permite_descenso)
VALUES 
(4, 1, 'Terminal Guayaquil', 'Terminal Terrestre Guayaquil', '08:00:00', 10, 0.00, TRUE, FALSE),
(4, 2, 'Naranjal', 'Centro de Naranjal', '09:15:00', 5, 3.00, TRUE, TRUE),
(4, 3, 'La Troncal', 'Centro de La Troncal', '10:30:00', 5, 5.00, TRUE, TRUE),
(4, 4, 'El Guabo', 'Centro de El Guabo', '11:30:00', 5, 7.00, TRUE, TRUE),
(4, 5, 'Terminal Machala', 'Terminal Terrestre Machala', '12:00:00', 0, 8.00, TRUE, TRUE);

-- Frecuencia 5: Quito - Esmeraldas, Bus 4, Salida 06:30
INSERT INTO frecuencia_viaje (bus_id, ruta_id, hora_salida, hora_llegada_estimada, dias_operacion, precio_base, asientos_disponibles, observaciones)
VALUES (4, 4, '06:30:00', '12:30:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', 12.00, 40, 'Ruta hacia la costa norte');

-- Paradas para Frecuencia 5
INSERT INTO parada_frecuencia (frecuencia_viaje_id, orden, nombre_parada, direccion, tiempo_llegada, tiempo_espera_minutos, precio_desde_origen, permite_abordaje, permite_descenso)
VALUES 
(5, 1, 'Terminal Carcelén', 'Av. Diego de Vásquez, Quito', '06:30:00', 10, 0.00, TRUE, FALSE),
(5, 2, 'Calacalí', 'Centro de Calacalí', '07:30:00', 5, 2.50, TRUE, TRUE),
(5, 3, 'Nanegalito', 'Centro de Nanegalito', '08:15:00', 5, 4.00, TRUE, TRUE),
(5, 4, 'San Miguel de los Bancos', 'Centro de San Miguel', '09:00:00', 5, 6.00, TRUE, TRUE),
(5, 5, 'Pedro Vicente Maldonado', 'Centro de Pedro Vicente', '09:45:00', 5, 8.00, TRUE, TRUE),
(5, 6, 'Terminal Esmeraldas', 'Terminal Terrestre Esmeraldas', '12:30:00', 0, 12.00, TRUE, TRUE);
