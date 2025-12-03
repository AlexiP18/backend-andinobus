-- V40: Agregar campos para capacidad operativa y descansos diferenciados
-- Fecha: 2025-11-30

-- Agregar nuevos campos a frecuencia_config_cooperativa
ALTER TABLE frecuencia_config_cooperativa 
ADD COLUMN IF NOT EXISTS descanso_interprovincial_minutos INTEGER DEFAULT 120;

ALTER TABLE frecuencia_config_cooperativa 
ADD COLUMN IF NOT EXISTS descanso_intraprovincial_minutos INTEGER DEFAULT 45;

ALTER TABLE frecuencia_config_cooperativa 
ADD COLUMN IF NOT EXISTS umbral_interprovincial_km DECIMAL(10,2) DEFAULT 100.0;

ALTER TABLE frecuencia_config_cooperativa 
ADD COLUMN IF NOT EXISTS semanas_planificacion_defecto INTEGER DEFAULT 1;

ALTER TABLE frecuencia_config_cooperativa 
ADD COLUMN IF NOT EXISTS semanas_planificacion_max INTEGER DEFAULT 4;

-- Comentarios para documentar
COMMENT ON COLUMN frecuencia_config_cooperativa.descanso_interprovincial_minutos IS 'Tiempo de descanso en minutos para viajes interprovinciales (>100km)';
COMMENT ON COLUMN frecuencia_config_cooperativa.descanso_intraprovincial_minutos IS 'Tiempo de descanso en minutos para viajes intraprovinciales (<100km)';
COMMENT ON COLUMN frecuencia_config_cooperativa.umbral_interprovincial_km IS 'Distancia en km a partir de la cual un viaje se considera interprovincial';
COMMENT ON COLUMN frecuencia_config_cooperativa.semanas_planificacion_defecto IS 'Semanas por defecto para generación automática de frecuencias';
COMMENT ON COLUMN frecuencia_config_cooperativa.semanas_planificacion_max IS 'Máximo de semanas permitidas para planificación';
