-- V17: Alinear tipo de columna frecuencia_viaje.precio_base con la entidad (Double => DOUBLE PRECISION)
-- Objetivo: Evitar error de Hibernate "wrong column type ... found numeric, but expecting float(53)"
-- Estrategia: Cambiar el tipo de NUMERIC a DOUBLE PRECISION solo si actualmente no es double precision.
-- Idempotente para múltiples ejecuciones.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns c
        WHERE c.table_schema = 'public'
          AND c.table_name = 'frecuencia_viaje'
          AND c.column_name = 'precio_base'
          AND c.data_type <> 'double precision'
    ) THEN
        ALTER TABLE public.frecuencia_viaje
            ALTER COLUMN precio_base TYPE DOUBLE PRECISION
            USING precio_base::double precision;
    END IF;
END
$$;