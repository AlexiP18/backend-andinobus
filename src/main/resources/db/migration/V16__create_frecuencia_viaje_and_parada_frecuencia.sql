-- V16: Crear tablas frecuencia_viaje y parada_frecuencia (idempotente)
-- Contexto: Algunas BD ya tienen un V14 diferente aplicado, por lo que
--            la creación original no se ejecutó. Este script asegura
--            que existan las tablas requeridas por JPA.
-- Notas:
--  - Usa CREATE TABLE IF NOT EXISTS para ser seguro en entornos ya inicializados.
--  - Crea índices también con IF NOT EXISTS.
--  - No inserta datos de ejemplo (no es necesario para validación de esquema).

-- ==============================================
-- TABLA: frecuencia_viaje
-- ==============================================
CREATE TABLE IF NOT EXISTS frecuencia_viaje (
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

-- Índices
CREATE INDEX IF NOT EXISTS idx_frecuencia_viaje_bus ON frecuencia_viaje(bus_id);
CREATE INDEX IF NOT EXISTS idx_frecuencia_viaje_ruta ON frecuencia_viaje(ruta_id);
CREATE INDEX IF NOT EXISTS idx_frecuencia_viaje_activo ON frecuencia_viaje(activo);
CREATE INDEX IF NOT EXISTS idx_frecuencia_viaje_hora_salida ON frecuencia_viaje(hora_salida);

-- ==============================================
-- TABLA: parada_frecuencia
-- ==============================================
CREATE TABLE IF NOT EXISTS parada_frecuencia (
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

-- Índices
CREATE INDEX IF NOT EXISTS idx_parada_frecuencia_viaje ON parada_frecuencia(frecuencia_viaje_id);
CREATE INDEX IF NOT EXISTS idx_parada_orden ON parada_frecuencia(frecuencia_viaje_id, orden);
