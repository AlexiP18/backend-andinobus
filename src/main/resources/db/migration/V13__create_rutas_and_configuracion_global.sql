-- Crear tabla de rutas
CREATE TABLE IF NOT EXISTS ruta (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    origen VARCHAR(100) NOT NULL,
    destino VARCHAR(100) NOT NULL,
    distancia_km DECIMAL(10, 2),
    duracion_estimada_minutos INTEGER,
    descripcion TEXT,
    aprobada_ant BOOLEAN NOT NULL DEFAULT FALSE,
    numero_resolucion_ant VARCHAR(100),
    fecha_aprobacion_ant DATE,
    vigencia_hasta DATE,
    observaciones_ant TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uq_ruta_nombre UNIQUE (nombre)
);

-- Crear índices para optimizar búsquedas
CREATE INDEX idx_ruta_activo ON ruta(activo);
CREATE INDEX idx_ruta_aprobada_ant ON ruta(aprobada_ant);
CREATE INDEX idx_ruta_origen_destino ON ruta(origen, destino);

-- Crear tabla de configuración global (singleton)
CREATE TABLE IF NOT EXISTS configuracion_global (
    id BIGSERIAL PRIMARY KEY,
    nombre_aplicacion VARCHAR(100) DEFAULT 'AndinoBus',
    logo_url VARCHAR(500),
    logo_small_url VARCHAR(500),
    favicon_url VARCHAR(500),
    color_primario VARCHAR(7) DEFAULT '#1E40AF',
    color_secundario VARCHAR(7) DEFAULT '#3B82F6',
    color_acento VARCHAR(7) DEFAULT '#10B981',
    facebook_url VARCHAR(200),
    twitter_url VARCHAR(200),
    instagram_url VARCHAR(200),
    youtube_url VARCHAR(200),
    linkedin_url VARCHAR(200),
    email_soporte VARCHAR(100),
    telefono_soporte VARCHAR(20),
    whatsapp_soporte VARCHAR(20),
    direccion_fisica VARCHAR(300),
    horario_atencion VARCHAR(200),
    sitio_web VARCHAR(200),
    terminos_condiciones_url VARCHAR(500),
    politica_privacidad_url VARCHAR(500),
    descripcion TEXT,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Insertar configuración por defecto
INSERT INTO configuracion_global (
    nombre_aplicacion,
    color_primario,
    color_secundario,
    color_acento,
    descripcion
) VALUES (
    'AndinoBus',
    '#1E40AF',
    '#3B82F6',
    '#10B981',
    'Sistema de gestión y venta de boletos para cooperativas de transporte interprovincial'
) ON CONFLICT DO NOTHING;

-- Insertar algunas rutas de ejemplo aprobadas por la ANT
INSERT INTO ruta (
    nombre,
    origen,
    destino,
    distancia_km,
    duracion_estimada_minutos,
    descripcion,
    aprobada_ant,
    numero_resolucion_ant,
    fecha_aprobacion_ant,
    vigencia_hasta,
    observaciones_ant
) VALUES
(
    'Quito - Guayaquil Vía Alóag',
    'Quito',
    'Guayaquil',
    420.5,
    480,
    'Ruta interprovincial principal que conecta Quito con Guayaquil a través de la vía Alóag',
    TRUE,
    'ANT-2023-001-RES',
    '2023-01-15',
    '2025-01-15',
    'Ruta aprobada con paradas autorizadas en: Machachi, Latacunga, Ambato, Riobamba, Guaranda, Babahoyo'
),
(
    'Quito - Cuenca Vía Panamericana',
    'Quito',
    'Cuenca',
    440.8,
    540,
    'Ruta interprovincial que une la capital con Cuenca por la Panamericana Sur',
    TRUE,
    'ANT-2023-002-RES',
    '2023-02-10',
    '2025-02-10',
    'Ruta aprobada con paradas autorizadas en: Machachi, Latacunga, Ambato, Riobamba, Alausí, Cañar, Azogues'
),
(
    'Guayaquil - Machala',
    'Guayaquil',
    'Machala',
    195.3,
    240,
    'Ruta costera que conecta las principales ciudades del sur del país',
    TRUE,
    'ANT-2023-003-RES',
    '2023-03-05',
    '2025-03-05',
    'Ruta aprobada con paradas autorizadas en: Naranjal, La Troncal, El Guabo'
),
(
    'Quito - Esmeraldas',
    'Quito',
    'Esmeraldas',
    318.7,
    360,
    'Ruta que conecta la capital con la provincia de Esmeraldas',
    TRUE,
    'ANT-2023-004-RES',
    '2023-04-12',
    '2025-04-12',
    'Ruta aprobada con paradas autorizadas en: Calacalí, Nanegalito, San Miguel de los Bancos, Pedro Vicente Maldonado, La Independencia'
);

-- Comentario sobre las rutas
COMMENT ON TABLE ruta IS 'Rutas de transporte interprovincial aprobadas y reguladas por la ANT (Agencia Nacional de Tránsito)';
COMMENT ON COLUMN ruta.aprobada_ant IS 'Indica si la ruta cuenta con aprobación vigente de la ANT';
COMMENT ON COLUMN ruta.numero_resolucion_ant IS 'Número de resolución oficial emitido por la ANT';
COMMENT ON COLUMN ruta.fecha_aprobacion_ant IS 'Fecha en que la ANT aprobó esta ruta';
COMMENT ON COLUMN ruta.vigencia_hasta IS 'Fecha hasta la cual es válida la aprobación de la ruta';
COMMENT ON COLUMN ruta.observaciones_ant IS 'Observaciones adicionales sobre la aprobación, paradas autorizadas, restricciones, etc.';
