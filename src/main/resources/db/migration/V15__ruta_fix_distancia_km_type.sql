-- V10: Ajustar tipo de columna distancia_km en tabla ruta a DOUBLE PRECISION
-- Fecha: 2025-11-17
-- Motivo: La entidad Ruta usa Double (Hibernate espera float(53)/double precision),
--         pero la base tenía distancia_km como NUMERIC, causando error de validación.
-- Idempotente: solo altera si actualmente es NUMERIC.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns c
        WHERE c.table_schema = 'public'
          AND c.table_name = 'ruta'
          AND c.column_name = 'distancia_km'
          AND c.data_type = 'numeric'
    ) THEN
        EXECUTE 'ALTER TABLE public.ruta ALTER COLUMN distancia_km TYPE DOUBLE PRECISION USING distancia_km::double precision';
    END IF;
END $$;