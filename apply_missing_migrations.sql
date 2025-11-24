-- ============================================
-- SCRIPT PARA APLICAR MIGRACIONES MANUALMENTE
-- ============================================
-- Ejecuta este script en tu base de datos das_dev
-- usando pgAdmin, DBeaver, o cualquier cliente PostgreSQL

-- ============================================
-- MIGRACIÓN V21: Agregar foto_url y foto_filename a usuario_cooperativa
-- ============================================

-- Verificar si las columnas ya existen antes de agregarlas
DO $$
BEGIN
    -- Agregar foto_url si no existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'usuario_cooperativa' 
        AND column_name = 'foto_url'
    ) THEN
        ALTER TABLE usuario_cooperativa ADD COLUMN foto_url VARCHAR(500);
        COMMENT ON COLUMN usuario_cooperativa.foto_url IS 'URL pública de la foto de perfil del usuario';
        RAISE NOTICE 'Columna foto_url agregada a usuario_cooperativa';
    ELSE
        RAISE NOTICE 'Columna foto_url ya existe en usuario_cooperativa';
    END IF;

    -- Agregar foto_filename si no existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'usuario_cooperativa' 
        AND column_name = 'foto_filename'
    ) THEN
        ALTER TABLE usuario_cooperativa ADD COLUMN foto_filename VARCHAR(255);
        COMMENT ON COLUMN usuario_cooperativa.foto_filename IS 'Nombre del archivo de la foto almacenado en el servidor';
        RAISE NOTICE 'Columna foto_filename agregada a usuario_cooperativa';
    ELSE
        RAISE NOTICE 'Columna foto_filename ya existe en usuario_cooperativa';
    END IF;
END $$;

-- ============================================
-- MIGRACIÓN V22: Agregar foto_filename a bus
-- ============================================

DO $$
BEGIN
    -- Agregar foto_filename a bus si no existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bus' 
        AND column_name = 'foto_filename'
    ) THEN
        ALTER TABLE bus ADD COLUMN foto_filename VARCHAR(255);
        COMMENT ON COLUMN bus.foto_filename IS 'Nombre del archivo almacenado en el sistema de archivos (ej: bus_123_1234567890.jpg)';
        RAISE NOTICE 'Columna foto_filename agregada a bus';
    ELSE
        RAISE NOTICE 'Columna foto_filename ya existe en bus';
    END IF;
END $$;

-- ============================================
-- REGISTRAR MIGRACIONES EN FLYWAY
-- ============================================

-- Insertar V21 en historial de Flyway si no existe
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT 
    COALESCE((SELECT MAX(installed_rank) FROM flyway_schema_history), 0) + 1,
    '21',
    'add foto to usuario cooperativa',
    'SQL',
    'V21__add_foto_to_usuario_cooperativa.sql',
    NULL,
    CURRENT_USER,
    NOW(),
    0,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '21'
);

-- Insertar V22 en historial de Flyway si no existe
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT 
    COALESCE((SELECT MAX(installed_rank) FROM flyway_schema_history), 0) + 1,
    '22',
    'add foto filename to bus',
    'SQL',
    'V22__add_foto_filename_to_bus.sql',
    NULL,
    CURRENT_USER,
    NOW(),
    0,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '22'
);

-- ============================================
-- VERIFICACIÓN
-- ============================================

-- Mostrar el estado final
SELECT 
    'usuario_cooperativa' as tabla,
    column_name,
    data_type,
    character_maximum_length
FROM information_schema.columns
WHERE table_name = 'usuario_cooperativa' 
AND column_name IN ('foto_url', 'foto_filename')

UNION ALL

SELECT 
    'bus' as tabla,
    column_name,
    data_type,
    character_maximum_length
FROM information_schema.columns
WHERE table_name = 'bus' 
AND column_name = 'foto_filename';

-- Mostrar versiones de Flyway aplicadas
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 5;
