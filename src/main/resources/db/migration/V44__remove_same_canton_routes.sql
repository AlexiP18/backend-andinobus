-- V44: Eliminar rutas entre terminales del mismo cantón
-- Regla de negocio: No se permiten viajes entre terminales del mismo cantón
-- Las rutas intraprovinciales son válidas solo si son entre diferentes cantones

-- Primero, verificar cuántas rutas del mismo cantón existen
DO $$
DECLARE
    rutas_mismo_canton INTEGER;
BEGIN
    SELECT COUNT(*) INTO rutas_mismo_canton
    FROM ruta r
    JOIN terminal t1 ON r.terminal_origen_id = t1.id
    JOIN terminal t2 ON r.terminal_destino_id = t2.id
    WHERE t1.canton = t2.canton;
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'LIMPIEZA DE RUTAS DEL MISMO CANTÓN';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Rutas a eliminar (mismo cantón): %', rutas_mismo_canton;
END $$;

-- Eliminar frecuencias de viaje asociadas a rutas del mismo cantón (si existen)
-- La tabla correcta es frecuencia_viaje, no frecuencia
DELETE FROM frecuencia_viaje
WHERE ruta_id IN (
    SELECT r.id
    FROM ruta r
    JOIN terminal t1 ON r.terminal_origen_id = t1.id
    JOIN terminal t2 ON r.terminal_destino_id = t2.id
    WHERE t1.canton = t2.canton
);

-- Eliminar las rutas donde origen y destino están en el mismo cantón
DELETE FROM ruta
WHERE id IN (
    SELECT r.id
    FROM ruta r
    JOIN terminal t1 ON r.terminal_origen_id = t1.id
    JOIN terminal t2 ON r.terminal_destino_id = t2.id
    WHERE t1.canton = t2.canton
);

-- Agregar constraint CHECK para prevenir futuras rutas del mismo cantón
-- Nota: Este constraint usa una función para validar
CREATE OR REPLACE FUNCTION check_different_canton()
RETURNS TRIGGER AS $$
DECLARE
    canton_origen VARCHAR;
    canton_destino VARCHAR;
BEGIN
    -- Obtener cantones de los terminales
    SELECT canton INTO canton_origen FROM terminal WHERE id = NEW.terminal_origen_id;
    SELECT canton INTO canton_destino FROM terminal WHERE id = NEW.terminal_destino_id;
    
    -- Validar que no sean del mismo cantón
    IF canton_origen IS NOT NULL AND canton_destino IS NOT NULL AND canton_origen = canton_destino THEN
        RAISE EXCEPTION 'No se permiten rutas entre terminales del mismo cantón: %', canton_origen;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear trigger para validar en INSERT y UPDATE
DROP TRIGGER IF EXISTS trg_check_different_canton ON ruta;
CREATE TRIGGER trg_check_different_canton
    BEFORE INSERT OR UPDATE ON ruta
    FOR EACH ROW
    WHEN (NEW.terminal_origen_id IS NOT NULL AND NEW.terminal_destino_id IS NOT NULL)
    EXECUTE FUNCTION check_different_canton();

-- Estadísticas finales
DO $$
DECLARE
    total_rutas INTEGER;
    rutas_inter INTEGER;
    rutas_intra INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_rutas FROM ruta WHERE activo = TRUE;
    SELECT COUNT(*) INTO rutas_inter FROM ruta WHERE tipo_ruta = 'INTERPROVINCIAL' AND activo = TRUE;
    SELECT COUNT(*) INTO rutas_intra FROM ruta WHERE tipo_ruta = 'INTRAPROVINCIAL' AND activo = TRUE;
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'RUTAS DESPUÉS DE LIMPIEZA';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Total de rutas activas: %', total_rutas;
    RAISE NOTICE 'Rutas interprovinciales: %', rutas_inter;
    RAISE NOTICE 'Rutas intraprovinciales (diferentes cantones): %', rutas_intra;
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Trigger de validación creado exitosamente';
    RAISE NOTICE 'Las futuras rutas del mismo cantón serán rechazadas';
    RAISE NOTICE '==========================================';
END $$;
