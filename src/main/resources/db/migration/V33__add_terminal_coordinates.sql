-- V33: Añadir coordenadas a los terminales terrestres de Ecuador
-- Las coordenadas son aproximadas a las ubicaciones de las terminales principales de cada ciudad

-- AZUAY
UPDATE terminal SET latitud = -2.8833, longitud = -78.9877 WHERE nombre = 'Cuenca' AND provincia = 'AZUAY';
UPDATE terminal SET latitud = -2.8833, longitud = -78.7789 WHERE nombre = 'Gualaceo' AND provincia = 'AZUAY';
UPDATE terminal SET latitud = -3.3333, longitud = -79.0667 WHERE nombre = 'Nabón' AND provincia = 'AZUAY';

-- BOLÍVAR
UPDATE terminal SET latitud = -1.5918, longitud = -79.0010 WHERE nombre = 'Guaranda' AND provincia = 'BOLÍVAR';

-- CAÑAR
UPDATE terminal SET latitud = -2.5574, longitud = -78.9353 WHERE nombre = 'Azogues' AND provincia = 'CAÑAR';
UPDATE terminal SET latitud = -2.2233, longitud = -78.9320 WHERE nombre = 'Cañar' AND provincia = 'CAÑAR';
UPDATE terminal SET latitud = -2.3500, longitud = -79.2167 WHERE nombre = 'La Troncal' AND provincia = 'CAÑAR';

-- CARCHI
UPDATE terminal SET latitud = 0.8258, longitud = -77.9881 WHERE nombre = 'Tulcán' AND provincia = 'CARCHI';
UPDATE terminal SET latitud = 0.5500, longitud = -77.9333 WHERE nombre = 'El Ángel' AND provincia = 'CARCHI';

-- CHIMBORAZO
UPDATE terminal SET latitud = -1.6596, longitud = -78.6497 WHERE nombre = 'Riobamba' AND provincia = 'CHIMBORAZO';
UPDATE terminal SET latitud = -2.0000, longitud = -78.7167 WHERE nombre = 'Alausí' AND provincia = 'CHIMBORAZO';

-- COTOPAXI
UPDATE terminal SET latitud = -0.9346, longitud = -78.6128 WHERE nombre = 'Latacunga' AND provincia = 'COTOPAXI';
UPDATE terminal SET latitud = -0.9333, longitud = -79.2333 WHERE nombre = 'La Maná' AND provincia = 'COTOPAXI';

-- EL ORO
UPDATE terminal SET latitud = -3.2581, longitud = -79.9606 WHERE nombre = 'Machala' AND provincia = 'EL ORO';
UPDATE terminal SET latitud = -3.4333, longitud = -79.9500 WHERE nombre = 'Santa Rosa' AND provincia = 'EL ORO';
UPDATE terminal SET latitud = -3.6628, longitud = -79.9628 WHERE nombre = 'Huaquillas' AND provincia = 'EL ORO';
UPDATE terminal SET latitud = -3.3833, longitud = -79.7667 WHERE nombre = 'Pasaje' AND provincia = 'EL ORO';

-- ESMERALDAS
UPDATE terminal SET latitud = 0.9681, longitud = -79.6536 WHERE nombre = 'Esmeraldas' AND provincia = 'ESMERALDAS';
UPDATE terminal SET latitud = 1.0667, longitud = -79.4000 WHERE nombre = 'Atacames' AND provincia = 'ESMERALDAS';
UPDATE terminal SET latitud = 0.8500, longitud = -79.7333 WHERE nombre = 'Quinindé' AND provincia = 'ESMERALDAS';

-- GALÁPAGOS
UPDATE terminal SET latitud = -0.9003, longitud = -89.6087 WHERE nombre = 'Puerto Baquerizo' AND provincia = 'GALÁPAGOS';
UPDATE terminal SET latitud = -0.7500, longitud = -90.3167 WHERE nombre = 'Puerto Ayora' AND provincia = 'GALÁPAGOS';

-- GUAYAS
UPDATE terminal SET latitud = -2.1894, longitud = -79.8891 WHERE nombre = 'Guayaquil' AND provincia = 'GUAYAS';
UPDATE terminal SET latitud = -2.2500, longitud = -79.5500 WHERE nombre = 'Milagro' AND provincia = 'GUAYAS';
UPDATE terminal SET latitud = -2.2000, longitud = -79.9833 WHERE nombre = 'Durán' AND provincia = 'GUAYAS';
UPDATE terminal SET latitud = -2.0667, longitud = -79.9333 WHERE nombre = 'Daule' AND provincia = 'GUAYAS';
UPDATE terminal SET latitud = -2.7167, longitud = -80.2333 WHERE nombre = 'Playas' AND provincia = 'GUAYAS';

-- IMBABURA
UPDATE terminal SET latitud = 0.3517, longitud = -78.1223 WHERE nombre = 'Ibarra' AND provincia = 'IMBABURA';
UPDATE terminal SET latitud = 0.2333, longitud = -78.2667 WHERE nombre = 'Otavalo' AND provincia = 'IMBABURA';
UPDATE terminal SET latitud = 0.2167, longitud = -78.1167 WHERE nombre = 'Cotacachi' AND provincia = 'IMBABURA';

-- LOJA
UPDATE terminal SET latitud = -3.9936, longitud = -79.2042 WHERE nombre = 'Loja' AND provincia = 'LOJA';
UPDATE terminal SET latitud = -4.0333, longitud = -79.2167 WHERE nombre = 'Catamayo' AND provincia = 'LOJA';
UPDATE terminal SET latitud = -4.8667, longitud = -79.2000 WHERE nombre = 'Macará' AND provincia = 'LOJA';

-- LOS RÍOS
UPDATE terminal SET latitud = -1.8019, longitud = -79.5342 WHERE nombre = 'Babahoyo' AND provincia = 'LOS RÍOS';
UPDATE terminal SET latitud = -1.0333, longitud = -79.4667 WHERE nombre = 'Quevedo' AND provincia = 'LOS RÍOS';
UPDATE terminal SET latitud = -1.5833, longitud = -79.5500 WHERE nombre = 'Ventanas' AND provincia = 'LOS RÍOS';

-- MANABÍ
UPDATE terminal SET latitud = -1.0544, longitud = -80.4525 WHERE nombre = 'Portoviejo' AND provincia = 'MANABÍ';
UPDATE terminal SET latitud = -0.9500, longitud = -80.7333 WHERE nombre = 'Manta' AND provincia = 'MANABÍ';
UPDATE terminal SET latitud = -0.9500, longitud = -80.0500 WHERE nombre = 'Chone' AND provincia = 'MANABÍ';
UPDATE terminal SET latitud = -1.2667, longitud = -80.4333 WHERE nombre = 'Jipijapa' AND provincia = 'MANABÍ';
UPDATE terminal SET latitud = -1.5500, longitud = -80.8000 WHERE nombre = 'Puerto López' AND provincia = 'MANABÍ';

-- MORONA SANTIAGO
UPDATE terminal SET latitud = -2.5397, longitud = -78.1242 WHERE nombre = 'Macas' AND provincia = 'MORONA SANTIAGO';
UPDATE terminal SET latitud = -3.0500, longitud = -78.1833 WHERE nombre = 'Gualaquiza' AND provincia = 'MORONA SANTIAGO';

-- NAPO
UPDATE terminal SET latitud = -0.9894, longitud = -77.8131 WHERE nombre = 'Tena' AND provincia = 'NAPO';
UPDATE terminal SET latitud = -0.7167, longitud = -77.5833 WHERE nombre = 'Archidona' AND provincia = 'NAPO';

-- ORELLANA
UPDATE terminal SET latitud = -0.4653, longitud = -76.9872 WHERE nombre = 'Coca' AND provincia = 'ORELLANA';

-- PASTAZA
UPDATE terminal SET latitud = -1.4833, longitud = -78.0028 WHERE nombre = 'Puyo' AND provincia = 'PASTAZA';

-- PICHINCHA
UPDATE terminal SET latitud = -0.1807, longitud = -78.4678 WHERE nombre = 'Quitumbe' AND provincia = 'PICHINCHA';
UPDATE terminal SET latitud = -0.1000, longitud = -78.4667 WHERE nombre = 'Carcelén' AND provincia = 'PICHINCHA';
UPDATE terminal SET latitud = -0.2392, longitud = -78.5219 WHERE nombre = 'La Ofelia' AND provincia = 'PICHINCHA';
UPDATE terminal SET latitud = 0.0000, longitud = -78.4500 WHERE nombre = 'Cayambe' AND provincia = 'PICHINCHA';
UPDATE terminal SET latitud = -0.3167, longitud = -78.5500 WHERE nombre = 'Sangolquí' AND provincia = 'PICHINCHA';

-- SANTA ELENA
UPDATE terminal SET latitud = -2.2261, longitud = -80.8586 WHERE nombre = 'Santa Elena' AND provincia = 'SANTA ELENA';
UPDATE terminal SET latitud = -2.0333, longitud = -80.7333 WHERE nombre = 'Salinas' AND provincia = 'SANTA ELENA';
UPDATE terminal SET latitud = -1.9500, longitud = -80.7333 WHERE nombre = 'La Libertad' AND provincia = 'SANTA ELENA';

-- SANTO DOMINGO DE LOS TSÁCHILAS
UPDATE terminal SET latitud = -0.2542, longitud = -79.1719 WHERE nombre = 'Santo Domingo' AND provincia = 'SANTO DOMINGO DE LOS TSÁCHILAS';

-- SUCUMBÍOS
UPDATE terminal SET latitud = 0.0858, longitud = -76.8831 WHERE nombre = 'Lago Agrio' AND provincia = 'SUCUMBÍOS';
UPDATE terminal SET latitud = 0.3333, longitud = -77.0500 WHERE nombre = 'Cascales' AND provincia = 'SUCUMBÍOS';

-- TUNGURAHUA
UPDATE terminal SET latitud = -1.2490, longitud = -78.6167 WHERE nombre = 'Ambato' AND provincia = 'TUNGURAHUA';
UPDATE terminal SET latitud = -1.4000, longitud = -78.4167 WHERE nombre = 'Baños' AND provincia = 'TUNGURAHUA';
UPDATE terminal SET latitud = -1.1833, longitud = -78.6167 WHERE nombre = 'Pelileo' AND provincia = 'TUNGURAHUA';

-- ZAMORA CHINCHIPE
UPDATE terminal SET latitud = -4.0668, longitud = -78.9538 WHERE nombre = 'Zamora' AND provincia = 'ZAMORA CHINCHIPE';
UPDATE terminal SET latitud = -3.9167, longitud = -78.6833 WHERE nombre = 'Yantzaza' AND provincia = 'ZAMORA CHINCHIPE';

-- Verificar que se actualizaron las coordenadas
SELECT COUNT(*) as terminales_con_coordenadas FROM terminal WHERE latitud IS NOT NULL AND longitud IS NOT NULL;
