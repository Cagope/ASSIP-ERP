-- ==========================================
-- ERP ASSIP SOLIDARIA Y FINANCIERA
-- MIGRACIÓN V002 — TABLAS BASE DE SEGURIDAD
-- Fecha: 22/10/2025
-- ==========================================

SET search_path TO seguridad;

-- ==========================================
-- 1. TABLA DE ROLES
-- ==========================================
CREATE TABLE IF NOT EXISTS roles (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE
);

-- ==========================================
-- 2. TABLA DE USUARIOS
-- ==========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(200),
    email VARCHAR(150),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion VARCHAR(50),
    usuario_actualizacion VARCHAR(50),
    id_rol INT REFERENCES roles(id_rol)
);

CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- ==========================================
-- 3. TABLA DE PERMISOS
-- ==========================================
CREATE TABLE IF NOT EXISTS permisos (
    id_permiso SERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_rol INT REFERENCES roles(id_rol)
);

-- ==========================================
-- 4. TABLA DE LOG DE EVENTOS
-- ==========================================
CREATE TABLE IF NOT EXISTS log_evento (
    id_log SERIAL PRIMARY KEY,
    id_usuario INT,
    modulo VARCHAR(100),
    accion VARCHAR(100),
    descripcion TEXT,
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(50),
    user_agent TEXT
);
-- ==========================================
-- ERP ASSIP SOLIDARIA Y FINANCIERA
-- MIGRACIÓN V002 — TABLAS BASE DE SEGURIDAD
-- Fecha: 22/10/2025
-- ==========================================

SET search_path TO seguridad;

-- ==========================================
-- 1. TABLA DE ROLES
-- ==========================================
CREATE TABLE IF NOT EXISTS roles (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE
);

-- ==========================================
-- 2. TABLA DE USUARIOS
-- ==========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(200),
    email VARCHAR(150),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion VARCHAR(50),
    usuario_actualizacion VARCHAR(50),
    id_rol INT REFERENCES roles(id_rol)
);

CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- ==========================================
-- 3. TABLA DE PERMISOS
-- ==========================================
CREATE TABLE IF NOT EXISTS permisos (
    id_permiso SERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_rol INT REFERENCES roles(id_rol)
);

-- ==========================================
-- 4. TABLA DE LOG DE EVENTOS
-- ==========================================
CREATE TABLE IF NOT EXISTS log_evento (
    id_log SERIAL PRIMARY KEY,
    id_usuario INT,
    modulo VARCHAR(100),
    accion VARCHAR(100),
    descripcion TEXT,
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(50),
    user_agent TEXT
);
