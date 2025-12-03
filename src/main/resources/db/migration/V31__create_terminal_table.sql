-- V31: Crear tabla de terminales terrestres
-- Los terminales son los puntos físicos donde operan los buses

CREATE TABLE terminal (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    canton VARCHAR(100) NOT NULL,
    tipologia VARCHAR(2) NOT NULL CHECK (tipologia IN ('T1', 'T2', 'T3', 'T4', 'T5')),
    andenes INTEGER NOT NULL DEFAULT 1,
    frecuencias_por_anden INTEGER NOT NULL DEFAULT 96,
    max_frecuencias_diarias INTEGER NOT NULL DEFAULT 96,
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    direccion VARCHAR(300),
    telefono VARCHAR(20),
    horario_apertura VARCHAR(5),
    horario_cierre VARCHAR(5),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para búsquedas frecuentes
CREATE INDEX idx_terminal_provincia ON terminal(provincia);
CREATE INDEX idx_terminal_canton ON terminal(canton);
CREATE INDEX idx_terminal_tipologia ON terminal(tipologia);
CREATE INDEX idx_terminal_activo ON terminal(activo);

-- Comentarios de documentación
COMMENT ON TABLE terminal IS 'Terminales terrestres donde operan los buses';
COMMENT ON COLUMN terminal.tipologia IS 'T1=básico, T2=pequeño, T3=mediano, T4=grande, T5=principal/hub';
COMMENT ON COLUMN terminal.andenes IS 'Número de andenes para embarque/desembarque';
COMMENT ON COLUMN terminal.frecuencias_por_anden IS 'Máximo de frecuencias por andén por día (default 96 = 1 cada 15min)';
COMMENT ON COLUMN terminal.max_frecuencias_diarias IS 'Capacidad total = andenes * frecuencias_por_anden';

-- Insertar terminales del Ecuador (datos de terminales.csv y maximo_frecuencias.csv)
INSERT INTO terminal (nombre, provincia, canton, tipologia, andenes, frecuencias_por_anden, max_frecuencias_diarias) VALUES
-- AZUAY
('Cuenca', 'AZUAY', 'Cuenca', 'T5', 43, 96, 4128),
('Gualaceo', 'AZUAY', 'Gualaceo', 'T3', 18, 96, 1728),
('Nabón', 'AZUAY', 'Nabón', 'T3', 8, 96, 768),
('Paute', 'AZUAY', 'Paute', 'T2', 4, 96, 384),
('Sigsig', 'AZUAY', 'Sigsig', 'T2', 5, 96, 480),

-- BOLÍVAR
('Guaranda Viejo', 'BOLÍVAR', 'Guaranda', 'T3', 8, 96, 768),

-- CAÑAR
('Azogues', 'CAÑAR', 'Azogues', 'T3', 23, 96, 2208),
('Cañar', 'CAÑAR', 'Cañar', 'T4', 24, 96, 2304),
('Deleg', 'CAÑAR', 'Deleg', 'T3', 7, 96, 672),
('La Troncal', 'CAÑAR', 'La Troncal', 'T3', 20, 96, 1920),

-- CARCHI
('San Gabriel', 'CARCHI', 'Montúfar', 'T3', 9, 96, 864),
('Tulcán', 'CARCHI', 'Tulcán', 'T3', 10, 96, 960),

-- CHIMBORAZO
('Chambo', 'CHIMBORAZO', 'Chambo', 'T2', 5, 96, 480),
('Riobamba Intraprovincial', 'CHIMBORAZO', 'Riobamba', 'T3', 12, 96, 1152),
('Riobamba', 'CHIMBORAZO', 'Riobamba', 'T3', 8, 96, 768),

-- COTOPAXI
('Latacunga', 'COTOPAXI', 'Latacunga', 'T3', 18, 96, 1728),
('Pujilí', 'COTOPAXI', 'Pujilí', 'T1', 1, 96, 96),
('San Miguel de Salcedo', 'COTOPAXI', 'Salcedo', 'T3', 14, 96, 1344),

-- EL ORO
('Machala', 'EL ORO', 'Machala', 'T5', 49, 96, 4704),
('Binacional Santa Rosa', 'EL ORO', 'Santa Rosa', 'T4', 28, 96, 2688),

-- ESMERALDAS
('Puerto Green Center', 'ESMERALDAS', 'Esmeraldas', 'T3', 16, 96, 1536),
('Quinindé', 'ESMERALDAS', 'Quinindé', 'T3', 11, 96, 1056),

-- GUAYAS
('Durán', 'GUAYAS', 'Durán', 'T3', 12, 96, 1152),
('Satélite Pascuales', 'GUAYAS', 'Guayaquil', 'T3', 22, 96, 2112),
('Jaime Roldós Aguilera', 'GUAYAS', 'Guayaquil', 'T5', 112, 96, 10752),
('Martha Bucaram', 'GUAYAS', 'Milagro', 'T3', 16, 96, 1536),

-- IMBABURA
('Cotacachi', 'IMBABURA', 'Cotacachi', 'T2', 6, 96, 576),
('Imbabus', 'IMBABURA', 'Ibarra', 'T3', 20, 96, 1920),
('Otavalo', 'IMBABURA', 'Otavalo', 'T3', 7, 96, 672),

-- LOJA
('Calvas', 'LOJA', 'Calvas', 'T2', 5, 96, 480),
('Celica', 'LOJA', 'Celica', 'T2', 3, 96, 288),
('Amaluza-Espíndola', 'LOJA', 'Espíndola', 'T2', 4, 96, 384),
('Reina del Cisne', 'LOJA', 'Loja', 'T3', 16, 96, 1536),

-- LOS RÍOS
('Babahoyo', 'LOS RÍOS', 'Babahoyo', 'T3', 20, 96, 1920),
('Mocache', 'LOS RÍOS', 'Mocache', 'T3', 7, 96, 672),
('Quevedo', 'LOS RÍOS', 'Quevedo', 'T5', 39, 96, 3744),
('Gilberto Gordillo Ruiz', 'LOS RÍOS', 'Ventanas', 'T3', 18, 96, 1728),
('Vinces', 'LOS RÍOS', 'Vinces', 'T4', 34, 96, 3264),

-- MANABÍ
('Calceta-Bolívar', 'MANABÍ', 'Bolívar', 'T3', 8, 96, 768),
('Chone', 'MANABÍ', 'Chone', 'T3', 13, 96, 1248),
('Carmen', 'MANABÍ', 'El Carmen', 'T3', 15, 96, 1440),
('Xipixapa', 'MANABÍ', 'Jipijapa', 'T3', 8, 96, 768),
('Manta', 'MANABÍ', 'Manta', 'T5', 49, 96, 4704),
('Paján', 'MANABÍ', 'Paján', 'T2', 5, 96, 480),
('Pedernales', 'MANABÍ', 'Pedernales', 'T3', 15, 96, 1440),
('Portoviejo', 'MANABÍ', 'Portoviejo', 'T5', 40, 96, 3840),
('Puerto López', 'MANABÍ', 'Puerto López', 'T2', 6, 96, 576),
('Bahía de Caráquez', 'MANABÍ', 'Sucre', 'T2', 6, 96, 576),

-- MORONA SANTIAGO
('Gualaquiza', 'MORONA SANTIAGO', 'Gualaquiza', 'T3', 13, 96, 1248),
('Dr. Roberto Villareal', 'MORONA SANTIAGO', 'Macas', 'T4', 24, 96, 2304),
('Sucúa', 'MORONA SANTIAGO', 'Sucúa', 'T3', 7, 96, 672),

-- NAPO
('Tena', 'NAPO', 'Tena', 'T2', 3, 96, 288),

-- ORELLANA
('El Coca EP', 'ORELLANA', 'Francisco de Orellana', 'T3', 14, 96, 1344),
('Loreto', 'ORELLANA', 'Loreto', 'T2', 5, 96, 480),

-- PASTAZA
('Ciudad del Puyo', 'PASTAZA', 'Pastaza', 'T2', 4, 96, 384),
('Pastaza-Intraprovincial', 'PASTAZA', 'Pastaza', 'T1', 1, 96, 96),

-- PICHINCHA
('Cayambe', 'PICHINCHA', 'Cayambe', 'T2', 4, 96, 384),
('Quitumbe', 'PICHINCHA', 'Quito', 'T5', 66, 96, 6336),
('Carcelén', 'PICHINCHA', 'Quito', 'T3', 13, 96, 1248),

-- SANTA ELENA
('Sumpa', 'SANTA ELENA', 'Santa Elena', 'T3', 20, 96, 1920),

-- SANTO DOMINGO
('Satélite La Concordia', 'SANTO DOMINGO', 'La Concordia', 'T1', 1, 96, 96),
('Santo Domingo', 'SANTO DOMINGO', 'Santo Domingo', 'T5', 36, 96, 3456),

-- SUCUMBÍOS
('Lago Agrio', 'SUCUMBÍOS', 'Lago Agrio', 'T3', 12, 96, 1152),

-- TUNGURAHUA
('Ingahurco', 'TUNGURAHUA', 'Ambato', 'T3', 10, 96, 960),
('Mercado Mayorista', 'TUNGURAHUA', 'Ambato', 'T3', 16, 96, 1536),
('Cashapamba', 'TUNGURAHUA', 'Ambato', 'T4', 28, 96, 2688),
('Huachi San Francisco', 'TUNGURAHUA', 'Ambato', 'T3', 20, 96, 1920),
('Jorge Viteri Guevara', 'TUNGURAHUA', 'Baños', 'T3', 8, 96, 768),
('Píllaro', 'TUNGURAHUA', 'Santiago de Píllaro', 'T2', 2, 96, 192),

-- ZAMORA CHINCHIPE
('Zumba', 'ZAMORA CHINCHIPE', 'Chinchipe', 'T3', 10, 96, 960),
('El Pangui', 'ZAMORA CHINCHIPE', 'El Pangui', 'T2', 4, 96, 384),
('Paquisha', 'ZAMORA CHINCHIPE', 'Paquisha', 'T2', 4, 96, 384),
('Yantzaza', 'ZAMORA CHINCHIPE', 'Yantzaza', 'T2', 5, 96, 480),
('Zamora', 'ZAMORA CHINCHIPE', 'Zamora', 'T3', 12, 96, 1152);
