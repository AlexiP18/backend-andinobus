-- Aumentar el tamaño de la columna dias_operacion en la tabla frecuencia
-- El valor completo "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO" tiene 52 caracteres
ALTER TABLE frecuencia ALTER COLUMN dias_operacion TYPE VARCHAR(100);
