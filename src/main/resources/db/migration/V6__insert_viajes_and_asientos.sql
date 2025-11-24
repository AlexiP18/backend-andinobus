-- V6: Crear viajes de ejemplo y generar asientos con formato alfanumérico (1A-10D)

-- 1. Crear viajes de ejemplo para las frecuencias existentes (si no existen)
INSERT INTO viaje (frecuencia_id, bus_id, fecha, estado)
SELECT f.id, b.id, CURRENT_DATE, 'PROGRAMADO'
FROM frecuencia f
CROSS JOIN bus b
WHERE f.origen = 'Quito' AND f.destino = 'Guayaquil' 
  AND b.placa = 'ABC-1234'
  AND NOT EXISTS (
    SELECT 1 FROM viaje v 
    WHERE v.frecuencia_id = f.id 
      AND v.fecha = CURRENT_DATE
  );

INSERT INTO viaje (frecuencia_id, bus_id, fecha, estado)
SELECT f.id, b.id, CURRENT_DATE, 'PROGRAMADO'
FROM frecuencia f
CROSS JOIN bus b
WHERE f.origen = 'Quito' AND f.destino = 'Cuenca'
  AND b.placa = 'DEF-5678'
  AND NOT EXISTS (
    SELECT 1 FROM viaje v 
    WHERE v.frecuencia_id = f.id 
      AND v.fecha = CURRENT_DATE
  );

INSERT INTO viaje (frecuencia_id, bus_id, fecha, estado)
SELECT f.id, b.id, CURRENT_DATE + INTERVAL '1 day', 'PROGRAMADO'
FROM frecuencia f
CROSS JOIN bus b
WHERE f.origen = 'Guayaquil' AND f.destino = 'Loja'
  AND b.placa = 'ABC-1234'
  AND NOT EXISTS (
    SELECT 1 FROM viaje v 
    WHERE v.frecuencia_id = f.id 
      AND v.fecha = CURRENT_DATE + INTERVAL '1 day'
  );

-- 2. Generar asientos para cada viaje (formato 1A-10D = 40 asientos por bus)
DO $$
DECLARE
    v_viaje RECORD;
    v_fila INTEGER;
    v_columna CHAR(1);
BEGIN
    -- Para cada viaje PROGRAMADO que no tenga asientos
    FOR v_viaje IN 
        SELECT v.id 
        FROM viaje v 
        WHERE v.estado = 'PROGRAMADO'
          AND NOT EXISTS (
            SELECT 1 FROM viaje_asiento va WHERE va.viaje_id = v.id
          )
    LOOP
        -- Generar asientos: 10 filas x 4 columnas (A, B, C, D)
        FOR v_fila IN 1..10 LOOP
            FOR v_columna IN SELECT unnest(ARRAY['A', 'B', 'C', 'D']) LOOP
                INSERT INTO viaje_asiento (
                    viaje_id, 
                    numero_asiento, 
                    tipo_asiento, 
                    estado, 
                    created_at, 
                    updated_at
                )
                VALUES (
                    v_viaje.id,
                    v_fila || v_columna,  -- Formato: 1A, 1B, 1C, 1D, 2A, 2B, etc.
                    'NORMAL',
                    'DISPONIBLE',
                    NOW(),
                    NOW()
                );
            END LOOP;
        END LOOP;
        
        RAISE NOTICE 'Asientos creados para viaje ID: %', v_viaje.id;
    END LOOP;
END $$;
