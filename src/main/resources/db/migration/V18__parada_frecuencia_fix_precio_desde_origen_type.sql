-- V18: Ajustar tipo de columna precio_desde_origen en parada_frecuencia a DOUBLE PRECISION
-- Contexto: La entidad JPA usa Double, que Hibernate valida como DOUBLE PRECISION (float(53)).
-- En algunas BD la columna fue creada como NUMERIC(10,2), causando error de validación.
-- Este script es idempotente y solo cambia el tipo si actualmente no es DOUBLE PRECISION.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns c
        WHERE c.table_schema = 'public'
          AND c.table_name = 'parada_frecuencia'
          AND c.column_name = 'precio_desde_origen'
          AND c.data_type <> 'double precision'
    ) THEN
        ALTER TABLE public.parada_frecuencia
            ALTER COLUMN precio_desde_origen TYPE DOUBLE PRECISION
            USING precio_desde_origen::double precision;
    END IF;
END $$;