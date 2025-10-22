-- ==========================================
-- ERP ASSIP SOLIDARIA Y FINANCIERA
-- MIGRACIÓN V003 — AJUSTES DE PERMISOS Y RELACIÓN ROL–PERMISO
-- Fecha: 22/10/2025
-- ==========================================

SET search_path TO seguridad;

-- ==========================================
-- 1. AJUSTE DE TABLA PERMISOS
-- ==========================================
-- Elimina la vieja referencia 1:N con roles y agrega campo activo
ALTER TABLE permisos
DROP CONSTRAINT IF EXISTS permisos_id_rol_fkey;

ALTER TABLE permisos
DROP COLUMN IF EXISTS id_rol;

ALTER TABLE permisos
ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE;

-- ==========================================
-- 2. RELACIÓN ROL–PERMISO (muchos a muchos)
-- ==========================================
CREATE TABLE IF NOT EXISTS rol_permisos (
    id_rol INT REFERENCES roles(id_rol) ON DELETE CASCADE,
    id_permiso INT REFERENCES permisos(id_permiso) ON DELETE CASCADE,
    PRIMARY KEY (id_rol, id_permiso)
);

-- ==========================================
-- 3. DATOS BASE DE ROLES
-- ==========================================
INSERT INTO roles (nombre, descripcion, activo)
VALUES
    ('ADMIN', 'Administrador general del sistema', TRUE),
    ('OPERATIVO', 'Usuario operativo con permisos limitados', TRUE),
    ('LECTURA', 'Usuario con permisos solo de consulta', TRUE)
ON CONFLICT (nombre) DO NOTHING;

-- ==========================================
-- 4. DATOS BASE DE PERMISOS
-- ==========================================
INSERT INTO permisos (codigo, descripcion, activo)
VALUES
    ('HOJAVIDA_VIEW',        'Consultar hojas de vida', TRUE),
    ('HOJAVIDA_EDIT',        'Editar hojas de vida', TRUE),
    ('CONTABILIDAD_VIEW',    'Consultar comprobantes contables', TRUE),
    ('CONTABILIDAD_EDIT',    'Registrar comprobantes contables', TRUE),
    ('USUARIOS_VIEW',        'Listar usuarios del sistema', TRUE),
    ('USUARIOS_EDIT',        'Gestionar usuarios y roles', TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- ==========================================
-- 5. ASIGNACIÓN DE PERMISOS A ROLES
-- ==========================================
-- ADMIN → todos los permisos
INSERT INTO rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r, permisos p
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

-- OPERATIVO → todos excepto USUARIOS_EDIT
INSERT INTO rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r
JOIN permisos p ON p.codigo != 'USUARIOS_EDIT'
WHERE r.nombre = 'OPERATIVO'
ON CONFLICT DO NOTHING;

-- LECTURA → solo permisos que terminan en _VIEW
INSERT INTO rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r
JOIN permisos p ON p.codigo LIKE '%_VIEW'
WHERE r.nombre = 'LECTURA'
ON CONFLICT DO NOTHING;
