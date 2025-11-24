-- V8: Reestructuración de roles y creación de usuario_cooperativa y viaje
-- Fecha: 2025-11-16
-- Autor: Sistema AndinoBus

-- =====================================================
-- TABLA: usuario_cooperativa
-- Descripción: Todos los usuarios del entorno COOPERATIVA (ADMIN, OFICINISTA, CHOFER)
-- =====================================================

CREATE TABLE IF NOT EXISTS usuario_cooperativa (
    id BIGSERIAL PRIMARY KEY,
    
    -- Datos de autenticación
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Datos personales
    nombres VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    cedula VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    
    -- Relación con cooperativa
    cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id) ON DELETE CASCADE,
    
    -- Rol dentro de la cooperativa
    rol_cooperativa VARCHAR(20) NOT NULL, -- 'ADMIN', 'OFICINISTA', 'CHOFER'
    
    -- Campos específicos para OFICINISTA
    codigo_empleado VARCHAR(20),
    terminal VARCHAR(50),
    
    -- Campos específicos para CHOFER
    licencia_conducir VARCHAR(50),
    fecha_vencimiento_licencia DATE,
    tipo_licencia VARCHAR(10), -- 'C', 'D', 'E'
    experiencia_anios INT DEFAULT 0,
    calificacion DECIMAL(3,2) DEFAULT 0.00,
    
    -- Control de estado
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT uk_cooperativa_codigo_empleado UNIQUE(cooperativa_id, codigo_empleado),
    CONSTRAINT chk_rol_cooperativa CHECK (rol_cooperativa IN ('ADMIN', 'OFICINISTA', 'CHOFER')),
    CONSTRAINT chk_oficinista_campos CHECK (
        rol_cooperativa != 'OFICINISTA' OR 
        (codigo_empleado IS NOT NULL)
    ),
    CONSTRAINT chk_chofer_campos CHECK (
        rol_cooperativa != 'CHOFER' OR 
        (licencia_conducir IS NOT NULL AND fecha_vencimiento_licencia IS NOT NULL)
    )
);

-- Índices para optimizar consultas
CREATE INDEX idx_usuario_cooperativa_cooperativa_id ON usuario_cooperativa(cooperativa_id);
CREATE INDEX idx_usuario_cooperativa_rol ON usuario_cooperativa(rol_cooperativa);
CREATE INDEX idx_usuario_cooperativa_email ON usuario_cooperativa(email);
CREATE INDEX idx_usuario_cooperativa_activo ON usuario_cooperativa(activo);

-- =====================================================
-- AJUSTES: viaje
-- Descripción: Agregar columnas e índices si no existen (tabla creada en V2)
-- =====================================================

-- Columnas adicionales para viaje (si no existen)
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS chofer_id BIGINT REFERENCES usuario_cooperativa(id) ON DELETE RESTRICT;
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS hora_salida_programada TIME;
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS hora_salida_real TIME;
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS hora_llegada_estimada TIME;
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS hora_llegada_real TIME;
ALTER TABLE viaje ADD COLUMN IF NOT EXISTS observaciones TEXT;

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS ix_viaje_fecha ON viaje(fecha);
CREATE INDEX IF NOT EXISTS ix_viaje_frecuencia ON viaje(frecuencia_id);
CREATE INDEX IF NOT EXISTS idx_viaje_bus_id ON viaje(bus_id);
CREATE INDEX IF NOT EXISTS idx_viaje_chofer_id ON viaje(chofer_id);
CREATE INDEX IF NOT EXISTS idx_viaje_estado ON viaje(estado);
CREATE INDEX IF NOT EXISTS idx_viaje_fecha_estado ON viaje(fecha, estado);

-- =====================================================
-- ACTUALIZACIÓN: Tabla reserva
-- Agregar relación con viaje y oficinista
-- =====================================================

ALTER TABLE reserva ADD COLUMN IF NOT EXISTS viaje_id BIGINT REFERENCES viaje(id) ON DELETE SET NULL;
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS oficinista_id BIGINT REFERENCES usuario_cooperativa(id) ON DELETE SET NULL;
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS venta_presencial BOOLEAN DEFAULT false;

-- Índices para las nuevas columnas
CREATE INDEX IF NOT EXISTS ix_reserva_viaje ON reserva(viaje_id);
CREATE INDEX IF NOT EXISTS ix_reserva_oficinista ON reserva(oficinista_id);

-- Constraint: si hay oficinista, debe tener rol OFICINISTA
-- Nota: Este constraint se validará en la lógica de negocio para evitar problemas de rendimiento

-- =====================================================
-- MIGRACIÓN DE DATOS: Usuarios COOPERATIVA existentes
-- Migrar usuarios con rol='COOPERATIVA' de app_user a usuario_cooperativa
-- =====================================================

-- Insertar usuarios COOPERATIVA existentes en la nueva tabla como ADMIN
INSERT INTO usuario_cooperativa (
    email,
    password_hash,
    nombres,
    apellidos,
    cedula,
    telefono,
    cooperativa_id,
    rol_cooperativa,
    activo,
    fecha_registro,
    created_at,
    updated_at
)
SELECT 
    u.email,
    u.password_hash,
    COALESCE(u.nombres, 'Admin'),
    COALESCE(u.apellidos, 'Cooperativa'),
    COALESCE(u.email, CAST(u.id AS VARCHAR)), -- Usar email como cedula temporal si no existe
    NULL as telefono,
    1 as cooperativa_id, -- Asignar a la primera cooperativa (ajustar según necesidad)
    'ADMIN' as rol_cooperativa,
    u.activo,
    u.created_at as fecha_registro,
    u.created_at,
    CURRENT_TIMESTAMP as updated_at
FROM app_user u
WHERE u.rol = 'COOPERATIVA'
ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- DATOS DE EJEMPLO: Crear algunos usuarios de prueba
-- =====================================================

-- Usuario ADMIN de cooperativa (si no existe de la migración)
INSERT INTO usuario_cooperativa (
    email,
    password_hash,
    nombres,
    apellidos,
    cedula,
    telefono,
    cooperativa_id,
    rol_cooperativa,
    activo
) VALUES (
    'admin@andinobus.com',
    '$2a$10$xQGQq8KgVZ5VBhJH0VqXE.pqO5cF6qJ7YW6fDZLnRqE3vJ7kLH9qe', -- password: admin123
    'Administrador',
    'Cooperativa 1',
    '1234567890',
    '0999999999',
    1,
    'ADMIN',
    true
) ON CONFLICT (email) DO NOTHING;

-- Usuario OFICINISTA de ejemplo
INSERT INTO usuario_cooperativa (
    email,
    password_hash,
    nombres,
    apellidos,
    cedula,
    telefono,
    cooperativa_id,
    rol_cooperativa,
    codigo_empleado,
    terminal,
    activo
) VALUES (
    'oficinista@andinobus.com',
    '$2a$10$xQGQq8KgVZ5VBhJH0VqXE.pqO5cF6qJ7YW6fDZLnRqE3vJ7kLH9qe', -- password: admin123
    'Juan',
    'Pérez',
    '0987654321',
    '0988888888',
    1,
    'OFICINISTA',
    'OF-001',
    'Terminal Quitumbe',
    true
) ON CONFLICT (email) DO NOTHING;

-- Usuario CHOFER de ejemplo
INSERT INTO usuario_cooperativa (
    email,
    password_hash,
    nombres,
    apellidos,
    cedula,
    telefono,
    cooperativa_id,
    rol_cooperativa,
    licencia_conducir,
    fecha_vencimiento_licencia,
    tipo_licencia,
    experiencia_anios,
    calificacion,
    activo
) VALUES (
    'chofer@andinobus.com',
    '$2a$10$xQGQq8KgVZ5VBhJH0VqXE.pqO5cF6qJ7YW6fDZLnRqE3vJ7kLH9qe', -- password: admin123
    'Carlos',
    'Ramírez',
    '1122334455',
    '0977777777',
    1,
    'CHOFER',
    'EC-LC-123456',
    '2027-12-31',
    'D',
    5,
    4.75,
    true
) ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- COMENTARIOS EN TABLAS
-- =====================================================

COMMENT ON TABLE usuario_cooperativa IS 'Usuarios del entorno COOPERATIVA (ADMIN, OFICINISTA, CHOFER)';
COMMENT ON TABLE viaje IS 'Viajes programados y realizados con bus, frecuencia y chofer asignados';

COMMENT ON COLUMN usuario_cooperativa.rol_cooperativa IS 'Rol dentro de la cooperativa: ADMIN, OFICINISTA, CHOFER';
COMMENT ON COLUMN usuario_cooperativa.codigo_empleado IS 'Código único del oficinista dentro de la cooperativa';
COMMENT ON COLUMN usuario_cooperativa.licencia_conducir IS 'Número de licencia de conducir del chofer';
COMMENT ON COLUMN usuario_cooperativa.tipo_licencia IS 'Tipo de licencia: C (liviana), D (pesada), E (articulado)';

COMMENT ON COLUMN viaje.estado IS 'Estado del viaje: PROGRAMADO, EN_TERMINAL, EN_RUTA, COMPLETADO, CANCELADO';
COMMENT ON COLUMN reserva.venta_presencial IS 'Indica si el boleto fue vendido por un oficinista en terminal';
