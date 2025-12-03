-- V34: Generar todas las rutas posibles entre los terminales
-- Esta migración crea todas las combinaciones de rutas entre los 75 terminales
-- Total aproximado: 75 * 74 / 2 = 2,775 rutas (bidireccionales consideradas como una)

-- Primero, agregar columna para distinguir tipo de ruta (interprovincial/intraprovincial)
ALTER TABLE ruta ADD COLUMN IF NOT EXISTS tipo_ruta VARCHAR(20) DEFAULT 'INTERPROVINCIAL';

-- Agregar columnas para IDs de terminales de origen y destino
ALTER TABLE ruta ADD COLUMN IF NOT EXISTS terminal_origen_id BIGINT REFERENCES terminal(id);
ALTER TABLE ruta ADD COLUMN IF NOT EXISTS terminal_destino_id BIGINT REFERENCES terminal(id);

-- Agregar índices para las nuevas columnas
CREATE INDEX IF NOT EXISTS idx_ruta_tipo_ruta ON ruta(tipo_ruta);
CREATE INDEX IF NOT EXISTS idx_ruta_terminal_origen ON ruta(terminal_origen_id);
CREATE INDEX IF NOT EXISTS idx_ruta_terminal_destino ON ruta(terminal_destino_id);

-- Comentarios
COMMENT ON COLUMN ruta.tipo_ruta IS 'INTERPROVINCIAL = entre diferentes provincias, INTRAPROVINCIAL = dentro de la misma provincia';
COMMENT ON COLUMN ruta.terminal_origen_id IS 'ID del terminal de origen';
COMMENT ON COLUMN ruta.terminal_destino_id IS 'ID del terminal de destino';

-- Eliminar rutas existentes (las de ejemplo) para empezar limpio
DELETE FROM ruta WHERE terminal_origen_id IS NULL;

-- Función para calcular distancia aproximada usando fórmula de Haversine
CREATE OR REPLACE FUNCTION haversine_distance(lat1 DOUBLE PRECISION, lon1 DOUBLE PRECISION, lat2 DOUBLE PRECISION, lon2 DOUBLE PRECISION)
RETURNS DOUBLE PRECISION AS $$
DECLARE
    R DOUBLE PRECISION := 6371; -- Radio de la Tierra en km
    dlat DOUBLE PRECISION;
    dlon DOUBLE PRECISION;
    a DOUBLE PRECISION;
    c DOUBLE PRECISION;
BEGIN
    dlat := radians(lat2 - lat1);
    dlon := radians(lon2 - lon1);
    a := sin(dlat/2) * sin(dlat/2) + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon/2) * sin(dlon/2);
    c := 2 * atan2(sqrt(a), sqrt(1-a));
    RETURN R * c;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Asegurar que no exista la restricción de unicidad por nombre antes de insertar rutas masivas
ALTER TABLE ruta DROP CONSTRAINT IF EXISTS uq_ruta_nombre;

-- Generar todas las rutas posibles entre terminales
-- Solo genera rutas donde origen.id < destino.id para evitar duplicados (A-B y B-A se consideran la misma ruta)
INSERT INTO ruta (
    nombre,
    origen,
    destino,
    distancia_km,
    duracion_estimada_minutos,
    descripcion,
    aprobada_ant,
    activo,
    tipo_ruta,
    terminal_origen_id,
    terminal_destino_id,
    created_at
)
SELECT 
    -- Nombre de la ruta: Siempre incluye nombre del terminal para mayor claridad
    t1.canton || ' (' || t1.nombre || ') - ' || t2.canton || ' (' || t2.nombre || ')' AS nombre,
    -- Origen en formato "provincia|canton|terminal_id"
    t1.provincia || '|' || t1.canton || '|' || t1.id AS origen,
    -- Destino en formato "provincia|canton|terminal_id"
    t2.provincia || '|' || t2.canton || '|' || t2.id AS destino,
    -- Distancia en km (Haversine * 1.3 para aproximar distancia por carretera)
    ROUND((haversine_distance(t1.latitud, t1.longitud, t2.latitud, t2.longitud) * 1.3)::NUMERIC, 1) AS distancia_km,
    -- Duración estimada: distancia / 60 km/h promedio * 60 minutos
    ROUND((haversine_distance(t1.latitud, t1.longitud, t2.latitud, t2.longitud) * 1.3 / 60 * 60)::NUMERIC) AS duracion_estimada_minutos,
    -- Descripción
    CASE 
        WHEN t1.provincia = t2.provincia THEN 
            'Ruta intraprovincial en ' || t1.provincia || ': ' || t1.nombre || ' ↔ ' || t2.nombre
        ELSE 
            'Ruta interprovincial: ' || t1.nombre || ' (' || t1.provincia || ') ↔ ' || t2.nombre || ' (' || t2.provincia || ')'
    END AS descripcion,
    -- No aprobada por ANT (pendiente aprobación manual)
    FALSE AS aprobada_ant,
    -- Activa por defecto
    TRUE AS activo,
    -- Tipo de ruta
    CASE 
        WHEN t1.provincia = t2.provincia THEN 'INTRAPROVINCIAL'
        ELSE 'INTERPROVINCIAL'
    END AS tipo_ruta,
    -- IDs de terminales
    t1.id AS terminal_origen_id,
    t2.id AS terminal_destino_id,
    -- Timestamp
    CURRENT_TIMESTAMP AS created_at
FROM 
    terminal t1
    CROSS JOIN terminal t2
WHERE 
    t1.id < t2.id  -- Evitar duplicados y rutas a sí mismo
    AND t1.activo = TRUE
    AND t2.activo = TRUE
    AND t1.latitud IS NOT NULL 
    AND t1.longitud IS NOT NULL
    AND t2.latitud IS NOT NULL 
    AND t2.longitud IS NOT NULL
ORDER BY 
    t1.provincia, t1.canton, t2.provincia, t2.canton;

-- Actualizar el constraint unique para permitir nombres duplicados (diferentes terminales pueden tener mismo cantón)
-- Primero eliminar el constraint existente si existe
ALTER TABLE ruta DROP CONSTRAINT IF EXISTS uq_ruta_nombre;

-- Crear nuevo constraint único basado en terminales
ALTER TABLE ruta ADD CONSTRAINT uq_ruta_terminales UNIQUE (terminal_origen_id, terminal_destino_id);

-- Estadísticas de las rutas generadas
DO $$
DECLARE
    total_rutas INTEGER;
    rutas_inter INTEGER;
    rutas_intra INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_rutas FROM ruta;
    SELECT COUNT(*) INTO rutas_inter FROM ruta WHERE tipo_ruta = 'INTERPROVINCIAL';
    SELECT COUNT(*) INTO rutas_intra FROM ruta WHERE tipo_ruta = 'INTRAPROVINCIAL';
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'RUTAS GENERADAS EXITOSAMENTE';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Total de rutas: %', total_rutas;
    RAISE NOTICE 'Rutas interprovinciales: %', rutas_inter;
    RAISE NOTICE 'Rutas intraprovinciales: %', rutas_intra;
    RAISE NOTICE '==========================================';
END $$;

-- Mostrar resumen por provincia
SELECT 
    'Rutas por provincia de origen' AS resumen,
    SPLIT_PART(origen, '|', 1) AS provincia,
    COUNT(*) AS total_rutas,
    COUNT(*) FILTER (WHERE tipo_ruta = 'INTERPROVINCIAL') AS interprovinciales,
    COUNT(*) FILTER (WHERE tipo_ruta = 'INTRAPROVINCIAL') AS intraprovinciales
FROM ruta
GROUP BY SPLIT_PART(origen, '|', 1)
ORDER BY provincia;
