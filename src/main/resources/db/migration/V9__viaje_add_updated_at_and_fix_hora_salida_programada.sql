-- V9: Ajustes en viaje: agregar updated_at y normalizar hora_salida_programada
-- Fecha: 2025-11-16
-- Autor: Sistema AndinoBus
-- Objetivo:
--  - Agregar la columna updated_at requerida por la entidad Viaje (@Column(nullable=false)).
--  - Asegurar que hora_salida_programada sea NOT NULL, rellenando con hora_salida cuando esté ausente.
--  - Mantener idempotencia para múltiples ejecuciones.

-- 1) Agregar columna updated_at si no existe
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- 1.1) Rellenar valores nulos con NOW() para cumplir NOT NULL
UPDATE viaje SET updated_at = NOW() WHERE updated_at IS NULL;

-- 1.2) Forzar NOT NULL y DEFAULT NOW() (seguro aunque ya esté aplicado)
ALTER TABLE viaje ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE viaje ALTER COLUMN updated_at SET DEFAULT NOW();

-- 2) Asegurar hora_salida_programada no nula
-- (En V2 existía hora_salida NOT NULL; en V8 se añadió hora_salida_programada sin NOT NULL)
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS hora_salida_programada TIME;

-- 2.1) Rellenar con hora_salida si está en NULL
UPDATE viaje 
SET hora_salida_programada = COALESCE(hora_salida_programada, hora_salida)
WHERE hora_salida_programada IS NULL;

-- 2.2) Forzar NOT NULL en hora_salida_programada para alinear con la entidad
ALTER TABLE viaje ALTER COLUMN hora_salida_programada SET NOT NULL;

-- 3) (Opcional de robustez) Alinear defaults/constraints de auditoría
-- created_at ya existía en V2 como NOT NULL DEFAULT NOW(); reforzamos por si acaso
ALTER TABLE viaje ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE viaje ALTER COLUMN created_at SET DEFAULT NOW();
