-- Agregar campos de configuración a la tabla cooperativa
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS descripcion TEXT;
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS color_primario VARCHAR(7) DEFAULT '#16a34a';
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS color_secundario VARCHAR(7) DEFAULT '#15803d';
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS facebook VARCHAR(255);
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS twitter VARCHAR(255);
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS instagram VARCHAR(255);
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS linkedin VARCHAR(255);
ALTER TABLE cooperativa ADD COLUMN IF NOT EXISTS youtube VARCHAR(255);

-- Comentarios para documentación
COMMENT ON COLUMN cooperativa.descripcion IS 'Descripción de la cooperativa para mostrar en página pública';
COMMENT ON COLUMN cooperativa.color_primario IS 'Color primario corporativo en formato hexadecimal';
COMMENT ON COLUMN cooperativa.color_secundario IS 'Color secundario corporativo en formato hexadecimal';
COMMENT ON COLUMN cooperativa.facebook IS 'URL del perfil de Facebook';
COMMENT ON COLUMN cooperativa.twitter IS 'URL del perfil de Twitter/X';
COMMENT ON COLUMN cooperativa.instagram IS 'URL del perfil de Instagram';
COMMENT ON COLUMN cooperativa.linkedin IS 'URL del perfil de LinkedIn';
COMMENT ON COLUMN cooperativa.youtube IS 'URL del canal de YouTube';
