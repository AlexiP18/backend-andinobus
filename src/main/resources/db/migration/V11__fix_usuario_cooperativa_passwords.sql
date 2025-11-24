-- V11: Corregir contraseñas de usuarios cooperativa
-- Fecha: 2025-11-16
-- Descripción: Actualizar el password_hash de los usuarios de prueba
-- La contraseña es: admin123
-- Hash BCrypt generado: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdx/H5D0i

-- Actualizar usuario ADMIN de cooperativa
UPDATE usuario_cooperativa 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdx/H5D0i'
WHERE email = 'admin@andinobus.com';

-- Actualizar usuario OFICINISTA
UPDATE usuario_cooperativa 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdx/H5D0i'
WHERE email = 'oficinista@andinobus.com';

-- Actualizar usuario CHOFER
UPDATE usuario_cooperativa 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdx/H5D0i'
WHERE email = 'chofer@andinobus.com';
